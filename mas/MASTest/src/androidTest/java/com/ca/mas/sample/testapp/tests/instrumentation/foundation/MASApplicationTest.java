/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.foundation;

import android.annotation.TargetApi;
import android.net.http.SslError;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.test.InstrumentationRegistry;
import android.webkit.SslErrorHandler;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.auth.MASApplication;
import com.ca.mas.foundation.auth.MASWebApplication;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;

public class MASApplicationTest extends MASIntegrationBaseTest {

    @Test
    public void enterpriseBrowserTest() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MASApplication.retrieveEnterpriseApps(new MASCallback<List<MASApplication>>() {
            @Override
            public void onSuccess(List<MASApplication> apps) {
                if (!apps.isEmpty() && apps.size() == 3) {
                    result[0] = true;

                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);
        assertTrue(result[0]);

    }

    @Test
    public void testApplicationLauncherWithMASWebApplication() throws Exception {

        final CountDownLatch latch = new CountDownLatch(2);
        final int[] count = {0};
        final boolean[] result = {false};

        MASApplication.setApplicationLauncher(new MASApplication.MASApplicationLauncher() {

            @Override
            public void onNativeAppLaunch(MASApplication application) {
                count[0]++;
            }

            @Override
            public void onWebAppLaunch(MASApplication application) {
                count[0]++;

                WebView webView = new WebView(InstrumentationRegistry.getInstrumentation().getTargetContext());
                new MASWebApplication(webView, application.getAuthUrl()) {
                    @Override
                    protected WebViewClient getWebViewClient() {
                        final WebViewClient webViewClient =  super.getWebViewClient();
                        return new WebViewClient() {

                            @Override
                            public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
                                return webViewClient.shouldInterceptRequest(view, url);
                            }

                            @TargetApi(Build.VERSION_CODES.LOLLIPOP)
                            @Override
                            public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
                                return webViewClient.shouldInterceptRequest(view, request);
                            }

                            @Override
                            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                                webViewClient.onReceivedSslError(view, handler, error);
                            }

                            @Override
                            public void onPageFinished(WebView view, String url) {
                                if (url.endsWith("/connect/enterprise/browser/websso/login")) {
                                    result[0] = true;
                                    latch.countDown();
                                }
                            }
                        };
                    }
                };
            }
        });

        MASApplication.retrieveEnterpriseApps(new MASCallback<List<MASApplication>>() {

            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(List<MASApplication> apps) {

                List<ImageView> imageViews = new ArrayList<ImageView>();

                for (MASApplication a: apps) {
                    ImageView im = new ImageView(InstrumentationRegistry.getInstrumentation().getTargetContext());
                    imageViews.add(im);
                    a.renderEnterpriseIcon(im);
                }

                for (ImageView imageView: imageViews) {
                    imageView.callOnClick();
                }
                latch.countDown();

            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });


        await(latch);
        //Assume 3 applications and one is Web Application
        assertTrue(count[0] == 3);
        assertTrue(result[0]);
    }
}
