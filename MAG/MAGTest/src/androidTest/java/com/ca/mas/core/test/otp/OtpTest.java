/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.test.otp;

import android.content.Context;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.test.BaseTest;
import com.ca.mas.core.test.DefaultDispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;


/*
OtpTest class tests for all of the OTP related scenarios
 */
@RunWith(AndroidJUnit4.class)
public class OtpTest extends BaseTest {
    Context context;


    /*
    Test Case 1 : Happy flow test case.
    It first sends a MAG request to /otpProtected end point which is super protected.
    Mockserver will return otp delivery channels.
    Then test case will sends a MAG request to /auth/otp end point with the otp delivery channel.
    Mockserver will return Success.
    It again sends a MAG request to /otpProtected end point which is super protected with the X-OTP header with a otp value.
    Mockserver will return Success.
    */
    @Test
    public void otpPositiveFlowTest() throws Exception {
        context = this.getContext();
        final boolean success[] = {false, false};
        MAGRequest request = null;
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(final OtpAuthenticationHandler otpAuthenticationHandler) {
                success[0] = true;
                otpAuthenticationHandler.deliver("EMAIL", new MAGResultReceiver<Void>() {
                    @Override
                    public void onSuccess(MAGResponse<Void> response) {
                        success[1] = true;
                        otpAuthenticationHandler.proceed(context, "1234");
                    }

                    @Override
                    public void onError(MAGError error) {

                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/otpProtected")).build();
        processRequest(request);
        for (int i = 0; i < success.length; i++) {
            assertTrue(success[i]);
        }
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
    }

    /*
    Test Case 2 : OTP provided has expired.
    It first sends a MAG request to /otpProtected end point which is super protected.
    Mockserver will return otp delivery channels.
    Then test case will sends a MAG request to /auth/otp end point with the otp delivery channel.
    Mockserver will return Success.
    It again sends a MAG request to /otpProtected end point which is super protected with the X-OTP header with a otp value.
    Mockserver will return otp expired error.
    */

    @Test
    public void otpExpiredFlowTest() throws Exception {
        context = this.getContext();
        final int errorCode = 8000143;
        MAGRequest request = null;
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse otpProtectedResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .setHeader("X-OTP", "expired")
                        .setHeader("X-OTP-RETRY", "1")
                        .setHeader("x-ca-err", errorCode)
                        .setBody("{\"error\":\"otp_expired\"," +
                                "\"error_description\":\"The one-time password.\"}");
            }
        });
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(final OtpAuthenticationHandler otpAuthenticationHandler) {
                otpAuthenticationHandler.deliver("EMAIL", new MAGResultReceiver<Void>() {
                    @Override
                    public void onSuccess(MAGResponse<Void> response) {
                        otpAuthenticationHandler.proceed(context, "1234");
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/otpProtected")).build();
        processRequest(request);
        assertTrue(error.getCause() instanceof TargetApiException);
    }


    /*
    Test Case 3 : New OTP transaction is requested but the user in barred/suspended state
    It first sends a MAG request to /otpProtected end point which is super protected.
    Mockserver will return otp delivery channels.
    Then test case will sends a MAG request to /auth/otp end point with the otp delivery channel.
    Mockserver will return Success.
    It again sends a MAG request to /otpProtected end point which is super protected with the X-OTP header with a otp value.
    Mockserver will return user suspended error.
    */
    @Test
    public void otpUserSuspendedFlowTest() throws Exception {
        context = this.getContext();
        final int errorCode = 8000145;
        MAGRequest request = null;
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse otpProtectedResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
                        .setHeader("X-OTP", "suspended")
                        .setHeader("X-OTP-RETRY", "-1")
                        .setHeader("X-OTP-RETRY-INTERVAL", "100")
                        .setHeader("x-ca-err", errorCode)
                        .setBody("{\"error\":\"otp_suspended\"," +
                                "\"error_description\":\"Max Retry exceeded, you can try after 100 seconds.\"}");
            }
        });
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(final OtpAuthenticationHandler otpAuthenticationHandler) {
                otpAuthenticationHandler.deliver("EMAIL", new MAGResultReceiver<Void>() {
                    @Override
                    public void onSuccess(MAGResponse<Void> response) {
                        otpAuthenticationHandler.proceed(context, "1234");
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/otpProtected")).build();
        processRequest(request);
        assertTrue(error.getCause() instanceof TargetApiException);
    }

    /*
    Test Case 4 : Any other error caused due to Internal Server problems.
    It first sends a MAG request to /otpProtected end point which is super protected.
    Mockserver will return otp delivery channels.
    Then test case will sends a MAG request to /auth/otp end point with the otp delivery channel.
    Mockserver will return Success.
    It again sends a MAG request to /otpProtected end point which is super protected with the X-OTP header with a otp value.
    Mockserver will return Internal Server Error.
     */
    @Test
    public void otpServerErrorFlowTest() throws Exception {
        context = this.getContext();
        final int errorCode = 8000500;
        MAGRequest request = null;
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse otpProtectedResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                        .setBody("{\"error\":\"internal_error\"," +
                                "\"error_description\":\"Internal Server Error.Contact System Administrator\"}");
            }
        });
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(final OtpAuthenticationHandler otpAuthenticationHandler) {
                otpAuthenticationHandler.deliver("EMAIL", new MAGResultReceiver<Void>() {
                    @Override
                    public void onSuccess(MAGResponse<Void> response) {
                        otpAuthenticationHandler.proceed(context, "1234");
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/otpProtected")).build();
        processRequest(request);
        assertTrue(error.getCause() instanceof TargetApiException);
    }

    /*
    Test Case 5 : New OTP transaction is requested but the user is still in barred/suspended state.
    It first sends a MAG request to /otpProtected end point which is super protected.
    Mockserver will return otp delivery channels.
    Then test case will sends a MAG request to /auth/otp end point with the otp delivery channel.
    Mockserver will return otp suspended error.
    */
    @Test
    public void otpUserBarredFlowTest() throws Exception {
        context = this.getContext();
        final boolean success[] = {false, false};
        MAGRequest request = null;
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse generateOtp() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
                        .setHeader("X-OTP", "suspended")
                        .setHeader("X-OTP-RETRY", "-1")
                        .setHeader("X-OTP-RETRY-INTERVAL", "100")
                        .setHeader("x-ca-err", 8000145);

            }
        });
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(final OtpAuthenticationHandler otpAuthenticationHandler) {
                success[0] = true;
                otpAuthenticationHandler.deliver("EMAIL", new MAGResultReceiver<Void>() {
                    @Override
                    public void onSuccess(MAGResponse<Void> response) {
                        otpAuthenticationHandler.proceed(context, "1234");
                    }

                    @Override
                    public void onError(MAGError error) {
                        success[1] = true;
                        assertTrue(error.getCause() instanceof TargetApiException);
                        assertEquals(HttpURLConnection.HTTP_FORBIDDEN, ((TargetApiException) error.getCause()).getResponse().getResponseCode());
                        mobileSso.cancelRequest(requestId);
                    }

                    @Override
                    public void onRequestCancelled() {
                    }
                });
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/otpProtected")).build();
        processRequest(request);
        for (int i = 0; i < success.length; i++) {
            assertTrue(success[i]);
        }
    }

    /*
    Test Case 6 : Any other error which is caused by invalid user input.
    It first sends a MAG request to /otpProtected end point which is super protected.
    Mockserver will return otp delivery channels.
    Then test case will sends a MAG request to /auth/otp end point with the otp delivery channel.
    Mockserver will return invalid request error.
    */
    @Test
    public void otpErrorGeneratingFlowTest() throws Exception {
        context = this.getContext();
        final boolean success[] = {false, false};
        MAGRequest request = null;
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse generateOtp() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .setHeader("x-ca-err", 8000400);
            }
        });
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(final OtpAuthenticationHandler otpAuthenticationHandler) {
                success[0] = true;
                otpAuthenticationHandler.deliver("EMAIL", new MAGResultReceiver<Void>() {
                    @Override
                    public void onSuccess(MAGResponse<Void> response) {
                        otpAuthenticationHandler.proceed(context, "1234");
                    }

                    @Override
                    public void onError(MAGError error) {
                        success[1] = true;
                        assertTrue(error.getCause() instanceof TargetApiException);
                        assertEquals(HttpURLConnection.HTTP_BAD_REQUEST, ((TargetApiException) error.getCause()).getResponse().getResponseCode());
                        mobileSso.cancelRequest(requestId);

                    }

                    @Override
                    public void onRequestCancelled() {
                    }
                });
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/otpProtected")).build();
        processRequest(request);
        for (int i = 0; i < success.length; i++) {
            assertTrue(success[i]);
        }
    }

    /*
    Test Case 7 : Any other caused due to Internal Server problems.
    It first sends a MAG request to /otpProtected end point which is super protected.
    Mockserver will return otp delivery channels.
    Then test case will sends a MAG request to /auth/otp end point with the otp delivery channel.
    Mockserver will return internal error.
   */
    @Test
    public void otpInternalServerErrorGeneratingFlowTest() throws Exception {
        context = this.getContext();
        final boolean success[] = {false, false};
        MAGRequest request = null;
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse generateOtp() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                        .setHeader("x-ca-err", 8000500);
            }
        });
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(final OtpAuthenticationHandler otpAuthenticationHandler) {
                success[0] = true;
                otpAuthenticationHandler.deliver("EMAIL", new MAGResultReceiver<Void>() {
                    @Override
                    public void onSuccess(MAGResponse<Void> response) {
                        otpAuthenticationHandler.proceed(context, "1234");
                    }

                    @Override
                    public void onError(MAGError error) {
                        success[1] = true;
                        assertTrue(error.getCause() instanceof TargetApiException);
                        assertEquals(HttpURLConnection.HTTP_INTERNAL_ERROR, ((TargetApiException) error.getCause()).getResponse().getResponseCode());
                        mobileSso.cancelRequest(requestId);
                    }

                    @Override
                    public void onRequestCancelled() {
                    }
                });
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/otpProtected")).build();
        processRequest(request);
        for (int i = 0; i < success.length; i++) {
            assertTrue(success[i]);
        }
    }

    //TODO @Test
    public void otpInvalidFlowTest() throws Exception {
        context = this.getContext();
        final int errorCode = 8000142;
        MAGRequest request = null;
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse otpProtectedResponse() {
                return new MockResponse().setResponseCode(401)
                        .setHeader("X-OTP", "invalid")
                        .setHeader("X-OTP-RETRY", "3")
                        .setHeader("x-ca-err", errorCode)
                        .setBody("{\"error\":\"otp_expired\"," +
                                "\"error_description\":\"The one-time password.\"}");
            }
        });
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider authenticationProvider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }

            @Override
            public void onOtpAuthenticationRequest(final OtpAuthenticationHandler otpAuthenticationHandler) {
                otpAuthenticationHandler.deliver("EMAIL", new MAGResultReceiver<Void>() {
                    @Override
                    public void onSuccess(MAGResponse<Void> response) {
                        otpAuthenticationHandler.proceed(context, "1234");
                    }

                    @Override
                    public void onError(MAGError error) {
                    }

                    @Override
                    public void onRequestCancelled() {

                    }
                });
            }
        });
        request = new MAGRequest.MAGRequestBuilder(getURI("/otpProtected")).build();
        processRequest(request);
        assertTrue(error.getCause() instanceof TargetApiException);
    }
}
