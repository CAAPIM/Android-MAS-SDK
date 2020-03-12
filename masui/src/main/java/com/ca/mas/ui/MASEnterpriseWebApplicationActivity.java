/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.ui;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.ca.mas.foundation.auth.MASWebApplication;

import static android.content.DialogInterface.BUTTON_NEGATIVE;
import static android.content.DialogInterface.BUTTON_POSITIVE;

/**
 * Sample to show an Enterprise Web Application in an Activity.
 */
public class MASEnterpriseWebApplicationActivity extends AppCompatActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mas_web_application);
        String url = getIntent().getExtras().getString(MASEnterpriseBrowserFragment.AUTH_URL);
        if (url != null) {
            WebView webView = (WebView) findViewById(R.id.webView);

            //Show progress Bar
            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

            webView.setWebChromeClient(new WebChromeClient() {
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
            //Render the Web application in the WebView

            new MASWebApplication(webView, url) {
                @Override
                protected WebViewClient getWebViewClient() {
                    final WebViewClient webViewClient = super.getWebViewClient();

                    return new WebViewClient() {
                        @Override
                        public void onReceivedSslError(WebView view, final SslErrorHandler handler, SslError error) {

                            AlertDialog.Builder builder = new AlertDialog.Builder(MASEnterpriseWebApplicationActivity.this);
                            AlertDialog ad = builder.create();
                            String message = null;
                            switch (error.getPrimaryError()) {
                                case SslError.SSL_UNTRUSTED:
                                    message = "Certificate is untrusted.";
                                    break;
                                case SslError.SSL_EXPIRED:
                                    message = "Certificate has expired.";
                                    break;
                                case SslError.SSL_IDMISMATCH:
                                    message = "Certificate ID is mismatched.";
                                    break;
                                case SslError.SSL_NOTYETVALID:
                                    message = "Certificate is not yet valid.";
                                    break;
                                case SslError.SSL_DATE_INVALID:
                                    message = "Certificate date is invalid .";
                                    break;
                                default:
                                    message = "Certificate is invalid .";
                                    break;
                            }
                            message += " Do you want to continue anyway?";
                            ad.setTitle("SSL Certificate Error");
                            ad.setMessage(message);
                            ad.setButton(BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    handler.proceed();
                                }
                            });
                            ad.setButton(BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    handler.cancel();
                                    MASEnterpriseWebApplicationActivity.this.finish();
                                }
                            });
                            ad.show();
                        }

                        @SuppressWarnings("deprecation")
                        @Override
                        public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                            return webViewClient.shouldInterceptRequest(view, url);
                        }

                        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                        @Override
                        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                            return webViewClient.shouldInterceptRequest(view, request);
                        }
                    };
                }
            };
        }
    }
}
