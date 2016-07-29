/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test;

import android.os.ResultReceiver;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.AuthenticationException;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.request.internal.OAuthTokenRequest;
import com.ca.mas.core.service.AuthenticationProvider;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class AuthenticationTest extends BaseTest {

    @Test
    public void mobileSsoListenerTest() throws JSONException, InterruptedException {

        final MAGResponse[] result = new MAGResponse[1];


        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                        result[0] = response;
                    }

                    @Override
                    public void onError(MAGError error) {
                        fail();
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
        assertEquals(HttpURLConnection.HTTP_OK, result[0].getResponseCode());
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

    }

    @Test
    public void testCallbackWithAuthenticateFailed() throws JSONException, InterruptedException {

        final boolean[] override = {true};
        final int expectedErrorCode = 1000202;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The resource owner could not be authenticated due to missing or invalid credentials\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";
        final long[] reqId = {-1L};

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
                if (override[0]) {
                    return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).
                            setHeader("x-ca-err", expectedErrorCode).
                            setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
                } else {
                    return super.registerDeviceResponse();
                }
            }
        });

        final long[] actualRequestId = new long[1];
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
                        actualRequestId[0] = requestId;
                        result[0] = error;
                        override[0] = false;
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

        long expectedRequestId = processRequest(new OAuthTokenRequest());

        assertEquals(expectedRequestId, actualRequestId[0]);
        assertTrue(result[0].getCause() instanceof AuthenticationException);
        AuthenticationException e = (AuthenticationException) result[0].getCause();
        assertEquals(CONTENT_TYPE_VALUE, e.getContentType());
        assertEquals(expectedErrorCode, e.getErrorCode());
        assertEquals(expectedErrorMessage, e.getMessage());
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatus());

    }

    @Test
    public void testCallbackWithAuthenticateFailedAndCancel() throws JSONException, InterruptedException {

        final int expectedErrorCode = 1000202;
        final int[] failCount = {0};
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The resource owner could not be authenticated due to missing or invalid credentials\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).
                        setHeader("x-ca-err", expectedErrorCode).
                        setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
            }
        });
        final CountDownLatch countDownLatch = new CountDownLatch(2);

        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(final long requestId, AuthenticationProvider provider) {

                if (failCount[0] > 0) {
                    mobileSso.cancelRequest(requestId);
                    return;
                }

                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                        failCount[0] = failCount[0] + 1;
                        countDownLatch.countDown();
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
        //trigger the onRequestCancelled
        assertNull(response);
        assertNull(error);


    }

    @Test
    public void authenticateTest() throws InterruptedException {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final boolean[] success = {false};
        mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {

            @Override
            public void onSuccess(MAGResponse response) {
                mobileSso.processRequest(new OAuthTokenRequest(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                        success[0] = true;
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(MAGError error) {
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onRequestCancelled() {

                    }

                });
            }

            @Override
            public void onError(MAGError error) {
                countDownLatch.countDown();
            }

            @Override
            public void onRequestCancelled() {

            }

        });

        countDownLatch.await();
        assertTrue(success[0]);
    }

    @Test
    public void testAuthenticationFail() throws InterruptedException {

        final int expectedErrorCode = 1000202;
        final String expectedErrorMessage = "{ \"error\":\"invalid_request\", \"error_description\":\"The resource owner could not be authenticated due to missing or invalid credentials\" }";
        final String CONTENT_TYPE = "Content-Type";
        final String CONTENT_TYPE_VALUE = "application/json";

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED).
                        setHeader("x-ca-err", expectedErrorCode).
                        setHeader(CONTENT_TYPE, CONTENT_TYPE_VALUE).setBody(expectedErrorMessage);
            }
        });
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        final MAGError[] result = new MAGError[1];

        mobileSso.authenticate(getUsername(), "invalid".toCharArray(), new MAGResultReceiver<MAGResponse>() {

            @Override
            public void onSuccess(MAGResponse response) {
                countDownLatch.countDown();
            }

            @Override
            public void onError(MAGError error) {
                result[0] = error;
                countDownLatch.countDown();
            }

            @Override
            public void onRequestCancelled() {

            }
        });

        countDownLatch.await();
        assertTrue(result[0].getCause() instanceof AuthenticationException);
        AuthenticationException e = (AuthenticationException) result[0].getCause();
        assertEquals(CONTENT_TYPE_VALUE, e.getContentType());
        assertEquals(expectedErrorCode, e.getErrorCode());
        assertEquals(expectedErrorMessage, e.getMessage());
        assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, e.getStatus());

    }

    @Test(expected = NullPointerException.class)
    public void authenticateTestWithNullUsername() {
        mobileSso.authenticate(null, getPassword(), new ResultReceiver(null));
    }

    @Test(expected = NullPointerException.class)
    public void authenticateTestWithNullPassword() {
        mobileSso.authenticate(getUsername(), null, new ResultReceiver(null));
    }
}
