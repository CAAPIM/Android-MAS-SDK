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
import com.ca.mas.core.clientcredentials.ClientCredentialsServerException;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.request.internal.OAuthTokenRequest;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.test.BaseTest;
import com.ca.mas.core.test.DefaultDispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MAGEndpointTest extends BaseTest {

    @Before
    public void before() throws Exception {
        super.before();
        assumeMockServer();
    }

    @Test
    public void initializeClientCredentialsTest() throws InterruptedException {
        final int expectedErrorCode = 1002201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The client could not be authenticated due to missing or invalid client_id\" }";

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse initializeResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).setHeader("x-ca-err", expectedErrorCode).setBody(expectedErrorMessage);
            }
        });

        processRequest(new OAuthTokenRequest());
        assertTrue(error.getCause() instanceof ClientCredentialsServerException);
        assertEquals(expectedErrorCode, ((MAGServerException) error.getCause()).getErrorCode());
        assertEquals(expectedErrorMessage, error.getMessage());

    }

    @Test
    public void registerWithInvalidClientCredentials() throws InterruptedException {
        final boolean[] override = {true};
        final int expectedErrorCode = 1000201;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The client could not be authenticated due to missing or invalid credentials\" }";

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
                if (override[0]) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).setHeader("x-ca-err", expectedErrorCode).setBody(expectedErrorMessage);
                } else {
                    return super.registerDeviceResponse();
                }
            }
        });

        final MAGError[] registrationError = new MAGError[1];

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
                        registrationError[0] = error;
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

        assertEquals(expectedErrorCode, ((MAGServerException) registrationError[0].getCause()).getErrorCode());
        assertEquals(expectedErrorMessage, registrationError[0].getMessage());

    }

    @Test
    public void registerWithInvalidResourceOwner() throws InterruptedException {

        final boolean[] override = {true};
        final int expectedErrorCode = 1000202;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The resource owner could not be authenticated due to missing or invalid credentials\" }";

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
                if (override[0]) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).setHeader("x-ca-err", expectedErrorCode).setBody(expectedErrorMessage);
                } else {
                    return super.registerDeviceResponse();
                }
            }
        });
        final MAGError[] registrationError = new MAGError[1];

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
                        registrationError[0] = error;
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

        assertTrue(registrationError[0].getCause() instanceof AuthenticationException);
        assertEquals(expectedErrorCode, ((MAGServerException) registrationError[0].getCause()).getErrorCode());
        assertEquals(expectedErrorMessage, registrationError[0].getMessage());


    }

    @Test
    public void appEndpointError() throws Exception {
        final String expectedErrorMessage = "{ \"error\":\"This is App Error\" }";

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_FORBIDDEN).setBody(expectedErrorMessage);
            }
        });

        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts"))
                .responseBody(MAGResponseBody.stringBody())
                .build();

        processRequest(request);

        assertTrue(error.getCause() instanceof TargetApiException);
        TargetApiException e = (TargetApiException) error.getCause();
        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, e.getResponse().getResponseCode());
        assertEquals(expectedErrorMessage, (e.getResponse().getBody().getContent()));
    }
}
