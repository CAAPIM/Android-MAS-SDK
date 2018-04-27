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
import com.ca.mas.core.oauth.OAuthException;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.junit.Test;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASOAuthTest extends MASStartTestBase {

    @Test
    public void getAccessTokenWithMissingAccessToken() throws InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                String token = "{\n" +
                        "  \"token_type\":\"Bearer\",\n" +
                        "  \"expires_in\":" + new Date().getTime() + 3600 + ",\n" +
                        "  \"refresh_token\":\"19785fca-4b86-4f8e-a73c-7de1d420f88d\",\n" +
                        "  \"scope\":\"openid msso phone profile address email\"\n" +
                        "}";
                return new MockResponse().setResponseCode(200).setBody(token);
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        try {
            Assert.assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof OAuthException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof OAuthException);
        }
    }

    @Test
    public void testInvalidMAGIdentifierDuringTokenRequest() throws InterruptedException, ExecutionException {

        final boolean[] override = {true};
        final int expectedErrorCode = 3003107;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given mag-identifier is either invalid or it points to an unknown device\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.retrieveTokenResponse();
                }
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        Assert.assertNotNull(callback.get());
    }

    @Test
    public void testInvalidClientCredentialDuringTokenRequest() throws InterruptedException, ExecutionException {

        final boolean[] override = {true};
        final int expectedErrorCode = 3003201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given client credentials were not valid\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.retrieveTokenResponse();
                }
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        Assert.assertNotNull(callback.get());
    }

    @Test
    public void testInvalidClientCertificateDuringTokenRequest() throws InterruptedException, ExecutionException {

        final boolean[] override = {true};
        final int expectedErrorCode = 3003206;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given client certificate has expired\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.retrieveTokenResponse();
                }
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        Assert.assertNotNull(callback.get());
        //Make sure it has invoke renew endpoint
        assertNotNull(getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_RENEW));
    }



}
