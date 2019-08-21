/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.auth.AuthenticationException;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.token.JWTExpiredException;
import com.ca.mas.core.token.JWTInvalidAUDException;
import com.ca.mas.core.token.JWTInvalidAZPException;
import com.ca.mas.core.token.JWTInvalidSignatureException;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;

import junit.framework.Assert;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.RecordedRequest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASLoginTest extends MASStartTestBase {

    //Mock response for device registration
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


    @After
    public void deregister() throws InterruptedException, ExecutionException {
        // - reset singleton value to default one
        MAS.enableIdTokenValidation(true);
        if (isSkipped) return;
        MASCallbackFuture<Void> logoutCallback = new MASCallbackFuture<Void>();
        if (MASUser.getCurrentUser() != null) {
            MASUser.getCurrentUser().logout(true, logoutCallback);
            Assert.assertNull(logoutCallback.get());
        }

        if (MASDevice.getCurrentDevice().isRegistered()) {
            MASCallbackFuture<Void> deregisterCallback = new MASCallbackFuture<Void>();
            MASDevice.getCurrentDevice().deregister(deregisterCallback);
            Assert.assertNull(deregisterCallback.get());
        }
    }

    @Test
    public void testAuthenticationListener() throws JSONException, InterruptedException, URISyntaxException, ExecutionException {

        final boolean[] result = {false};
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        MAS.setAuthenticationListener(new MASAuthenticationListener() {

            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                MASUser.login("test", "test".toCharArray(), new MASCallback<MASUser>() {
                    @Override
                    public void onSuccess(MASUser masUser) {
                        result[0] = true;
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        result[0] = false;
                        countDownLatch.countDown();
                    }
                });
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        Assert.assertNotNull(callback.get());
        countDownLatch.await();
        Assert.assertTrue(result[0]);

    }

    @Test
    public void testCallbackWithAuthenticateFailed() throws InterruptedException, URISyntaxException, ExecutionException {

        final boolean[] override = {true};
        final int expectedErrorCode = 1000202;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The resource owner could not be authenticated due to missing or invalid credentials\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                if (override[0]) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                            .setHeader("WWW-Authenticate", "Basic realm=\"fake\"")
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.registerDeviceResponse(request);
                }
            }
        });

        final Throwable[] throwable = new Throwable[1];

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, final long requestId, MASAuthenticationProviders providers) {
                MASUser.login("test", "test".toCharArray(), new MASCallback<MASUser>() {
                    @Override
                    public void onSuccess(MASUser masUser) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(Throwable e) {
                        throwable[0] = e;
                        override[0] = false;
                        MAS.processPendingRequests();
                        countDownLatch.countDown();
                    }
                });
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        Assert.assertNotNull(callback.get());
        countDownLatch.await();

        assertTrue((throwable[0]).getCause() instanceof AuthenticationException);
        assertTrue(((MASException) throwable[0]).getRootCause() instanceof AuthenticationException);
        AuthenticationException e = (AuthenticationException) (throwable[0]).getCause();
        assertEquals(CONTENT_TYPE_VALUE, e.getContentType());
        assertEquals(expectedErrorCode, e.getErrorCode());
        assertEquals(expectedErrorMessage, e.getMessage());
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatus());

    }

    @Test
    public void invalidSignature() throws InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("device-status", "activated")
                        .setHeader("mag-identifier", "test-device")
                        .setHeader("id-token", "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDI0MDA4Nzg1OTEsCiAiYXpwIjogInRlc3QtZGV2aWNlIiwKICJzdWIiOiAieCIsCiAiYXVkIjogImR1bW15IiwKICJpc3MiOiAiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsCiAiaWF0IjogMTQwMDg3ODU5MQp9.zenKvXlhDtpXym_auPCbukBiVqr3rqZrcoeDyfsvftA")
                        .setHeader("id-token-type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                        .setBody(cert);
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        try {
            Assert.assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof JWTInvalidSignatureException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof JWTInvalidSignatureException);
        }
    }

    @Test(expected = ExecutionException.class)
    public void invalidAlgorithmLoginValidationEnabled() throws InterruptedException, ExecutionException {

        // - the idtoken with RS254
        final String idToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJleHAiOjI0MDA4Nzg1OTEsImF6cCI6InRlc3QtZGV2aWNlIiwic3ViIjoieCIsImF1ZCI6ImR1bW15IiwiaXNzIjoiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsImlhdCI6MTQwMDg3ODU5MX0.HJ5B3CZZ7Oxk8SZfHNARYialgF8E0r4WQPd4uQLYJPp0VUhOVkbUbPxS95rFbIUHADFYPbMOQcEGscJ0864LnBOXCkXCBEybOH56hKNKQuMl1Kg5Ow2f80-9-8zStqEikgSCZ8-fpeH_8KMgSsdHp21kiDe1BIwIcxIZ_o-WO0M";
        final String idTokenType = "urn:ietf:params:oauth:grant-type:jwt-bearer";
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("device-status", "activated")
                        .setHeader("mag-identifier", "test-device")
                        .setHeader("id-token", idToken)
                        .setHeader("id-token-type", idTokenType)
                        .setBody(cert);
            }
        });
        MAS.enableIdTokenValidation(true);
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        assertNotNull(callback.get());
    }

    @Test
    public void invalidAlgorithmLoginValidationDisabled() throws InterruptedException, ExecutionException {

        // - the idtoken with RS254
        final String idToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiJ9.eyJleHAiOjI0MDA4Nzg1OTEsImF6cCI6InRlc3QtZGV2aWNlIiwic3ViIjoieCIsImF1ZCI6ImR1bW15IiwiaXNzIjoiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsImlhdCI6MTQwMDg3ODU5MX0.HJ5B3CZZ7Oxk8SZfHNARYialgF8E0r4WQPd4uQLYJPp0VUhOVkbUbPxS95rFbIUHADFYPbMOQcEGscJ0864LnBOXCkXCBEybOH56hKNKQuMl1Kg5Ow2f80-9-8zStqEikgSCZ8-fpeH_8KMgSsdHp21kiDe1BIwIcxIZ_o-WO0M";
        final String idTokenType = "urn:ietf:params:oauth:grant-type:jwt-bearer";
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("device-status", "activated")
                        .setHeader("mag-identifier", "test-device")
                        .setHeader("id-token", idToken)
                        .setHeader("id-token-type", idTokenType)
                        .setBody(cert);
            }
        });
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MAS.enableIdTokenValidation(false);
        MASUser.login("test", "test".toCharArray(), callback);
        assertNotNull(callback.get());
    }

    @Test
    public void invalidAud() throws InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse initializeResponse() {
                String result = "{\"client_id\":\"8298bc51-f242-4c6d-b547-d1d8e8519cb5\", \"client_secret\":\"dummy\", \"client_expiration\":" + new Date().getTime() + 36000 + "}";
                return new MockResponse().setResponseCode(200).setBody(result);
            }

            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("device-status", "activated")
                        .setHeader("mag-identifier", "test-device")
                        .setHeader("id-token", "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDI0MDA4Nzg1OTEsCiAiYXpwIjogInRlc3QtZGV2aWNlIiwKICJzdWIiOiAieCIsCiAiYXVkIjogImR1bW15IiwKICJpc3MiOiAiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsCiAiaWF0IjogMTQwMDg3ODU5MQp9.zenKvXlhDtpXym_auPCbukBiVqr3rqZrcoeDyfsvftA")
                        .setHeader("id-token-type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                        .setBody(cert);
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        try {
            Assert.assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof JWTInvalidAUDException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof JWTInvalidAUDException);

        }
    }

    @Test
    public void invalidAzp() throws InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("device-status", "activated")
                        .setHeader("mag-identifier", "dummy-device")
                        .setHeader("id-token", "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDI0MDA4Nzg1OTEsCiAiYXpwIjogInRlc3QtZGV2aWNlIiwKICJzdWIiOiAieCIsCiAiYXVkIjogImR1bW15IiwKICJpc3MiOiAiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsCiAiaWF0IjogMTQwMDg3ODU5MQp9.zenKvXlhDtpXym_auPCbukBiVqr3rqZrcoeDyfsvftA")
                        .setHeader("id-token-type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                        .setBody(cert);
            }
        });
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        try {
            Assert.assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof JWTInvalidAZPException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof JWTInvalidAZPException);
        }

    }

    @Test
    public void invalidExp() throws InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse initializeResponse() {
                String result = "{\"client_id\":\"8298bc51-f242-4c6d-b547-d1d8e8519cb4\", \"client_secret\":\"dummy\", \"client_expiration\":" + new Date().getTime() + 36000 + "}";
                return new MockResponse().setResponseCode(200).setBody(result);
            }

            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("device-status", "activated")
                        .setHeader("mag-identifier", "d9b38b22-14a2-4573-80b7-b95de92b18he")
                        .setHeader("id-token", "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDE0MDA3OTQ2MjEsCiAiYXpwIjogImQ5YjM4YjIyLTE0YTItNDU3My04MGI3LWI5NWRlOTJiMThoZSIsCiAic3ViIjogIngiLAogImF1ZCI6ICI4Mjk4YmM1MS1mMjQyLTRjNmQtYjU0Ny1kMWQ4ZTg1MTljYjQiLAogImlzcyI6ICJodHRwOi8vbS5sYXllcjd0ZWNoLmNvbS9jb25uZWN0IiwKICJpYXQiOiAxNDAwNzk0NjIxCn0.H4Yvz9d-uzoWGWeshgYTFLm110B1M1pb63vrwrJsIIg")
                        .setHeader("id-token-type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                        .setBody(cert);
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        try {
            Assert.assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof JWTExpiredException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof JWTExpiredException);
        }
    }

    @Test
    public void testLoginWithIdToken() throws Exception {
        String expected = "dummy_id_token";
        String expectedType = "dummy_id_token_type";

        //Register with id token
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASIdToken idToken = new MASIdToken.Builder().value(expected).type(expectedType).build();
        MASUser.login(idToken, callback);
        Assert.assertNotNull(callback.get());
        RecordedRequest registerRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER);
        Assert.assertEquals(registerRequest.getHeader("authorization"), "Bearer " + expected);
        Assert.assertEquals(registerRequest.getHeader("x-authorization-type"), expectedType);

        //Logout
        MASCallbackFuture<Void> logoutCallback = new MASCallbackFuture<>();
        MASUser.getCurrentUser().logout(true, logoutCallback);
        logoutCallback.get();

        //invoke token with id token
        MASCallbackFuture<MASUser> loginCallback = new MASCallbackFuture<>();
        MASIdToken newIdToken = new MASIdToken.Builder().value(expected).type(expectedType).build();
        MASUser.login(newIdToken, loginCallback);
        Assert.assertNotNull(loginCallback.get());

        RecordedRequest tokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String body = URLDecoder.decode(tokenRequest.getBody().readUtf8(), "UTF-8");
        assertTrue(body.contains(ServerClient.ASSERTION + "=" + expected));
        assertTrue(body.contains(ServerClient.GRANT_TYPE + "=" + expectedType));
    }

    @Test
    public void testLoginWithIdTokenDefaultType() throws Exception {
        String expected = "dummy_id_token";
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASIdToken idToken = new MASIdToken.Builder().value(expected).build();
        MASUser.login(idToken, callback);
        Assert.assertNotNull(callback.get());
        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER);
        Assert.assertEquals(rr.getHeader("authorization"), "Bearer " + expected);
        Assert.assertEquals(rr.getHeader("x-authorization-type"), MASIdToken.JWT_DEFAULT);
    }

    @Test
    public void testLoginFailFalseParameter() throws ExecutionException, InterruptedException {
        String expected = "dummy_id_token";
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASIdToken idToken = new MASIdToken.Builder().value(expected).build();
        MASUser.login(idToken, callback);
        callback.get();

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse logout() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
            }
        });

        MASCallbackFuture<Void> logoutCallback = new MASCallbackFuture<Void>();
        MASUser.getCurrentUser().logout(false, logoutCallback);
        try {
            logoutCallback.get();
            fail();
        } catch (Exception e) {
            assertNotNull(StorageProvider.getInstance().getTokenManager().getIdToken());
        }
        setDispatcher(new GatewayDefaultDispatcher());
    }

    @Test(expected = NullPointerException.class)
    public void testLoginWithIDTokenNullValue() throws Exception {
        new MASIdToken.Builder().build();
    }

    @Test
    public void testCallbackWithAuthenticateFailedAndCancel() throws JSONException, InterruptedException, URISyntaxException {

        final int expectedErrorCode = 1000202;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The resource owner could not be authenticated due to missing or invalid credentials\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).
                        setHeader("x-ca-err", expectedErrorCode).
                        setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);

            }
        });

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, final long requestId, MASAuthenticationProviders providers) {

                MASUser.login("admin", "7layer".toCharArray(), new MASCallback<MASUser>() {
                    @Override
                    public void onSuccess(MASUser result) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        MAS.cancelRequest(requestId);
                    }
                });

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .notifyOnCancel()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof MAS.RequestCancelledException);
        }
    }

    @Test
    public void loginTest() throws InterruptedException, ExecutionException {

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);
        assertNotNull(callback.get());
        assertNotNull(MASUser.getCurrentUser());

    }

    @Test
    public void testAuthenticationFail() throws InterruptedException {

        final int expectedErrorCode = 1000202;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The resource owner could not be authenticated due to missing or invalid credentials\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                        .setHeader("WWW-Authenticate", "Basic realm=\"fake\"")
                        .setHeader("x-ca-err", expectedErrorCode)
                        .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);

            }
        });

        MASCallbackFuture<MASUser> callbackFuture = new MASCallbackFuture<>();

        MASUser.login("admin", "invalid".toCharArray(), callbackFuture);

        try {
            callbackFuture.get();
            fail();
        } catch (ExecutionException e) {
            assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, ((AuthenticationException) e.getCause().getCause()).getStatus());
        }


    }

    @Test(expected = IllegalArgumentException.class)
    public void authenticateTestWithNullUsername() {
        MASUser.login(null, "7layer".toCharArray(), null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void authenticateTestWithNullPassword() {
        MASUser.login("admin", (char[]) null, null);
    }

    @Test
    public void testPendingRequest() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .build();

        final CountDownLatch authenticationListenerCountDownLaunch = new CountDownLatch(10);

        MAS.setAuthenticationListener(new MASAuthenticationListener() {

            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                authenticationListenerCountDownLaunch.countDown();
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        final CountDownLatch requestCountDownLaunch = new CountDownLatch(10);

        for (int i = 0; i < 10; i++) {
            MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    requestCountDownLaunch.countDown();

                }

                @Override
                public void onError(Throwable e) {

                }
            });
        }
        authenticationListenerCountDownLaunch.await();
        //onAuthenticateRequest has been invoked 10 times
        assertTrue(true);

        MASUser.login("admin", "7layer".toCharArray(), null);

        requestCountDownLaunch.await();
        //All Pending request are executed after login
        assertTrue(true);


    }

    @Test
    public void testCustomAuthCredentials() throws Exception {
        MASAuthCredentials authCredentials = new CustomMASAuthCredentials("admin", "7layer".toCharArray(), "custom_value");
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login(authCredentials, callback);
        Assert.assertNotNull(callback.get());

        RecordedRequest registerRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER);
        Assert.assertEquals(registerRequest.getHeader("panCard"), "custom_value");

        //Logout
        MASCallbackFuture<Void> logoutCallback = new MASCallbackFuture<>();
        MASUser.getCurrentUser().logout(true,logoutCallback );
        logoutCallback.get();

        //invoke token with id token
        callback.reset();
        MASUser.login(authCredentials, callback);
        Assert.assertNotNull(callback.get());

        RecordedRequest tokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String body = URLDecoder.decode(tokenRequest.getBody().readUtf8(), "UTF-8");
        assertTrue(body.contains("panCard=custom_value"));
    }
}
