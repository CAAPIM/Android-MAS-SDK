/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */
package com.ca.mas.foundation;

import android.content.Context;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.auth.AuthenticationException;
import com.ca.mas.core.registration.RegistrationServerException;
import com.ca.mas.core.store.ClientCredentialContainer;
import com.ca.mas.core.store.OAuthTokenContainer;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MASRegistrationTest extends MASStartTestBase {
    @Test
    public void testRenewCertification() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                // Expired cert to trigger renew
                String cert = "-----BEGIN CERTIFICATE-----\n" +
                        "MIIB1jCCAT+gAwIBAgIJAMgniDRduPzqMA0GCSqGSIb3DQEBBQUAMC0xCzAJBgNV\n" +
                        "BAYTAkdCMQ8wDQYDVQQHEwZMb25kb24xDTALBgNVBAMTBFRlc3QwHhcNMTYxMTA5\n" +
                        "MDEwNjMwWhcNMTYxMTE5MDEwNjMwWjAtMQswCQYDVQQGEwJHQjEPMA0GA1UEBxMG\n" +
                        "TG9uZG9uMQ0wCwYDVQQDEwRUZXN0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB\n" +
                        "gQCZuzicKw/iV3XL+AEoNlBCm9Ssf7bEm5Fn0WFmmL04FFiU3SdiCV76PmI1lpaI\n" +
                        "Xf6u/mP26gLOWg0URkFTPlbq6u8SggOc8+lkqH24RSthjJm9SyziZdj/LCxNxLz7\n" +
                        "YF2NJyh13PLzqs1AFnodoYVJbFDCMQ6/T6YG1cPcRxLiGwIDAQABMA0GCSqGSIb3\n" +
                        "DQEBBQUAA4GBAIZrjaTgJxedR+ChsGUqWvVCejz1Vcjm6pmKKSucbsF3akTrJof4\n" +
                        "15p9JsU3zSyBt7g9y8v02JhksXNHKHhbVYpu35SR+u3YsHX0CYU6Ela6rqBnIkwA\n" +
                        "9gKN/wGTsip9Lzlk1+eV/kaYDfH96sw9Q/r+s9Q6FcZUePctnLS5zsJM\n" +
                        "-----END CERTIFICATE-----";
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("device-status", "activated")
                        .setHeader("mag-identifier", "test-device")
                        .setHeader("id-token", "dummy-idToken")
                        .setHeader("id-token-type", "dummy-idTokenType")
                        .setBody(cert);
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        // Repeat to trigger renew endpoint
        MASRequest request2 = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);
        assertNotNull(callback2.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback2.get().getResponseCode());

        //Make sure it has invoke renew endpoint
        assertNotNull(getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_RENEW));
    }

    @Test
    public void testDeregisterDeviceSuccess() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse deRegister() {
                return new MockResponse().setResponseCode(200);
            }
        });

        MASCallbackFuture<Void> callback2 = new MASCallbackFuture<>();
        MASDevice.getCurrentDevice().deregister(callback2);
        try {
            callback2.get();
        } catch (Exception e) {
            assertNotNull(e);
            Throwable cause = e.getCause().getCause().getCause();
            if (cause instanceof RegistrationServerException) {
                RegistrationServerException re = (RegistrationServerException) cause;
                assertEquals(re.getStatus(), 404);

                // Can't test the IOException portion, but we can verify the other failure case
                assertTrue(cause instanceof RegistrationServerException);
            }
        }

        // Verify the local credentials are cleared after a successful deregistration.
        StorageProvider sp = StorageProvider.getInstance();
        ClientCredentialContainer ccc = sp.getClientCredentialContainer();
        assertNotNull(ccc);
        assertNull(ccc.getMasterClientId());
        assertNull(ccc.getClientId());
        assertNull(ccc.getClientSecret());
        assertTrue(ccc.getClientExpiration() == -1);

        OAuthTokenContainer otc = sp.getOAuthTokenContainer();
        assertNotNull(otc);
        assertNull(otc.getAccessToken());
        assertNull(otc.getRefreshToken());
        assertTrue(otc.getExpiry() == 0);
        assertNull(otc.getGrantedScope());

        TokenManager tm = sp.getTokenManager();
        assertNotNull(tm);
        assertTrue(tm.getIdToken() == null && tm.getSecureIdToken() == null);
        assertNull(tm.getUserProfile());
        assertNull(tm.getClientPublicKey());
        assertNull(tm.getClientPrivateKey());
        assertNull(tm.getClientCertificateChain());
        assertNull(tm.getMagIdentifier());
    }

    @Test
    public void testDeregisterDeviceWithExceptionResponse() throws URISyntaxException, InterruptedException, IOException, ExecutionException {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse deRegister() {
                return new MockResponse()
                        .setResponseCode(404);
            }
        });

        MASCallbackFuture<Void> callback2 = new MASCallbackFuture<>();
        MASDevice.getCurrentDevice().deregister(callback2);
        try {
            callback2.get();
        } catch (Exception e) {
            assertNotNull(e);
            Throwable cause = e.getCause().getCause().getCause();
            if (cause instanceof RegistrationServerException) {
                RegistrationServerException re = (RegistrationServerException) cause;
                assertEquals(re.getStatus(), 404);

                // Can't test the IOException portion, but we can verify the other failure case
                assertTrue(cause instanceof RegistrationServerException);
            }
        }

        // Verify the local credentials are still retained after a failed deregistration.
        StorageProvider sp = StorageProvider.getInstance();
        ClientCredentialContainer ccc = sp.getClientCredentialContainer();
        assertNotNull(ccc);
        assertFalse(ccc.getMasterClientId().isEmpty());
        assertFalse(ccc.getClientId().isEmpty());
        assertFalse(ccc.getClientSecret().isEmpty());
        assertFalse(ccc.getClientExpiration() == -1);

        OAuthTokenContainer otc = sp.getOAuthTokenContainer();
        assertNotNull(otc);
        assertFalse(otc.getAccessToken().isEmpty());
        assertFalse(otc.getRefreshToken().isEmpty());
        assertFalse(otc.getExpiry() == 0);
        assertFalse(otc.getGrantedScope().isEmpty());

        TokenManager tm = sp.getTokenManager();
        assertNotNull(tm);
        assertTrue(tm.getIdToken() != null || tm.getSecureIdToken() != null);
        assertNotNull(tm.getUserProfile());
        assertNotNull(tm.getClientPublicKey());
        assertNotNull(tm.getClientPrivateKey());
        assertNotNull(tm.getClientCertificateChain());
        assertNotNull(tm.getMagIdentifier());
    }

    @Ignore(value = "Due to DE363094")
    public void testInvalidMAGIdentifierDuringRegistration() throws InterruptedException, ExecutionException {

        final boolean[] override = {true};
        final int expectedErrorCode = 1000107;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given mag-identifier is either invalid or it points to an unknown device\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.registerDeviceResponse(request);
                }
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        Assert.assertNotNull(callback.get());
    }

    @Test
    public void testInvalidClientCredentialDuringRegistration() throws InterruptedException, ExecutionException {

        final boolean[] override = {true};
        final int expectedErrorCode = 1000201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given client credentials were not valid\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.registerDeviceResponse(request);
                }
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        Assert.assertNotNull(callback.get());
    }

    @Test
    public void testInvalidClientCertificateDuringRegistration() throws InterruptedException, ExecutionException {

        final boolean[] override = {true};
        final int expectedErrorCode = 1000206;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The given client certificate has expired\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                if (override[0]) {
                    override[0] = false; //for retry
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                            .setHeader("x-ca-err", expectedErrorCode)
                            .setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.registerDeviceResponse(request);
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
