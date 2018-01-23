/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.net.Uri;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.clientcredentials.ClientCredentialsServerException;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.core.policy.exceptions.InvalidClientCredentialException;
import com.ca.mas.core.registration.RegistrationServerException;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
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
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof ClientCredentialsServerException);
        }
    }

    @Test
    public void registerWithInvalidClientCredentials() throws InterruptedException {
        final int expectedErrorCode = 1000201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The client could not be authenticated due to missing or invalid credentials\" }";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
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
    public void loginWithInvalidClientCredentials() throws InterruptedException, ExecutionException {

        MASCallbackFuture<MASUser> loginCallback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), loginCallback);
        loginCallback.get();

        //remove the id token
        MASCallbackFuture<Void> logoutCallback = new MASCallbackFuture<>();
        MASUser.getCurrentUser().logout(logoutCallback);
        logoutCallback.get();

        final int expectedErrorCode = 3003201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The client could not be authenticated due to missing or invalid credentials\" }";

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

    @Test
    public void testClientCredentialsGrantType() throws URISyntaxException, InterruptedException, IOException, ExecutionException {

        final boolean[] result = {true};

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return noIdTokenRegisterDeviceResponse();
            }
        });

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                result[0] = false;
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .clientCredential()
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());

        //Make sure the register request doesn't contain authorization header
        RecordedRequest registerRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER_CLIENT);
        assertNull(registerRequest.getHeader("authorization"));

        //Make sure the access token request use client_credentials grant type
        RecordedRequest accessTokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
        assertTrue(s.contains("grant_type=" + new MASAuthCredentialsClientCredentials().getGrantType()));

        assertNull(MASUser.getCurrentUser());
        assertTrue(result[0]);
    }

    @Test
    public void testClientCredentialsMASSetGrantType() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        try {
            final boolean[] result = {true};

            setDispatcher(new GatewayDefaultDispatcher() {

                @Override
                protected MockResponse registerDeviceResponse(RecordedRequest request) {
                    return noIdTokenRegisterDeviceResponse();
                }
            });

            MAS.setAuthenticationListener(new MASAuthenticationListener() {
                @Override
                public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                    result[0] = false;
                }

                @Override
                public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

                }
            });

            MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS);
            MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                    .build();

            MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
            MAS.invoke(request, callback);
            assertNotNull(callback.get());

            //Make sure the register request doesn't contain authorization header
            RecordedRequest registerRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER_CLIENT);
            assertNull(registerRequest.getHeader("authorization"));

            //Make sure the access token request use client_credentials grant type
            RecordedRequest accessTokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
            String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
            assertTrue(s.contains("grant_type=" + new MASAuthCredentialsClientCredentials().getGrantType()));

            assertNull(MASUser.getCurrentUser());
            assertTrue(result[0]);

            MASClaims claims = new MASClaimsBuilder().build();
            Uri uri = Uri.parse(MASConfiguration.getCurrentConfiguration().getGatewayUrl() + "/test?" + s);
            String clientId = uri.getQueryParameter("client_id");
            assertEquals(clientId, claims.getSubject());

        } finally {
            //reset to default
            MAS.setGrantFlow(MASConstants.MAS_GRANT_FLOW_PASSWORD);
        }
   }

    private MockResponse noIdTokenRegisterDeviceResponse() {

        //Mock response for device registration without id token
        String cert = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDCjCCAfKgAwIBAgIIKzRkwk/TRDswDQYJKoZIhvcNAQEMBQAwIzEhMB8GA1UEAxMYYXdpdHJp\n" +
                "c25hLWRlc2t0b3AuY2EuY29tMB4XDTEzMTEyNzE5MzkwOVoXDTE4MTEyNjE5MzkwOVowIzEhMB8G\n" +
                "A1UEAxMYYXdpdHJpc25hLWRlc2t0b3AuY2EuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n" +
                "CgKCAQEAoaCzdLbRhqt3T4ROTgOBD5gizxsJ/vhqmIpagXU+3OPhZocwf0FIVjvbrybkj8ZynTve\n" +
                "p1cJsAmdkuX+w6m8ow2rAR/8BQnIaBD281gNqDCYXAGkguEZBbCQ2TvD4FZYnJZSmrE9PJtIe5pq\n" +
                "DneOqaO0Kqj3sJpYIG11U8djio9UNAqTd0J9q5+fEMVle/QG0X0ro3MR30PaHIA7bpvISpjFZ0zD\n" +
                "54rQc+85bOamg4aJFcfiNSMIaAYaFMi/peJLmW8Q4DZriAQSG6PIBcekMx1mi4tuXkSrr3P3ycKu\n" +
                "bU0ePKnxckxWHygK42bQ5ClLuJeYNPxqHiBapZj2hwmzsQIDAQABo0IwQDAdBgNVHQ4EFgQUZddX\n" +
                "bkxC+asQgSCSIViGKuGS2f4wHwYDVR0jBBgwFoAUZddXbkxC+asQgSCSIViGKuGS2f4wDQYJKoZI\n" +
                "hvcNAQEMBQADggEBAHK/QdXrRROjKjxwU05wo1KZNRmi8jBsKF/ughCTqcUCDmEuskW/x9VCIm/r\n" +
                "ZMFgOA3tou7vT0mX8gBds+95td+aNci1bcBBpiVIwiqOFhBrtbiAhYofgXtbcYchL9SRmIpek/3x\n" +
                "BwBj5CBmaimOZsTLp6wqzLE4gpAdTMaU+RIlwq+uSUmKhQem6fSthGdWx5Ea9gwKuVi8PwSFCs/Q\n" +
                "nwUfNnCvOTP8PtQgvmLsXeaFfy/lYK7iQp1CiwwXYpc3Xivv9A7DH7MqVSQZdtjDrRI2++1/1Yw9\n" +
                "XoYtMDN0dQ5lBNIyJB5rWtCixZgfacHp538bMPMskLePU3dxNdCqhas=\n" +
                "-----END CERTIFICATE-----";
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("device-status", "activated")
                .setHeader("mag-identifier", "test-device")
                .setBody(cert);

    }
}
