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
import android.os.Handler;
import android.util.Pair;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.DeviceIdentifier;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.io.Charsets;
import com.ca.mas.core.io.IoUtils;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.core.security.KeyStoreException;
import com.ca.mas.core.store.PrivateTokenStorage;
import com.ca.mas.core.store.StorageProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static com.ca.mas.core.client.ServerClient.SCOPE;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
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
    public void testAccessProtectedEndpointRunOnMainThread() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>(new Handler(getContext().getMainLooper()));
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertNotNull(callback.get().getBody().getContent());
    }

    @Test
    public void testAccessProtectedEndpointWithURL() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URL(
                MASConfiguration.getCurrentConfiguration().getGatewayUrl() +
                        GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
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
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_SLOW))
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
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_SLOW))
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
    public void testAccessProtectedEndpointWithInvalidScope() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        final int expectedErrorCode = 3003115;
        final String expectedErrorMessage = "{\n" +
                "\"error\":\"invalid_scope\",\n" +
                "\"error_description\":\"No registered scope value for this client has been requested\"\n" +
                "}";
        final String SCOPE = "invalid";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                String resp = expectedErrorMessage;
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .setHeader("x-ca-err", expectedErrorCode).setBody(resp);
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts"))
                .scope(SCOPE)
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof OAuthServerException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof OAuthServerException);
            assertEquals(expectedErrorCode, ((OAuthServerException) ((MASException) e.getCause()).getRootCause()).getErrorCode());
            assertEquals(expectedErrorMessage, (((MASException) e.getCause()).getRootCause()).getMessage());
        }

        RecordedRequest accessTokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
        assertTrue(s.contains("scope=" + URLEncoder.encode(SCOPE, "utf-8")));
    }

    @Test
    public void testAccessProtectedEndpointWithIDToken() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                String token = "{\n" +
                        "  \"id_token\":\"myIDToken\",\n" +
                        "  \"id_token_type\":\"myIDTokenType\",\n" +
                        "  \"access_token\":\"caa5871c-7c0f-44c7-b03b-1783609170e4\",\n" +
                        "  \"token_type\":\"Bearer\",\n" +
                        "  \"expires_in\":" + 3600 + ",\n" +
                        "  \"refresh_token\":\"19785fca-4b86-4f8e-a73c-7de1d420f88d\",\n" +
                        "  \"scope\":\"openid msso phone profile address email msso_register msso_client_register mas_messaging mas_storage mas_identity mas_identity_retrieve_users mas_identity_create_users mas_identity_update_users mas_identity_delete_users mas_identity_retrieve_groups mas_identity_create_groups mas_identity_update_groups mas_identity_delete_groups\"\n" +
                        "}";
                return new MockResponse().setResponseCode(200).setBody(token);
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts"))
                .scope(SCOPE)
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        callback.get();
        //Make sure it persist the id_token using jwt-bearer flow
        assertEquals("myIDToken", StorageProvider.getInstance().getTokenManager().getIdToken().getValue());
        assertEquals("myIDTokenType", StorageProvider.getInstance().getTokenManager().getIdToken().getType());
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
        DataSource<String, String> dataSource = getValue(StorageProvider.getInstance().getOAuthTokenContainer(), "storage", DataSource.class);
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
    public void appEndpointErrorOnMainThread() throws URISyntaxException, InterruptedException {
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
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>(new Handler(getContext().getMainLooper()));
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
                .build();

        MASCallbackFuture<MASResponse<JSONArray>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        JSONArray array = callback.get().getBody().getContent();
        assertNotNull(array);
    }

    @Test
    public void testOverrideResponseWithProduct() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .responseBody(new ProductResponseBody())
                .build();

        MASCallbackFuture<MASResponse<Product>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        Product products = callback.get().getBody().getContent();
        assertNotNull(products);
    }

    @Test
    public void testOverrideResponseWithProductList() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .responseBody(new ListProductResponseBody())
                .build();

        MASCallbackFuture<MASResponse<List<Product>>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        List<Product> productList = callback.get().getBody().getContent();
        assertNotNull(productList);
        assertEquals(productList.size(), 2);
    }

    public class Product {

        private JSONArray products;

        public Product(String source) {
            try {
                JSONObject job = new JSONObject(source);
                this.products = job.optJSONArray("products");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        public JSONArray getProducts() {
            return products;
        }
    }

    class ProductResponseBody extends MASResponseBody<Product> {

        @Override
        public Product getContent() {
            String source = new String(getRawContent());
            Product staff = new Product(source);
            return staff;
        }
    }

    class ListProductResponseBody extends MASResponseBody<List<Product>> {
        @Override
        public List<Product> getContent() {

            String source = new String(getRawContent());
            Product staff = new Product(source);
            List<Product> productList = new ArrayList<Product>();
            productList.add(staff);
            productList.add(staff);
            return productList;
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
    public void testHttpOverrideCharset() throws Exception {
        String requestData = "Expected Request Data";
        final ContentType customCharset = new ContentType("application/x-www-form-urlencoded", Charsets.UTF8);

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-Type", customCharset.toString()).
                        setHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE).
                        setBody(RESPONSE_DATA);
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .post(MASRequestBody.stringBody(requestData))
                .header("Content-Type", customCharset.toString())
                .header(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE)
                .build();

        MASCallbackFuture<MASResponse<String>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        String header = request.getHeaders().get("Content-Type").get(0);
        assertEquals(header, callback.get().getBody().getContentType());
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
                .removeHeader("test")
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        JSONAssert.assertEquals(requestData, callback.get().getBody().getContent(), false);
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertNotNull(callback.get().getHeaders().get(RESPONSE_HEADER_NAME));

        RecordedRequest recordedRequest = getRecordRequest(uri.getPath());
        assertEquals(request.getMethod(), recordedRequest.getMethod());

        assertEquals(requestData.toString(), new String(recordedRequest.getBody().readUtf8()));
        assertEquals(RESPONSE_HEADER_VALUE, recordedRequest.getHeader(RESPONSE_HEADER_NAME));
        assertTrue(uri.toString().endsWith(recordedRequest.getPath()));
    }

    @Test
    public void testExplicitJSONResponseType() throws Exception {
        final JSONObject responseData = new JSONObject();
        responseData.put("jsonName", "jsonValue");
        responseData.put("jsonName2", 1234);

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.APPLICATION_JSON).
                        setBody(responseData.toString());
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .responseBody(MASResponseBody.jsonBody())
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        JSONAssert.assertEquals(responseData, callback.get().getBody().getContent(), false);
    }

    @Test
    public void testExplicitStringResponseType() throws Exception {
        final String responseData = "test";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.TEXT_PLAIN).
                        setBody(responseData);
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .responseBody(MASResponseBody.stringBody())
                .build();

        MASCallbackFuture<MASResponse<String>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(responseData, callback.get().getBody().getContent());
    }

    @Test
    public void testExplicitByteResponseType() throws Exception {
        final String responseData = "test";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.APPLICATION_OCTET_STREAM).
                        setBody(responseData);
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .responseBody(MASResponseBody.byteArrayBody())
                .build();

        MASCallbackFuture<MASResponse<byte[]>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(responseData, new String(callback.get().getBody().getContent()));
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
    public void testAccessUnprotectedEndpoint() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
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

    @Test(expected = IllegalArgumentException.class)
    @SuppressWarnings("ResourceType")
    public void testInvalidGrantFlow() throws Exception {
        MAS.setGrantFlow(4);
    }

    @Test
    public void testGatewayIsReachable() throws Exception {
        MASCallbackFuture<Boolean> callback = new MASCallbackFuture<>();
        MAS.gatewayIsReachable(callback);
        assertTrue(callback.get());
    }

    @Test
    public void testStop() throws Exception {
        MAS.stop();
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();

        try {
            MAS.invoke(request, callback);
            fail();
        } catch (Exception e) {
            assertEquals(MASConstants.MAS_STATE_STOPPED, MAS.getState(getContext()));
            assertNull(MAS.getContext());
            MAS.start(getContext());
        }
    }

    @Test
    public void testMultiThreadRequest() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_SLOW))
                .build();

        long start = System.currentTimeMillis();
        int noOfRequest = Runtime.getRuntime().availableProcessors() * 2 + 1;
        final CountDownLatch countDownLatch = new CountDownLatch(noOfRequest);
        for (int i = 0; i < noOfRequest; i++) {
            MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    countDownLatch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();
        long end = System.currentTimeMillis();

        BigDecimal duration = new BigDecimal(end - start).divide(new BigDecimal(1000));

        //Without Multi-thread, all requests should completed more than 10 seconds.
        //assertTrue(duration.compareTo(new BigDecimal(10)) == 1);

        //With Multi-thread, all requests should completed within 1-3 seconds.
        assertTrue((duration.compareTo(new BigDecimal(3)) == -1));

    }

    @Test
    public void testLoading() throws Exception {

        final int[] failed = {0};

        int noOfRequest = 150;
        final CountDownLatch countDownLatch = new CountDownLatch(noOfRequest);
        for (int i = 0; i < noOfRequest; i++) {
            MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_SLOW))
                    .build();

            MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    countDownLatch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    failed[0]++;
                    assertTrue(e.getCause() instanceof RejectedExecutionException);
                    countDownLatch.countDown();
                }
            });
        }
        countDownLatch.await();

        assertTrue(failed[0] > 0);
    }

    @Test
    public void testMultiThreadRequestWithResponse() throws Exception {
        Uri.Builder builder1 = new Uri.Builder();
        builder1.encodedPath(GatewayDefaultDispatcher.ECHO);
        builder1.appendQueryParameter("name", "name1");
        MASRequest request1 = new MASRequest.MASRequestBuilder(builder1.build()).build();
        MASCallbackFuture<MASResponse<String>> callback1 = new MASCallbackFuture<>();
        MAS.invoke(request1, callback1);

        Uri.Builder builder2 = new Uri.Builder();
        builder2.encodedPath(GatewayDefaultDispatcher.ECHO);
        builder2.appendQueryParameter("name", "name2");
        MASRequest request2 = new MASRequest.MASRequestBuilder(builder2.build()).build();
        MASCallbackFuture<MASResponse<String>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);

        Uri.Builder builder3 = new Uri.Builder();
        builder3.encodedPath(GatewayDefaultDispatcher.ECHO);
        builder3.appendQueryParameter("name", "name3");
        MASRequest request3 = new MASRequest.MASRequestBuilder(builder3.build()).build();
        MASCallbackFuture<MASResponse<String>> callback3 = new MASCallbackFuture<>();
        MAS.invoke(request3, callback3);

        Uri.Builder builder4 = new Uri.Builder();
        builder4.encodedPath(GatewayDefaultDispatcher.ECHO);
        builder4.appendQueryParameter("name", "name4");
        MASRequest request4 = new MASRequest.MASRequestBuilder(builder4.build()).build();
        MASCallbackFuture<MASResponse<String>> callback4 = new MASCallbackFuture<>();
        MAS.invoke(request4, callback4);

        Uri.Builder builder5 = new Uri.Builder();
        builder5.encodedPath(GatewayDefaultDispatcher.ECHO);
        builder5.appendQueryParameter("name", "name5");
        MASRequest request5 = new MASRequest.MASRequestBuilder(builder5.build()).build();
        MASCallbackFuture<MASResponse<String>> callback5 = new MASCallbackFuture<>();
        MAS.invoke(request5, callback5);

        assertEquals(callback1.get().getBody().getContent(), "name1");
        assertEquals(callback2.get().getBody().getContent(), "name2");
        assertEquals(callback3.get().getBody().getContent(), "name3");
        assertEquals(callback4.get().getBody().getContent(), "name4");
        assertEquals(callback5.get().getBody().getContent(), "name5");

    }

    @Test
    public void testHttpPostWithJsonArray() throws Exception {

        final JSONArray requestData = new JSONArray();
        requestData.put("test1").put("test2");

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.APPLICATION_JSON).
                        setBody(requestData.toString()); //Response with JSONArray
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .post(MASRequestBody.jsonArrayBody(requestData))
                .build();

        MASCallbackFuture<MASResponse<JSONArray>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(requestData.toString(), callback.get().getBody().getContent().toString());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        RecordedRequest recordedRequest = getRecordRequest(uri.getPath());

        assertEquals(requestData.toString(), new String(recordedRequest.getBody().readUtf8()));
    }

    @Test
    public void testHttpPostWithProductObject() throws JSONException, InterruptedException, ExecutionException {
        final JSONObject requestData = new JSONObject();
        final JSONArray arr = new JSONArray();
        requestData.put("products", arr);

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse other() {
                return new MockResponse().
                        setResponseCode(HttpURLConnection.HTTP_OK).
                        setHeader("Content-type", ContentType.APPLICATION_JSON).
                        setBody(requestData.toString());
            }
        });

        Uri uri = new Uri.Builder().
                appendEncodedPath(GatewayDefaultDispatcher.OTHER).build();

        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .post(new ProductRequestBody(requestData.toString()))
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        assertEquals(requestData.toString(), callback.get().getBody().getContent().toString());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
    }

    class ProductRequestBody extends MASRequestBody {

        private Product product;

        public ProductRequestBody(String dta) {
            this.product = new Product(dta);
        }

        @Override
        public ContentType getContentType() {
            return ContentType.APPLICATION_JSON;
        }

        @Override
        public long getContentLength() {
            return product.getProducts().toString().getBytes(getContentType().getCharset()).length;
        }

        @Override
        public void write(OutputStream outputStream) throws IOException {
            outputStream.write(product.getProducts().toString().getBytes(getContentType().getCharset()));
        }
    }

    @Test
    public void testOAuthTokenRequest() throws Exception {
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(new MASTokenRequest(), callback);

        assertNotNull(callback.get().getBody().getContent().get("access_token"));
        assertNotNull(callback.get().getBody().getContent().get("refresh_token"));
        assertNotNull(callback.get().getBody().getContent().get("expires_in"));
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

    }

    @Ignore(value = "Due to DE363094")
    public void testInvalidMAGIdentifierDuringServiceRequest() throws InterruptedException, ExecutionException, URISyntaxException {

        final boolean[] override = {true};
        final int expectedErrorCode = 3003107;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given mag-identifier is either invalid or it points to an unknown device\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.secureServiceResponse();
                }
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
    }

    @Test
    public void testInvalidClientCredentialDuringServiceRequest() throws InterruptedException, ExecutionException, URISyntaxException {

        final boolean[] override = {true};
        final int expectedErrorCode = 3003201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given client credentials were not valid\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.secureServiceResponse();
                }
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
    }

    @Test
    public void testInvalidClientCertificateDuringServiceRequest() throws InterruptedException, ExecutionException, URISyntaxException {

        final boolean[] override = {true};
        final int expectedErrorCode = 3003206;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given client certificate has expired\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.secureServiceResponse();
                }
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        //Make sure it has invoke renew endpoint
        assertNotNull(getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_RENEW));
    }

    /**
     * Storage Change scenarios:
     * 1. When reset device Pin, the mag-identifier will be removed from the keystore
     * 2. Change Account from AMS
     * 3. Change from KeyStore to AMS
     * DE369778
     */
    @Test
    public void testReRegistrationWithStorageChange() throws KeyStoreException, NoSuchAlgorithmException, URISyntaxException, ExecutionException, InterruptedException {


        //mock to switch storage
        invoke(StorageProvider.getInstance().getTokenManager(), "deleteSecureItem",
                new Class[]{String.class}, new Object[]{"msso.magIdentifier"}, Void.class);

        DeviceIdentifier oldDeviceIdentifier = new DeviceIdentifier();

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());

        //Device ID renewed
        assertNotSame(oldDeviceIdentifier.toString(), new DeviceIdentifier().toString());
        RecordedRequest recordedRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER);
        //Hitting the /register with new device ID
        assertEquals(recordedRequest.getHeader(ServerClient.DEVICE_ID),
                IoUtils.base64(new DeviceIdentifier().toString(), Charsets.ASCII));

    }
}
