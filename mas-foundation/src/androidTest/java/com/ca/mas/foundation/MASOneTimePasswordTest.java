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
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.json.JSONObject;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASOneTimePasswordTest extends MASLoginTestBase {

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
        final boolean success[] = {false};

        MAS.setAuthenticationListener(new MASAuthenticationListener() {

            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                handler.deliver("EMAIL", new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        success[0] = true;
                        handler.proceed(getContext(), "1234");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertTrue(success[0]);


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
    public void otpExpiredFlowTest() throws URISyntaxException, InterruptedException {
        final int errorCode = 8000143;
        setDispatcher(new GatewayDefaultDispatcher() {
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

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                handler.deliver("EMAIL", new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        handler.proceed(getContext(), "1234");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof TargetApiException);
            assertEquals(Integer.toString(errorCode), ((List) ((TargetApiException) e.getCause().getCause()).getResponse().getHeaders().get("x-ca-err")).get(0));
        }
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
        final int errorCode = 8000145;
        setDispatcher(new GatewayDefaultDispatcher() {
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
        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                handler.deliver("EMAIL", new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        handler.proceed(getContext(), "1234");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

            }
        });
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof TargetApiException);
            assertEquals(Integer.toString(errorCode), ((List) ((TargetApiException) e.getCause().getCause()).getResponse().getHeaders().get("x-ca-err")).get(0));
        }
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
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse otpProtectedResponse() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                        .setBody("{\"error\":\"internal_error\"," +
                                "\"error_description\":\"Internal Server Error.Contact System Administrator\"}");
            }
        });
        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                handler.deliver("EMAIL", new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        handler.proceed(getContext(), "1234");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }
                });

            }
        });
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof TargetApiException);
        }
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
        final boolean success[] = {false};
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse generateOtp() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_FORBIDDEN)
                        .setHeader("X-OTP", "suspended")
                        .setHeader("X-OTP-RETRY", "-1")
                        .setHeader("X-OTP-RETRY-INTERVAL", "100")
                        .setHeader("x-ca-err", 8000145);

            }
        });

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                handler.deliver("EMAIL", new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        handler.proceed(getContext(), "1234");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if ((e.getCause() instanceof TargetApiException) &&
                                (HttpURLConnection.HTTP_FORBIDDEN == ((TargetApiException) e.getCause()).getResponse().getResponseCode())) {
                            success[0] = true;
                            MAS.cancelAllRequests();
                        }


                    }
                });

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL))
                .notifyOnCancel()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof MAS.RequestCancelledException);
            assertTrue(success[0]);
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
        final boolean success[] = {false};
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse generateOtp() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .setHeader("x-ca-err", 8000400);
            }
        });

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                handler.deliver("EMAIL", new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        handler.proceed(getContext(), "1234");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if ((e.getCause() instanceof TargetApiException) &&
                                (HttpURLConnection.HTTP_BAD_REQUEST == ((TargetApiException) e.getCause()).getResponse().getResponseCode())) {
                            success[0] = true;
                            MAS.cancelAllRequests();
                        }
                    }
                });
            }
        });
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL))
                .notifyOnCancel()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof MAS.RequestCancelledException);
            assertTrue(success[0]);
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
        final boolean success[] = {false};
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse generateOtp() {
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                        .setHeader("x-ca-err", 8000500);
            }
        });

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                handler.deliver("EMAIL", new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        handler.proceed(getContext(), "1234");
                    }

                    @Override
                    public void onError(Throwable e) {
                        if ((e.getCause() instanceof TargetApiException) &&
                                (HttpURLConnection.HTTP_INTERNAL_ERROR == ((TargetApiException) e.getCause()).getResponse().getResponseCode())) {
                            success[0] = true;
                            MAS.cancelAllRequests();
                        }
                    }
                });
            }
        });
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL))
                .notifyOnCancel()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof MAS.RequestCancelledException);
            assertTrue(success[0]);
        }

    }

    @Test
    public void otpInvalidFlowTest() throws Exception {
        final boolean success[] = {false};
        final int errorCode = 8000142;
        setDispatcher(new GatewayDefaultDispatcher() {
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

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {

            }

            @Override
            public void onOtpAuthenticateRequest(Context context, final MASOtpAuthenticationHandler handler) {
                if (handler.isInvalidOtp()) {
                    OtpAuthenticationHandler h = getValue(handler, "handler",  OtpAuthenticationHandler.class);
                    String selectedChannels = getValue(h, "selectedChannels", String.class);
                    if ("EMAIL".equals(selectedChannels)) {
                        success[0] = true;
                    }
                    setDispatcher(new GatewayDefaultDispatcher() {
                        @Override
                        protected MockResponse otpProtectedResponse() {
                            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_OK)
                                    .setBody("SUCCESS");
                        }
                    });

                }
                handler.deliver("EMAIL", new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        handler.proceed(getContext(), "1234");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                });
            }
        });
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.OTP_PROTECTED_URL)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());
        assertTrue(success[0]);


    }

}
