/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.oauth;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.request.internal.OAuthTokenRequest;
import com.ca.mas.core.store.PrivateTokenStorage;
import com.ca.mas.core.test.BaseTest;
import com.ca.mas.core.test.DefaultDispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class AccessProtectedEndpointTest extends BaseTest {

    private final String SCOPE = "read write";

    @Test
    public void testAccessProtectedEndpoint() throws URISyntaxException, InterruptedException, IOException {
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).password().build();
        processRequest(request);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (useMockServer()) {
            ssg.takeRequest(); //Authorize Request
            ssg.takeRequest(); //Client Credentials Request

            //Make sure the register request contain authorization header
            RecordedRequest registerRequest = ssg.takeRequest();
            assertNotNull(registerRequest.getHeader("authorization"));

            //Make sure the access token request use id-token grant type
            RecordedRequest accessTokenRequest = ssg.takeRequest();
            String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
            assertTrue(s.contains("assertion=" + getIdToken()));
            assertTrue(s.contains("grant_type=" + getIdTokenType()));
        }
    }

    @Test
    public void testAccessProtectedEndpointCancelOnExecutingRequest() throws URISyntaxException, InterruptedException, IOException {
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/slow")).password().build();
        final String KEY = "key";
        final String VALUE = "This is a test";

        final boolean[] result = {false};
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        long requestID = mobileSso.processRequest(request, new MAGResultReceiver() {
            @Override
            public void onSuccess(MAGResponse response) {
                countDownLatch.countDown();
            }

            @Override
            public void onError(MAGError error) {
                countDownLatch.countDown();
            }

            @Override
            public void onRequestCancelled(Bundle data) {
                String value = data.getString(KEY);
                if (value.equals(VALUE)) {
                    result[0] = true;
                }
                countDownLatch.countDown();
            }
        });
        Thread.sleep(100); //Let the engine put the message in the queue
        if (useMockServer()) {
            ssg.takeRequest(); //Authorize Request
            ssg.takeRequest(); //Client Credentials Request
            ssg.takeRequest(); //register request
            ssg.takeRequest(); //access token
        }
        Bundle data = new Bundle();
        data.putString(KEY, VALUE);
        mobileSso.cancelRequest(requestID, data);
        ssg.takeRequest(); //The slow response
        countDownLatch.await();
        assertTrue(result[0]);


    }


    @Test
    public void testAccessProtectedEndpointCancelAllOnExecutingRequest() throws URISyntaxException, InterruptedException, IOException {
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/slow")).password().build();
        final String KEY = "key";
        final String VALUE = "This is a test";

        final boolean[] result = {false};
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        long requestID = mobileSso.processRequest(request, new MAGResultReceiver() {
            @Override
            public void onSuccess(MAGResponse response) {
                countDownLatch.countDown();
            }

            @Override
            public void onError(MAGError error) {
                countDownLatch.countDown();
            }

            @Override
            public void onRequestCancelled(Bundle data) {
                String value = data.getString(KEY);
                if (value.equals(VALUE)) {
                    result[0] = true;
                }
                countDownLatch.countDown();
            }
        });
        Thread.sleep(100);
        if (useMockServer()) {
            ssg.takeRequest(); //Authorize Request
            ssg.takeRequest(); //Client Credentials Request
            ssg.takeRequest(); //register request
            ssg.takeRequest(); //access token
        }
        Bundle data = new Bundle();
        data.putString(KEY, VALUE);
        mobileSso.cancelAllRequests(data);
        ssg.takeRequest(); //The slow response
        countDownLatch.await();
        assertTrue(result[0]);


    }


    @Test
    public void testAccessProtectedEndpointWithOverrideScope() throws URISyntaxException, InterruptedException, IOException {
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts").toURL())
                .password()
                .scope(SCOPE)
                .build();
        processRequest(request);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (useMockServer()) {
            ssg.takeRequest(); //Authorize Request
            ssg.takeRequest(); //Client Credentials Request
            ssg.takeRequest(); //Register
            ssg.takeRequest(); //Access Token for authentication

            RecordedRequest accessTokenRequest = ssg.takeRequest(); //Access token request for new scope
            String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
            assertTrue(s.contains("scope=" + URLEncoder.encode(SCOPE, "utf-8")));
        }
    }

    @Test
    public void getAccessTokenUsingRefreshToken() throws InterruptedException, JSONException {

        assumeMockServer();

        final String NEW_ACCESS_TOKEN = "new_access_token";

        final boolean[] newToken = {false};

        ssg.setDispatcher(new DefaultDispatcher() {
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

        MAGRequest request = new OAuthTokenRequest();
        processRequest(request);
        response = null;
        error = null;
        newToken[0] = true;

        //Remove Access Token
        DataSource<String, String> dataSource = DataSourceFactory.getStorage(InstrumentationRegistry.getTargetContext(),
                KeystoreDataSource.class, null, null);
        for (String k : dataSource.getKeys(null)) {
            if (k.contains(PrivateTokenStorage.KEY.PREF_ACCESS_TOKEN.name())) {
                dataSource.remove(k);
            }
        }

        processRequest(request);
        assertEquals(NEW_ACCESS_TOKEN, ((JSONObject) (response.getBody().getContent())).getString("accesstoken"));

    }

    @Test
    public void testAccessProtectedEndpointWith204Response() throws URISyntaxException, InterruptedException, IOException {

        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/testNoContent").toURL())
                .build();

        processRequest(request);
        assertEquals(HttpURLConnection.HTTP_NO_CONTENT, response.getResponseCode());
        assertEquals(0, ((byte[]) response.getBody().getContent()).length);


    }

    @Test
    public void testMagIdentifier() throws Exception {
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();
        processRequest(request);

        RecordedRequest recordedRequest = null;
        for (int i = 0; i < ssg.getRequestCount(); i++) {
            recordedRequest = ssg.takeRequest();
            String path = recordedRequest.getPath();
            if (path.startsWith(DefaultDispatcher.CONNECT_DEVICE_REGISTER) ||
                    path.startsWith(DefaultDispatcher.AUTH_OAUTH_V2_AUTHORIZE) ||
                    path.startsWith(DefaultDispatcher.CONNECT_CLIENT_INITIALIZE) ||
                    path.startsWith(DefaultDispatcher.CONNECT_DEVICE_REGISTER_CLIENT)) {
                assertNull(recordedRequest.getHeader(ServerClient.MAG_IDENTIFIER));
            } else {
                assertNotNull(recordedRequest.getHeader(ServerClient.MAG_IDENTIFIER));
            }
        }

        //send again
        processRequest(request);
        recordedRequest = ssg.takeRequest();
        assertNotNull(recordedRequest.getHeader(ServerClient.MAG_IDENTIFIER));

        mobileSso.removeDeviceRegistration();
        recordedRequest = ssg.takeRequest();
        assertNotNull(recordedRequest.getHeader(ServerClient.MAG_IDENTIFIER));

    }

    @Test
    public void testConnectionListener() throws Exception {
        final boolean[] got = {false};
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts"))
                .connectionListener(new MAGRequest.MAGConnectionListener() {
                    @Override
                    public void onObtained(HttpURLConnection connection) {
                        got[0] = true;
                    }

                    @Override
                    public void onConnected(HttpURLConnection connection) {

                    }
                })
                .build();
        processRequest(request);
        assertTrue(got[0]);
    }
}
