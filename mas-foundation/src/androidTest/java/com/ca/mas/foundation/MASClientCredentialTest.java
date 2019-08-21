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

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

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
        MASUser.getCurrentUser().logout(true, logoutCallback);
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
                "MIIDGDCCAgCgAwIBAgIIaJHtKa4XQj4wDQYJKoZIhvcNAQEMBQAwKjEoMCYGA1UEAxMfbW9iaWxl\n" +
                "LXN0YWdpbmctbXlzcWwubDd0ZWNoLmNvbTAeFw0xNzAxMDQxNzI2MzlaFw0yNzAxMDIxNzI2Mzla\n" +
                "MCoxKDAmBgNVBAMTH21vYmlsZS1zdGFnaW5nLW15c3FsLmw3dGVjaC5jb20wggEiMA0GCSqGSIb3\n" +
                "DQEBAQUAA4IBDwAwggEKAoIBAQDsr6QwY8DL7J4aa1MWt+qmJKDGk/4M7Cx7sSUiMvuc9S3cGddQ\n" +
                "lYOaQsg1a6H8DzsCE7WkX/CcYvJSQ/V26pQfbuwp39C7kTofo5OXZNbQX0EYJjUDfJsZ0lo1GUkn\n" +
                "dCX0ugR1/NXAzmZYcTGIFVi/y2mMynZHLeEZUKL/O3vS3uniEw4qcxQ2Jz1qT4gGJJNcHHM+4SqV\n" +
                "17yXm5trvr1aHey3G3KgQWVo0OQ/vZoiRSURADUvWRsym+6CALp73KS1wtbsopE2VtSLrm4ztBbH\n" +
                "EfH/mp4PkZjpNisoaJwyqCCP+f7ITYSXnjuiGrC/z1KrENGCzXSJl3lHjUFOiZYnAgMBAAGjQjBA\n" +
                "MB0GA1UdDgQWBBRF0EYejzI/wOSIrB+kz+FgATJdcDAfBgNVHSMEGDAWgBRF0EYejzI/wOSIrB+k\n" +
                "z+FgATJdcDANBgkqhkiG9w0BAQwFAAOCAQEAOKKUsR3RsYCtiJ+3omovqDFmexlWlW02we0ELwia\n" +
                "312ATazQPFTxjiHnOyhG+K67ItqTbz3X7vQP8yvQ91JWTHesebnYSxJEAqTEiBC2uLPP7XqUWnJa\n" +
                "J/XGMAhRVIbkaHfzleWl+BtG++B4tclHqhRWrPfP5S1Ys3SCvmhte09XAmuuPYnuzsoZwJVpx/UJ\n" +
                "lYxOuSIkYxUOCzGVp7qUYBVzMVW2MEKOiJvAuXM0aeY5+D5Z6uMs+F58W5nbYCgjLTVXRAm46ntG\n" +
                "NP9R2i3LWnjHhdN+WLtSsmj6dFtzjQbrS9LXa8bR4GRncA34UdW/LMsyiJzd2Iy8mfe2sQu3Zg==\n" +
                "-----END CERTIFICATE-----";
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("device-status", "activated")
                .setHeader("mag-identifier", "test-device")
                .setBody(cert);

    }
}
