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
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.request.internal.OAuthTokenRequest;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.test.BaseTest;
import com.ca.mas.core.test.DefaultDispatcher;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.JWTExpiredException;
import com.ca.mas.core.token.JWTInvalidAUDException;
import com.ca.mas.core.token.JWTInvalidAZPException;
import com.ca.mas.core.token.JWTInvalidSignatureException;
import com.ca.mas.core.token.JWTValidation;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.Date;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class JWTValidationTest extends BaseTest {

    @Before
    public void before() throws Exception {
        super.before();
        assumeMockServer();
    }

    //Mock response for device registration
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

    @Test
    public void invalidSignature() throws InterruptedException {

        final Boolean[] returnError = {true};

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
                if (returnError[0]) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("device-status", "activated")
                            .setHeader("mag-identifier", "test-device")
                            .setHeader("id-token", "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDI0MDA4Nzg1OTEsCiAiYXpwIjogInRlc3QtZGV2aWNlIiwKICJzdWIiOiAieCIsCiAiYXVkIjogImR1bW15IiwKICJpc3MiOiAiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsCiAiaWF0IjogMTQwMDg3ODU5MQp9.zenKvXlhDtpXym_auPCbukBiVqr3rqZrcoeDyfsvftA")
                            .setHeader("id-token-type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                            .setBody(cert);
                } else {
                    return super.registerDeviceResponse();
                }
            }
        });

        final boolean[] result = {false};

        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                        result[0] = (error.getCause() instanceof JWTInvalidSignatureException);
                        returnError[0] = false;
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
        assertTrue(result[0]);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

    }

    @Test
    public void invalidAud() throws InterruptedException {

        final Boolean[] override = {true};

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse initializeResponse() {
                if (override[0]) {
                    String result = "{\"client_id\":\"8298bc51-f242-4c6d-b547-d1d8e8519cb5\", \"client_secret\":\"dummy\", \"client_expiration\":" + new Date().getTime() + 36000 + "}";
                    return new MockResponse().setResponseCode(200).setBody(result);
                } else {
                    return super.initializeResponse();
                }
            }

            @Override
            protected MockResponse registerDeviceResponse() {
                if (override[0]) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("device-status", "activated")
                            .setHeader("mag-identifier", "test-device")
                            .setHeader("id-token", "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDI0MDA4Nzg1OTEsCiAiYXpwIjogInRlc3QtZGV2aWNlIiwKICJzdWIiOiAieCIsCiAiYXVkIjogImR1bW15IiwKICJpc3MiOiAiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsCiAiaWF0IjogMTQwMDg3ODU5MQp9.zenKvXlhDtpXym_auPCbukBiVqr3rqZrcoeDyfsvftA")
                            .setHeader("id-token-type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                            .setBody(cert);
                } else {
                    return super.registerDeviceResponse();
                }
            }
        });

        final boolean[] result = {false};
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                        result[0] = (error.getCause() instanceof JWTInvalidAUDException);
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

        processRequest(new OAuthTokenRequest());
        assertTrue(result[0]);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

    }

    @Test
    public void invalidAzp() throws InterruptedException {

        final Boolean[] override = {true};


        ssg.setDispatcher(new DefaultDispatcher() {

            @Override
            protected MockResponse registerDeviceResponse() {
                if (override[0]) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("device-status", "activated")
                            .setHeader("mag-identifier", "dummy-device")
                            .setHeader("id-token", "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDI0MDA4Nzg1OTEsCiAiYXpwIjogInRlc3QtZGV2aWNlIiwKICJzdWIiOiAieCIsCiAiYXVkIjogImR1bW15IiwKICJpc3MiOiAiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsCiAiaWF0IjogMTQwMDg3ODU5MQp9.zenKvXlhDtpXym_auPCbukBiVqr3rqZrcoeDyfsvftA")
                            .setHeader("id-token-type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                            .setBody(cert);
                } else {
                    return super.registerDeviceResponse();
                }
            }
        });

        final boolean[] result = {false};
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                        result[0] = (error.getCause() instanceof JWTInvalidAZPException);
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

        processRequest(new OAuthTokenRequest());
        assertTrue(result[0]);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

    }

    @Test
    public void invalidExp() throws InterruptedException {

        final Boolean[] override = {true};

        ssg.setDispatcher(new DefaultDispatcher() {

            @Override
            protected MockResponse initializeResponse() {
                if (override[0]) {
                    String result = "{\"client_id\":\"8298bc51-f242-4c6d-b547-d1d8e8519cb4\", \"client_secret\":\"dummy\", \"client_expiration\":" + new Date().getTime() + 36000 + "}";
                    return new MockResponse().setResponseCode(200).setBody(result);
                } else {
                    return super.initializeResponse();
                }
            }

            @Override
            protected MockResponse registerDeviceResponse() {
                if (override[0]) {
                    return new MockResponse()
                            .setResponseCode(200)
                            .setHeader("device-status", "activated")
                            .setHeader("mag-identifier", "d9b38b22-14a2-4573-80b7-b95de92b18he")
                            .setHeader("id-token", "eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDE0MDA3OTQ2MjEsCiAiYXpwIjogImQ5YjM4YjIyLTE0YTItNDU3My04MGI3LWI5NWRlOTJiMThoZSIsCiAic3ViIjogIngiLAogImF1ZCI6ICI4Mjk4YmM1MS1mMjQyLTRjNmQtYjU0Ny1kMWQ4ZTg1MTljYjQiLAogImlzcyI6ICJodHRwOi8vbS5sYXllcjd0ZWNoLmNvbS9jb25uZWN0IiwKICJpYXQiOiAxNDAwNzk0NjIxCn0.H4Yvz9d-uzoWGWeshgYTFLm110B1M1pb63vrwrJsIIg")
                            .setHeader("id-token-type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
                            .setBody(cert);
                } else {
                    return super.registerDeviceResponse();
                }
            }
        });

        final boolean[] result = {false};
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                mobileSso.authenticate(getUsername(), getPassword(), new MAGResultReceiver() {
                    @Override
                    public void onSuccess(MAGResponse response) {
                    }

                    @Override
                    public void onError(MAGError error) {
                        result[0] = (error.getCause() instanceof JWTExpiredException);
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
        processRequest(new OAuthTokenRequest());
        assertTrue(result[0]);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

    }

    @Test
    public void testExpiredToken() throws Exception {

        IdToken idToken = new IdToken("eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDE0MDA3OTQ2MjEsCiAiYXpwIjogImQ5YjM4YjIyLTE0YTItNDU3My04MGI3LWI5NWRlOTJiMThoZSIsCiAic3ViIjogIngiLAogImF1ZCI6ICI4Mjk4YmM1MS1mMjQyLTRjNmQtYjU0Ny1kMWQ4ZTg1MTljYjQiLAogImlzcyI6ICJodHRwOi8vbS5sYXllcjd0ZWNoLmNvbS9jb25uZWN0IiwKICJpYXQiOiAxNDAwNzk0NjIxCn0.H4Yvz9d-uzoWGWeshgYTFLm110B1M1pb63vrwrJsIIg", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        assertTrue(JWTValidation.isIdTokenExpired(idToken));


        idToken = new IdToken("eyJ0eXAiOiJKV1QiLA0KICJhbGciOiJIUzI1NiJ9.ewogImV4cCI6IDI0MDA4Nzg1OTEsCiAiYXpwIjogInRlc3QtZGV2aWNlIiwKICJzdWIiOiAieCIsCiAiYXVkIjogImR1bW15IiwKICJpc3MiOiAiaHR0cDovL20ubGF5ZXI3dGVjaC5jb20vY29ubmVjdCIsCiAiaWF0IjogMTQwMDg3ODU5MQp9.zenKvXlhDtpXym_auPCbukBiVqr3rqZrcoeDyfsvftA", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        assertFalse(JWTValidation.isIdTokenExpired(idToken));



    }
}
