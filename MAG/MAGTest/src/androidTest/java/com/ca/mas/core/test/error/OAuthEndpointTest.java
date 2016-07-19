/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.error;

import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.AuthenticationException;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.oauth.OAuthException;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.core.request.internal.OAuthTokenRequest;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.test.BaseTest;
import com.ca.mas.core.test.DefaultDispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class OAuthEndpointTest extends BaseTest {

    @Test
    public void getAccessTokenWithInvalidClientCredentials() throws InterruptedException {
        final boolean[] override = {true};
        final int expectedErrorCode = 3003201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_client\", \"error_description\":\"The client could not be authenticated due to missing or invalid credentials\" }";

        ssg.setDispatcher(new DefaultDispatcher() {

            @Override
            protected MockResponse retrieveTokenResponse() {
                if (override[0]) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).setHeader("x-ca-err", expectedErrorCode).setBody(expectedErrorMessage);
                } else {
                    return super.retrieveTokenResponse();
                }
            }
        });

        final MAGError[] result = new MAGError[1];
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                        override[0] = false;
                        result[0] = error;
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });

        processRequest(new OAuthTokenRequest());
        assertTrue(result[0].getCause() instanceof OAuthServerException);
        assertEquals(expectedErrorCode, ((MAGServerException) result[0].getCause()).getErrorCode());
        assertEquals(expectedErrorMessage, result[0].getMessage());

    }

    @Test
    public void getAccessTokenWithInvalidResourceOwner() throws InterruptedException {

        final boolean[] override = {true};
        final int expectedErrorCode = 3003202;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The resource owner could not be authenticated due to missing or invalid credentials\" }";

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                if (override[0]) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).setHeader("x-ca-err", expectedErrorCode).setBody(expectedErrorMessage);
                } else {
                    return super.retrieveTokenResponse();
                }
            }
        });

        final MAGError[] result = new MAGError[1];
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                        override[0] = false;
                        result[0] = error;
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });

        processRequest(new OAuthTokenRequest());
        assertTrue(result[0].getCause() instanceof AuthenticationException);
        assertEquals(expectedErrorCode, ((MAGServerException) result[0].getCause()).getErrorCode());
        assertEquals(expectedErrorMessage, result[0].getMessage());


    }

    @Test
    public void getAccessTokenWithNoAccessToken() throws InterruptedException {

        final boolean[] override = {true};

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                if (override[0]) {
                    String token = "{\n" +
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
        final CountDownLatch latch = new CountDownLatch(2);

        final MAGError[] result = new MAGError[1];
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                        override[0] = false;
                        result[0] = error;
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });

        processRequest(new OAuthTokenRequest());
        assertTrue(result[0].getCause() instanceof OAuthException);

    }

}
