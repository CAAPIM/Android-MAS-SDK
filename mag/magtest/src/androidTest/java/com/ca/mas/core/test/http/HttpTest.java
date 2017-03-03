/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.http;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;
import android.test.AndroidTestCase;
import android.util.Pair;

import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGRequestBody;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@Deprecated
@RunWith(AndroidJUnit4.class)
public class HttpTest {

    private static final String RESPONSE_DATA = "Expected Response Data";
    private static final String RESPONSE_HEADER_NAME = "headerName";
    private static final String RESPONSE_HEADER_VALUE = "headerValue";
    private static final String QUERY_PARAMETER_NAME = "queryName";
    private static final String QUERY_PARAMETER_VALUE = "queryValue";
    public static final String HTTP_TEST = "httptest";


    private MockWebServer mockWebServer;
    private MAGHttpClient client;
    private Uri.Builder builder;

    @Before
    public void setUp() throws Exception {
        mockWebServer = new MockWebServer();
        mockWebServer.start();
        setupBuilder();

        client = new MAGHttpClient();
    }

    @After
    public void tearDown() throws Exception {
        mockWebServer.shutdown();
    }

    @Deprecated
    @Test
    public void testInvalidUrl() throws Exception {
        mockWebServer.enqueue(new MockResponse().
                setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).
                setBody(RESPONSE_DATA));

        Uri uri = builder.
                appendPath("invalidUrl").build();

        MAGRequest request = new MAGRequest.MAGRequestBuilder(new URL(uri.toString())).
                get().build();

        MAGResponse response = client.execute(request);

        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, response.getResponseCode());

        if (isLocal()) {
            assertEquals(RESPONSE_DATA, new String(response.getBody().getRawContent()));
        }
    }

    @Deprecated
    @Test
    public void testHttpGet() throws Exception {
        mockWebServer.enqueue(new MockResponse().setBody(RESPONSE_DATA));
        Uri uri = builder.
                appendPath(HTTP_TEST).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MAGRequest request = new MAGRequest.MAGRequestBuilder(new URL(uri.toString())).
                get().build();

        MAGResponse response = client.execute(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (isLocal()) {
            assertEquals(RESPONSE_DATA, new String(response.getBody().getRawContent()));
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals(request.getMethod(), recordedRequest.getMethod());
            assertTrue(uri.toString().endsWith(recordedRequest.getPath()));
        }
    }

    @Deprecated
    @Test
    public void testHttpDelete() throws Exception {
        mockWebServer.enqueue(new MockResponse());
        Uri uri = builder.
                appendPath(HTTP_TEST).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MAGRequest request = new MAGRequest.MAGRequestBuilder(new URL(uri.toString())).
                delete(null).build();

        MAGResponse response = client.execute(request);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
    }


    @Deprecated
    @Test
    public void testHttpPost() throws Exception {

        String requestData = "Expected Request Data";

        mockWebServer.enqueue(new MockResponse().setBody(RESPONSE_DATA).
                addHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE));

        Uri uri = builder.
                appendPath(HTTP_TEST).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MAGRequest request = new MAGRequest.MAGRequestBuilder(new URL(uri.toString())).
                post(MAGRequestBody.stringBody(requestData)).
                header(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE).build();

        MAGResponse response = client.execute(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (isLocal()) {
            assertEquals(RESPONSE_DATA, new String(response.getBody().getRawContent()));
            assertNotNull(response.getHeaders().get(RESPONSE_HEADER_NAME));

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals(request.getMethod(), recordedRequest.getMethod());
            assertEquals(requestData, new String(recordedRequest.getBody().readUtf8()));
            assertEquals(RESPONSE_HEADER_VALUE, recordedRequest.getHeader(RESPONSE_HEADER_NAME));
            assertTrue(uri.toString().endsWith(recordedRequest.getPath()));
        }

    }

    @Deprecated
    @Test
    public void testHttpPostWithJson() throws Exception {

        JSONObject requestData = new JSONObject();
        requestData.put("jsonName", "jsonValue");
        requestData.put("jsonName2", 1234);

        mockWebServer.enqueue(new MockResponse());

        Uri uri = builder.
                appendPath(HTTP_TEST).build();

        MAGRequest request = new MAGRequest.MAGRequestBuilder(new URL(uri.toString()))
                .post(MAGRequestBody.jsonBody(requestData))
                .responseBody(MAGResponseBody.jsonBody())
                .build();

        MAGResponse<JSONObject> response = client.execute(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (isLocal()) {
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals(request.getMethod(), recordedRequest.getMethod());
            assertEquals(requestData.toString(), new String(recordedRequest.getBody().readUtf8()));
        }

    }

    @Deprecated
    @Test
    public void testHttpPostWithUrlEncodedForm() throws Exception {

        mockWebServer.enqueue(new MockResponse());

        Uri uri = builder.
                appendPath(HTTP_TEST).build();

        List<Pair<String, String>> form = new ArrayList<>();
        form.add(new Pair<String, String>("formfield1", "field1Value"));
        form.add(new Pair<String, String>("formfield2", "field2Value"));
        form.add(new Pair<String, String>("formfield3", "field3Value"));


        MAGRequest request = new MAGRequest.MAGRequestBuilder(new URL(uri.toString())).
                post(MAGRequestBody.urlEncodedFormBody(form)).build();

        MAGResponse response = client.execute(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (isLocal()) {
            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals(request.getMethod(), recordedRequest.getMethod());
        }

    }

    @Deprecated
    @Test
    public void testHttpPut() throws Exception {

        String requestData = "Expected Request Data";
        String requestHeaderName = "ExpectedRequestHeaderName";
        String requestHeaderValue = "ExpectedRequestHeaderValue";

        mockWebServer.enqueue(new MockResponse().setBody(RESPONSE_DATA).
                addHeader(RESPONSE_HEADER_NAME, RESPONSE_HEADER_VALUE));

        Uri uri = builder.
                appendPath(HTTP_TEST).
                appendQueryParameter(QUERY_PARAMETER_NAME, QUERY_PARAMETER_VALUE).build();

        MAGRequest request = new MAGRequest.MAGRequestBuilder(new URL(uri.toString())).
                put(MAGRequestBody.stringBody(requestData)).
                header(requestHeaderName, requestHeaderValue).build();

        MAGResponse response = client.execute(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (isLocal()) {
            assertEquals(RESPONSE_DATA, new String(response.getBody().getRawContent()));
            assertNotNull(response.getHeaders().get(RESPONSE_HEADER_NAME));

            RecordedRequest recordedRequest = mockWebServer.takeRequest();
            assertEquals(request.getMethod(), recordedRequest.getMethod());
            assertEquals(requestData, new String(recordedRequest.getBody().readUtf8()));
            assertEquals(requestHeaderValue, recordedRequest.getHeader(requestHeaderName));
            assertTrue(uri.toString().endsWith(recordedRequest.getPath()));
        }

    }

    private void setupBuilder() {
        if (isLocal()) {
            builder = Uri.parse("http://localhost:" + mockWebServer.getPort()).buildUpon();
        } else {
            String host = "http://mag-longbow-rc1.ca.com:8080/";
            builder = Uri.parse(host).buildUpon();
        }
    }

    private boolean isLocal() {
        return true;
    }


}
