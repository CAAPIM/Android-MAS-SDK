/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.core.policy.exceptions.InvalidClientCredentialException;
import com.ca.mas.core.registration.RegistrationServerException;
import com.squareup.okhttp.mockwebserver.MockResponse;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASClientCredentialTest extends MASStartTestBase {

    @Test
    public void initializeClientCredentialsTest() throws InterruptedException, URISyntaxException {
        final int expectedErrorCode = 1002201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The client could not be authenticated due to missing or invalid client_id\" }";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse initializeResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).setHeader("x-ca-err", expectedErrorCode).setBody(expectedErrorMessage);
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof InvalidClientCredentialException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof InvalidClientCredentialException);
        }
    }

    @Test
    public void registerWithInvalidClientCredentials() throws InterruptedException {
        final int expectedErrorCode = 1000201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The client could not be authenticated due to missing or invalid credentials\" }";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).setHeader("x-ca-err", expectedErrorCode).setBody(expectedErrorMessage);
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        try {
            Assert.assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof InvalidClientCredentialException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof RegistrationServerException);
        }

    }

    @Test
    public void getAccessTokenWithInvalidClientCredentials() throws InterruptedException, TimeoutException {
        final int expectedErrorCode = 3003201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_client\", \"error_description\":\"The client could not be authenticated due to missing or invalid credentials\" }";

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse retrieveTokenResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).setHeader("x-ca-err", expectedErrorCode).setBody(expectedErrorMessage);
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        try {
            Assert.assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof InvalidClientCredentialException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof OAuthServerException);
        }
    }
}
