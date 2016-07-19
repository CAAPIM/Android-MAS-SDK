/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.gui;

import android.content.Intent;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.creds.AuthorizationCodeCredentials;
import com.ca.mas.core.io.http.TrustedCertificateConfigurationTrustManager;
import com.ca.mas.core.service.MssoIntents;

import java.io.ByteArrayInputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSocket;


@Deprecated
public class SocialLoginActivity extends AbstractLogonActivity {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildLayout());


        Intent intent = getIntent();
        String url = intent.getStringExtra(MssoIntents.EXTRA_SOCIAL_LOGIN_URL);
        loadUrl(url);
    }

    protected void loadUrl(String url) {
        WebView v = getWebView();
        v.loadUrl(url);
    }

    /**
     * Retrieve the default webview to handle the handshake between the gateway and the social login platform.
     * The default WebView handles https cert verification, show progress bar, capture call back url and invoke
     * MSSOService to proceed the logon process.
     * To customize the WebView, simply override this method.
     *
     * @return The Default WebView.
     */
    protected WebView getWebView() {

        final WebView v = (WebView) findViewById(ID.WEBVIEW.ordinal());

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        WebViewDatabase.getInstance(this).clearFormData();
        WebViewDatabase.getInstance(this).clearHttpAuthUsernamePassword();
        //We have to remove the username and password for APL level < 18
        WebViewDatabase.getInstance(this).clearUsernamePassword();

        v.clearCache(true);
        v.clearFormData();
        v.getSettings().setSaveFormData(false);
        v.getSettings().setJavaScriptEnabled(true);
        v.clearSslPreferences();
        v.loadUrl("about:blank");
        v.setVisibility(View.VISIBLE);
        v.setWebViewClient(new WebViewClient() {


            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {

                //The SslError object doesn't have public way to get the X509Certificate.
                Bundle bundle = SslCertificate.saveState(error.getCertificate());
                Collection<Certificate> x509Certificates;
                byte[] bytes = bundle.getByteArray("x509-certificate");
                if (bytes == null) {
                    x509Certificates = null;
                } else {
                    try {
                        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
                        x509Certificates = (Collection<Certificate>) certFactory.generateCertificates(new ByteArrayInputStream(bytes));
                    } catch (CertificateException e) {
                        x509Certificates = null;
                    }
                }

                if (x509Certificates != null && !x509Certificates.isEmpty()) {
                    ConfigurationProvider configurationProvider = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider();
                    TrustedCertificateConfigurationTrustManager tm = new TrustedCertificateConfigurationTrustManager(configurationProvider);
                    try {
                        X509Certificate[] chain = new X509Certificate[x509Certificates.size()];
                        x509Certificates.toArray(chain);

                        String authType = chain[0].getSigAlgName();
                        tm.checkServerTrusted(chain, authType);
                        /*
                        for (Certificate c : x509Certificates) {
                            HOSTNAME_VERIFIER.verify(configurationProvider.getTokenHost(), (X509Certificate) c);
                        }
                        */
                        handler.proceed();
                    } catch (Exception e) {
                        showErrorAndExit(e.getMessage());
                        handler.cancel();
                    }
                } else {
                    showErrorAndExit("Certificate is not provided.");
                    handler.cancel();
                }
            }

            private void showErrorAndExit(final String msg) {
                v.clearSslPreferences();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SocialLoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                    }
                });
                sendCancelIntent();
                SocialLoginActivity.this.finish();
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //If the url match with the configured callback url, the redirect is ended and proceed the logon process
                //by sending intent to the MssoService.
                String redirectUri = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
                if (url.startsWith(redirectUri)) {
                    Uri b = Uri.parse(url);
                    //look up for the authorization code from the response parameter.
                    String code = b.getQueryParameter("code");
                    sendCredentialsIntent(new AuthorizationCodeCredentials(code));
                    SocialLoginActivity.this.finish();
                    return true;
                }
                //To proper redirect we have to load the view explicitly, instead of returning false.
                view.loadUrl(url);
                return true;
            }
        });

        //Show progress Bar
        final ProgressBar progressBar;
        progressBar = (ProgressBar) findViewById(ID.PROGRESSBAR.ordinal());

        v.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress) {
                if (progress < 100 && progressBar.getVisibility() == ProgressBar.GONE) {
                    progressBar.setVisibility(ProgressBar.VISIBLE);
                }

                progressBar.setProgress(progress);
                if (progress == 100) {
                    progressBar.setVisibility(ProgressBar.GONE);
                }
            }
        });
        return v;

    }



    private static enum ID {
        NONE,
        WEBVIEW,
        PROGRESSBAR
    }

    private View buildLayout() {
        RelativeLayout rl = new RelativeLayout(this);
        WebView wv = new WebView(this);
        wv.setId(ID.WEBVIEW.ordinal());
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        wv.setLayoutParams(params);
        rl.addView(wv);

        ProgressBar pb = new ProgressBar(this);
        pb.setId(ID.PROGRESSBAR.ordinal());
        params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        pb.setLayoutParams(params);

        rl.addView(pb);

        return rl;


    }

}
