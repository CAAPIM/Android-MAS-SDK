/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.annotation.TargetApi;
import android.net.http.SslError;
import android.os.Build;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;

import java.io.ByteArrayInputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * The SDK uses {@link WebView} to display a Web browser application.
 * The {@link MASWebApplication} intercept the browser request to the gateway and perform the authentication process.
 * Once authentication is completed, the {@link WebView} renders the result from the Web Application.
 */
public class MASWebApplication {

    private String authUrl;

    public MASWebApplication(final WebView webView, String authUrl) {
        this.authUrl = authUrl;

        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(getWebViewClient());

        try {
            if (!(new URL(authUrl)).getHost().equals(MASConfiguration.getCurrentConfiguration().getGatewayHostName())) {
                throw new IllegalArgumentException("This auth url is valid only for the host that has issued the access_token");
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }

        webView.loadUrl(authUrl);
    }

    /**
     * @return The Request Timeout value in milliseconds. Default to 5000
     */
    protected int getRequestTimeout() {
        return 5000;
    }

    /**
     * The SDK uses WebView to display a Web browser application, it registers a {@link WebViewClient#shouldInterceptRequest(WebView, WebResourceRequest)}
     * to the WebView to intercept request.
     * By default the SDK invoke {@link SslErrorHandler#proceed()} when receiving
     * {@link WebViewClient#onReceivedSslError(WebView, SslErrorHandler, SslError)},
     * Developer may override the default behaviour for SSL challenge.
     */
    protected WebViewClient getWebViewClient() {

        return new WebViewClient() {

            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
            @Override
            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                return this.shouldInterceptRequest(view, request.getUrl().toString());
            }

            //Keep this to support version < Build.VERSION_CODES.LOLLIPOP
            @Override
            public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {

                if (url.equals(authUrl)) {

                    final CountDownLatch latch = new CountDownLatch(1);
                    final WebResourceResponse webResourceResponse = new WebResourceResponse(null, null, null);
                    MASRequest request = null;
                    try {
                        request = new MASRequest.MASRequestBuilder(new URL(url)).build();
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }

                    MAS.invoke(request, new MASCallback<MASResponse<byte[]>>() {

                        @Override
                        public void onSuccess(MASResponse<byte[]> result) {
                            try {
                                webResourceResponse.setData(new ByteArrayInputStream(result.getBody().getContent()));
                                webResourceResponse.setEncoding("UTF-8");
                                webResourceResponse.setMimeType("text/html");
                            } catch (Exception e) {
                                webResourceResponse.setData(new ByteArrayInputStream(e.getMessage().getBytes()));
                                webResourceResponse.setEncoding("UTF-8");
                                webResourceResponse.setMimeType("plain/text");
                            }
                            latch.countDown();
                        }

                        @Override
                        public void onError(Throwable e) {
                            latch.countDown();
                        }
                    });

                    try {
                        latch.await(getRequestTimeout(), TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        webResourceResponse.setData(new ByteArrayInputStream("No response from Server".getBytes()));
                        webResourceResponse.setEncoding("UTF-8");
                        webResourceResponse.setMimeType("plain/text");
                    }
                    return webResourceResponse;
                } else {
                    //Not intercept the request.
                    return null;
                }
            }
        };
    }
}
