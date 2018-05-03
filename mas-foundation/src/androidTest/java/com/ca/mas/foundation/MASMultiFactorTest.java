/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.conf.ConfigurationManager;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class MASMultiFactorTest extends MASLoginTestBase {

    private static final String FIRST_FACTOR_VALUE = "multi-factor-value";
    private static final String SECOND_FACTOR_VALUE = "multi-factor-value2";
    private static final String FIRST_FACTOR_ERROR = "x-Dummy-err";
    private static final String SECOND_FACTOR_ERROR = "x-Dummy2-err";


    private MASMultiFactorAuthenticator authenticator;

    @Before
    public void setupMultiFactorAuthenticator() {

        authenticator = new DummyMultiFactorAuthenticator() {
            @Override
            protected void onMultiFactorAuthenticationRequest(Context context, MASRequest originalRequest, MASMultiFactorHandler handler) {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(FIRST_FACTOR_VALUE, "1234");
                handler.proceed(getContext(), headers);
            }
        };

        MAS.registerMultiFactorAuthenticator(authenticator);
    }

    @Test
    public void testFirstFactorTriggered() throws URISyntaxException, ExecutionException, InterruptedException {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.MULTIFACTOR_ENDPOINT)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>(new Handler(getContext().getMainLooper()));
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertNotNull(callback.get().getBody().getContent());
        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.MULTIFACTOR_ENDPOINT);
        assertEquals("1234", rr.getHeader(FIRST_FACTOR_VALUE));
    }

    @Test
    public void testSecondFactorTriggered() throws URISyntaxException, ExecutionException, InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse multiFactor(RecordedRequest request) {

                if (request.getHeader(SECOND_FACTOR_VALUE) != null) {
                    return new MockResponse().setResponseCode(200).setBody("{\"test\":\"test value\"}");
                }
                if (request.getHeader(FIRST_FACTOR_VALUE) == null) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST).addHeader(FIRST_FACTOR_ERROR, "1234");
                }
                if (request.getHeader(SECOND_FACTOR_VALUE) == null) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST).addHeader(SECOND_FACTOR_ERROR, "3456");
                }

                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);

            }
        });

        MASMultiFactorAuthenticator authenticator2 = new MASMultiFactorAuthenticator<DummyMultiFactorHandler>() {
            @Override
            public DummyMultiFactorHandler getMultiFactorHandler(long requestId, MASRequest request, MASResponse response) {
                int statusCode = response.getResponseCode();
                if (statusCode == java.net.HttpURLConnection.HTTP_BAD_REQUEST) {
                    return new DummyMultiFactorHandler(requestId);
                }
                return null;
            }

            @Override
            protected void onMultiFactorAuthenticationRequest(Context context, MASRequest originalRequest, DummyMultiFactorHandler handler) {
                handler.proceed(getContext(), "3456");
            }
        };

        MAS.registerMultiFactorAuthenticator(authenticator2);

        try {
            MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.MULTIFACTOR_ENDPOINT)).build();
            MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>(new Handler(getContext().getMainLooper()));
            MAS.invoke(request, callback);
            assertNotNull(callback.get());
            assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
            assertNotNull(callback.get().getBody().getContent());
            RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.MULTIFACTOR_ENDPOINT);
            assertEquals("3456", rr.getHeader(SECOND_FACTOR_VALUE));
        } finally {
            ConfigurationManager.getInstance().unregisterResponseInterceptor(authenticator2);
        }
    }

    @Test
    public void testCancelWithData() throws URISyntaxException, InterruptedException {
        ConfigurationManager.getInstance().unregisterResponseInterceptor(authenticator);
        MAS.registerMultiFactorAuthenticator(new DummyMultiFactorAuthenticator() {

            @Override
            protected void onMultiFactorAuthenticationRequest(Context context, MASRequest originalRequest, MASMultiFactorHandler handler) {
                Bundle data = new Bundle();
                data.putString("TEST", "VALUE");
                handler.cancel(data);
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.MULTIFACTOR_ENDPOINT))
                .notifyOnCancel()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>(new Handler(getContext().getMainLooper()));
        MAS.invoke(request, callback);
        try {
            callback.get();
        } catch (ExecutionException e) {
            Assert.assertTrue(e.getCause().getCause() instanceof MAS.RequestCancelledException);
            Assert.assertEquals("VALUE", ((MAS.RequestCancelledException) (e.getCause().getCause())).getData().get("TEST"));
        }

    }

    @After
    public void tearDownMultiFactorAuthenticator() {
        ConfigurationManager.getInstance().getResponseInterceptors().clear();
    }


    private abstract static class DummyMultiFactorAuthenticator extends MASMultiFactorAuthenticator<MASMultiFactorHandler> {

        @Override
        public MASMultiFactorHandler getMultiFactorHandler(long requestId, MASRequest request, MASResponse response) {
            int statusCode = response.getResponseCode();
            if (statusCode == HttpURLConnection.HTTP_BAD_REQUEST) {
                List<String> errors = (List<String>) response.getHeaders().get(FIRST_FACTOR_ERROR);
                if (errors != null) {
                    return new MASMultiFactorHandler(requestId);
                }
            }
            return null;
        }

    }

    private static class DummyMultiFactorHandler extends MASMultiFactorHandler {

        public DummyMultiFactorHandler(long requestId) {
            super(requestId);
        }

        public void proceed(Context context, String anotherMultiFactor) {
            Map<String, String> additionalHeaders = new HashMap<>();
            additionalHeaders.put(FIRST_FACTOR_VALUE, "1234");
            additionalHeaders.put(SECOND_FACTOR_VALUE, anotherMultiFactor);
            super.proceed(context, additionalHeaders);
        }
    }
}
