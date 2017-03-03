/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.store.PrivateTokenStorage;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASTest extends MASLoginTestBase {

    @Test
    public void testAccessProtectedEndpoint() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertNotNull(callback.get().getBody().getContent());
    }

    @Test
    public void testAccessProtectedEndpointCancelOnExecutingRequest() throws URISyntaxException, InterruptedException, IOException {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/slow"))
                .notifyOnCancel()
                .build();
        final String KEY = "key";
        final String VALUE = "This is a test";
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        long requestId = MAS.invoke(request, callback);
        Thread.sleep(100); //Let the engine put the message in the queue
        Bundle data = new Bundle();
        data.putString(KEY, VALUE);
        MAS.cancelRequest(requestId, data);

        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            MAS.RequestCancelledException exception = (MAS.RequestCancelledException) e.getCause().getCause();
            assertEquals(exception.getData().get(KEY), VALUE);
        }
    }

    @Test
    public void testAccessProtectedEndpointCancelAllOnExecutingRequest() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/slow"))
                .notifyOnCancel()
                .build();
        final String KEY = "key";
        final String VALUE = "This is a test";
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        Thread.sleep(100); //Let the engine put the message in the queue
        Bundle data = new Bundle();
        data.putString(KEY, VALUE);
        MAS.cancelAllRequest(data);

        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            MAS.RequestCancelledException exception = (MAS.RequestCancelledException) e.getCause().getCause();
            assertEquals(exception.getData().get(KEY), VALUE);
        }
    }

    @Test
    public void testAccessProtectedEndpointWithOverrideScope() throws URISyntaxException, InterruptedException, IOException, ExecutionException {

        final String SCOPE = "read write";

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts"))
                .scope(SCOPE)
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());

        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        RecordedRequest accessTokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
        assertTrue(s.contains("scope=" + URLEncoder.encode(SCOPE, "utf-8")));
    }

    @Test
    public void getAccessTokenUsingRefreshToken() throws InterruptedException, JSONException, URISyntaxException, ExecutionException {

        final String NEW_ACCESS_TOKEN = "new_access_token";

        final boolean[] newToken = {false};

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                if (newToken[0]) {
                    String token = "{\n" +
                            "  \"access_token\":\"" + NEW_ACCESS_TOKEN + "\",\n" +
                            "  \"token_type\":\"Bearer\",\n" +
                            "  \"expires_in\":" + new Date().getTime() + 3600 + ",\n" +
                            "  \"refresh_token\":\"19785fca-4b86-4f8e-a73c-7de1d420f88d\",\n" +
                            "  \"scope\":\"openid msso phone profile address email\"\n" +
                            "}";
                    return new MockResponse().setResponseCode(200).setBody(token);
                } else {
                    return super.retrieveTokenResponse();
                }
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts"))
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());

        newToken[0] = true;

        //Remove Access Token
        DataSource<String, String> dataSource = DataSourceFactory.getStorage(InstrumentationRegistry.getTargetContext(),
                KeystoreDataSource.class, null, null);
        for (String k : dataSource.getKeys(null)) {
            if (k.contains(PrivateTokenStorage.KEY.PREF_ACCESS_TOKEN.name())) {
                dataSource.remove(k);
            }
        }

        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request, callback2);
        assertNotNull(callback2.get());

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        assertTrue(rr.getHeader("Authorization").contains(NEW_ACCESS_TOKEN));

    }

    @Test
    public void testAccessProtectedEndpointWith204Response() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/testNoContent")).build();
        MASCallbackFuture<MASResponse<byte[]>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, callback.get().getResponseCode());
        assertEquals(0, (callback.get().getBody().getContent()).length);
        assertNotNull(callback.get().getBody().getContent());
    }

    @Test
    public void appEndpointError() throws URISyntaxException, InterruptedException {
        final String expectedErrorMessage = "{\"error\":\"This is App Error\"}";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString())
                        .setBody(expectedErrorMessage);
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            TargetApiException error = (TargetApiException) e.getCause().getCause();
            assertTrue(((MASException)e.getCause()).getRootCause() instanceof TargetApiException );
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, error.getResponse().getResponseCode());
            assertEquals(expectedErrorMessage, (error.getResponse().getBody().getContent().toString()));
        }
    }

    @Test
    public void testOverrideResponseWithJSONArray() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/productsAsArray"))
                .responseBody(new JSONArrayResponse())
                .build();
        MASCallbackFuture<MASResponse<JSONArray>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        JSONArray array = callback.get().getBody().getContent();
        assertNotNull(array);
    }

    private static class JSONArrayResponse extends MASResponseBody<JSONArray> {

        @Override
        public JSONArray getContent() {
            try {
                return new JSONArray(new String(getRawContent()));
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }
    }
}
