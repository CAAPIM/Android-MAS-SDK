/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.net.Uri;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.util.Pair;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.store.PrivateTokenStorage;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASTest extends MASLoginTestBase {

    @Test
    public void testAccessProtectedEndpoint() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertNotNull(callback.get().getBody().getContent());
        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        assertNotNull(rr.getHeader("Authorization"));
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
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof TargetApiException);
            assertEquals(HttpURLConnection.HTTP_FORBIDDEN, error.getResponse().getResponseCode());
            assertEquals(expectedErrorMessage, (error.getResponse().getBody().getContent().toString()));
        }
    }

    @Test
    public void testOverrideResponseWithJSONArray() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS_AS_ARRAY))
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

    private static final String RESPONSE_DATA = "Expected Response Data";

    @Test
    public void testInvalidUrl() throws InterruptedException, ExecutionException {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).
                        setBody(RESPONSE_DATA);
            }
        });

        Uri uri = new Uri.Builder().
                appendPath("other").build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri).build();
        MASCallbackFuture<MASResponse<String>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertEquals(RESPONSE_DATA,
                    new String(((TargetApiException) (e.getCause()).getCause()).getResponse().getBody().getRawContent()));
            assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR,
                    ((TargetApiException) (e.getCause()).getCause()).getResponse().getResponseCode());

        }
    }

    private static final String RESPONSE_HEADER_NAME = "headerName";
    private static final String RESPONSE_HEADER_VALUE = "headerValue";
    private static final String QUERY_PARAMETER_NAME = "queryName";
    private static final String QUERY_PARAMETER_VALUE = "queryValue";
    public static final String HTTP_TEST = "httptest";

    @Test
    public void testHttpGet() throws Exception {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.TEXT_PLAIN).
                        setBody(RESPONSE_DATA);
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri).build();
        MASCallbackFuture<MASResponse<String>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(RESPONSE_DATA, callback.get().getBody().getContent());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        RecordedRequest recordedRequest = getRecordRequest(uri.getPath());
        assertEquals(request.getMethod(), recordedRequest.getMethod());
    }

    @Test
    public void testHttpDelete() throws Exception {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.TEXT_PLAIN).
                        setBody(RESPONSE_DATA);
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri).delete(null).build();
        MASCallbackFuture<MASResponse<String>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(RESPONSE_DATA, callback.get().getBody().getContent());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        RecordedRequest recordedRequest = getRecordRequest(uri.getPath());
        assertEquals(request.getMethod(), recordedRequest.getMethod());

    }


    @Test
    public void testHttpPost() throws Exception {

        String requestData = "Expected Request Data";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.TEXT_PLAIN).
                        setHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE).
                        setBody(RESPONSE_DATA);
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .post(MASRequestBody.stringBody(requestData))
                .header(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE)
                .build();

        MASCallbackFuture<MASResponse<String>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(RESPONSE_DATA, callback.get().getBody().getContent());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertNotNull(callback.get().getHeaders().get(RESPONSE_HEADER_NAME));

        RecordedRequest recordedRequest = getRecordRequest(uri.getPath());
        assertEquals(request.getMethod(), recordedRequest.getMethod());

        assertEquals(requestData, new String(recordedRequest.getBody().readUtf8()));
        assertEquals(RESPONSE_HEADER_VALUE, recordedRequest.getHeader(RESPONSE_HEADER_NAME));
    }

    @Test
    public void testHttpPostWithJson() throws Exception {

        final JSONObject requestData = new JSONObject();
        requestData.put("jsonName", "jsonValue");
        requestData.put("jsonName2", 1234);

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.APPLICATION_JSON).
                        setHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE).
                        setBody(requestData.toString());
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .post(MASRequestBody.jsonBody(requestData))
                .header(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE)
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(requestData.toString(), callback.get().getBody().getContent().toString());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertNotNull(callback.get().getHeaders().get(RESPONSE_HEADER_NAME));

        RecordedRequest recordedRequest = getRecordRequest(uri.getPath());
        assertEquals(request.getMethod(), recordedRequest.getMethod());

        assertEquals(requestData.toString(), new String(recordedRequest.getBody().readUtf8()));
        assertEquals(RESPONSE_HEADER_VALUE, recordedRequest.getHeader(RESPONSE_HEADER_NAME));
        assertTrue(uri.toString().endsWith(recordedRequest.getPath()));

    }

    @Test
    public void testHttpPostWithUrlEncodedForm() throws Exception {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK);
            }
        });

        List<Pair<String, String>> form = new ArrayList<>();
        form.add(new Pair<String, String>("formfield1", "field1Value"));
        form.add(new Pair<String, String>("formfield2", "field2Value"));
        form.add(new Pair<String, String>("formfield3", "field3Value"));

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .post(MASRequestBody.urlEncodedFormBody(form))
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        RecordedRequest recordedRequest = getRecordRequest(uri.toString());
        String s = recordedRequest.getBody().readUtf8();
        assertEquals("formfield1=field1Value&formfield2=field2Value&formfield3=field3Value", s);
        assertEquals(request.getMethod(), recordedRequest.getMethod());
        assertTrue(uri.toString().endsWith(recordedRequest.getPath()));

    }

    @Test
    public void testHttpPut() throws Exception {

        String requestData = "Expected Request Data";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.TEXT_PLAIN).
                        setHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE).
                        setBody(RESPONSE_DATA);
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .put(MASRequestBody.stringBody(requestData))
                .header(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE)
                .build();

        MASCallbackFuture<MASResponse<String>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(RESPONSE_DATA, callback.get().getBody().getContent());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertNotNull(callback.get().getHeaders().get(RESPONSE_HEADER_NAME));

        RecordedRequest recordedRequest = getRecordRequest(uri.getPath());
        assertEquals(request.getMethod(), recordedRequest.getMethod());

        assertEquals(requestData, new String(recordedRequest.getBody().readUtf8()));
        assertEquals(RESPONSE_HEADER_VALUE, recordedRequest.getHeader(RESPONSE_HEADER_NAME));
    }

    @Test
    public void testAccessUnProtectedEndpoint() throws URISyntaxException, InterruptedException, IOException, ExecutionException {

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts"))
                .setPublic()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        assertNull(rr.getHeader("Authorization"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAccessUnProtectedEndpointInOtherServer() throws Throwable {

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("https://somewhere/protected/resource/products?operation=listProducts"))
                .setPublic()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
        } catch (ExecutionException e) {
            throw e.getCause().getCause();
        }

    }

    @Test
    public void testMagIdentifier() throws Exception {


        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());

        RecordedRequest tokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        assertNotNull(tokenRequest.getHeader(ServerClient.MAG_IDENTIFIER));
        RecordedRequest apiRequest = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        assertNotNull(apiRequest.getHeader(ServerClient.MAG_IDENTIFIER));

        MASCallbackFuture<Void> deRegisterCallback = new MASCallbackFuture<>();
        MASDevice.getCurrentDevice().deregister(deRegisterCallback);
        deRegisterCallback.get();

        RecordedRequest deRegisterRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REMOVE);
        assertNotNull(deRegisterRequest.getHeader(ServerClient.MAG_IDENTIFIER));

    }

    @Test
    public void testConnectionListener() throws Exception {
        final boolean[] onObtained = {false};
        final boolean[] onConnected = {false};
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .connectionListener(new MASConnectionListener() {
                    @Override
                    public void onObtained(HttpURLConnection connection) {
                        onObtained[0] = true;
                    }

                    @Override
                    public void onConnected(HttpURLConnection connection) {
                        onConnected[0] = true;
                    }
                })
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertTrue(onObtained[0]);
        assertTrue(onConnected[0]);
    }

    @Test
    public void testStop() throws Exception {
        MAS.stop();
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();

        try {
            MAS.invoke(request, callback);
            Assert.fail();
        } catch (Exception e) {
            Assert.assertEquals(MASConstants.MAS_STATE_STOPPED, MAS.getState(getContext()));
            MAS.start(getContext());
        }
    }
}
