/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslCertificate;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebViewDatabase;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.creds.AuthorizationCodeCredentials;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.io.http.TrustedCertificateConfigurationTrustManager;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.service.MssoService;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;

import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * The SDK uses {@link WebView} to display social login web interface.
 * The {@link MASSocialLogin} intercept the browser request to the gateway and perform the authentication process.
 * Once authentication is completed, {@link MASSocialLogin#onAuthCodeReceived(String)} will be triggered.
 */
public abstract class MASSocialLogin {

    public MASSocialLogin(final Context context, final WebView webView, final long requestId, MASAuthenticationProvider provider) {
        CookieSyncManager.createInstance(context);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        WebViewDatabase.getInstance(context).clearFormData();
        WebViewDatabase.getInstance(context).clearHttpAuthUsernamePassword();
        //We have to remove the username and password for APL level < 18
        WebViewDatabase.getInstance(context).clearUsernamePassword();

        webView.clearCache(true);
        webView.clearFormData();
        webView.getSettings().setSaveFormData(false);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.clearSslPreferences();
        webView.loadUrl("about:blank");
        webView.setVisibility(View.VISIBLE);
        webView.setWebViewClient(new WebViewClient() {


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
                        onError(e.getMessage(), e);
                        handler.cancel();
                    }
                } else {
                    onError("Certificate is not provided.", null);
                    handler.cancel();
                }
            }

            private void onError(final String msg, Exception e) {
                webView.clearSslPreferences();
                MAS.cancelRequest(requestId);
                MASSocialLogin.this.onError(msg, e);
            }

            @TargetApi(Build.VERSION_CODES.N)
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                final Uri uri = request.getUrl();
                return shouldOverrideUrlLoading(view, uri);
            }

            //For Android M and below
            @SuppressWarnings("deprecation")
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return shouldOverrideUrlLoading(view, Uri.parse(url));
            }

            private boolean shouldOverrideUrlLoading(WebView view, Uri url) {
                //If the url match with the configured callback url, the redirect is ended and proceed the logon process
                //by sending intent to the MssoService.
                String redirectUri = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
                if (url.toString().startsWith(redirectUri)) {
                    //look up for the authorization code from the response parameter.
                    String code = url.getQueryParameter("code");
                    Intent intent = new Intent(MssoIntents.ACTION_CREDENTIALS_OBTAINED, null, context, MssoService.class);
                    intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
                    intent.putExtra(MssoIntents.EXTRA_CREDENTIALS, new AuthorizationCodeCredentials(code));
                    context.startService(intent);
                    onAuthCodeReceived(code);
                    return true;
                }
                //To proper redirect we have to load the view explicitly, instead of returning false.
                view.loadUrl(url.toString());
                return true;
            }
        });

        webView.loadUrl(provider.getAuthenticationUrl());

    }

    protected abstract void onError(String msg, Exception e);

    /**
     * Notify the Authenticate Renderer that Authorization code has been retrieved.
     *
     * @param code Authorization Code
     */
    protected abstract void onAuthCodeReceived(String code);

    private static class MagTask extends AsyncTask<Void, Void, MAGResponse> {
        Context context;
        MAGRequest request;
        MASCallback<Uri> callback;

        MagTask(Context context, MAGRequest request, MASCallback<Uri> callback) {
            this.context = context;
            this.request = request;
            this.callback = callback;
        }

        @Override
        protected MAGResponse<JSONObject> doInBackground(Void... params) {
            MAGHttpClient magHttpClient = new MAGHttpClient(context) {
                @Override
                protected void onConnectionObtained(HttpURLConnection connection) {
                    connection.setInstanceFollowRedirects(false);
                }
            };
            try {
                return magHttpClient.execute(request);
            } catch (IOException e) {
                Log.d("", e.getMessage());
            }

            return null;
        }

        @Override
        protected void onPostExecute(MAGResponse magResponse) {
            super.onPostExecute(magResponse);
            String location = null;
            Map<String, List<String>> headers = magResponse.getHeaders();
            location = headers.get("Location").get(0);
            callback.onSuccess(Uri.parse(location));
        }
    }

    public static void getAuthConfiguration(Context context, MASAuthenticationProvider provider, MASCallback<Uri> callback) {
        MAGRequest request = new MAGRequest.MAGRequestBuilder(Uri.parse(provider.getAuthenticationUrl()))
                .get()
                .responseBody(MAGResponseBody.jsonBody())
                .build();

        MagTask magTask = new MagTask(context, request, callback);
        magTask.execute();
    }
}
