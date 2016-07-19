/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.oauth;

import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.creds.ClientCredentials;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.oauth.GrantProvider;
import com.ca.mas.core.request.internal.OAuthTokenRequest;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.test.BaseTest;
import com.ca.mas.core.test.DefaultDispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class ClientCredentialsGrantTypeTest extends BaseTest {

    @Before
    public void before() throws Exception {
        super.before();
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
                return noIdTokenRegisterDeviceResponse();
            }
        });

        final CountDownLatch latch = new CountDownLatch(1);
        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                //Shouldn't ask for credential
                fail();
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });

    }

    @Test
    public void testClientCredentials() throws URISyntaxException, InterruptedException, IOException {

        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts").toURL())
                .clientCredential()
                .build();

        processRequest(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());
        if (useMockServer()) {
            ssg.takeRequest(); //Client Credentials Request

            //Make sure the register request doesn't contain authorization header
            RecordedRequest registerRequest = ssg.takeRequest();
            assertNull(registerRequest.getHeader("authorization"));

            //Make sure the access token request use client_credentials grant type
            RecordedRequest accessTokenRequest = ssg.takeRequest();
            String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
            assertTrue(s.contains("grant_type=" + new ClientCredentials().getGrantType()));
        }
    }

    @Test
    public void testClientCredentialsWithOAuthRequest() throws URISyntaxException, InterruptedException, IOException {

        MAGRequest request = new OAuthTokenRequest() {
            @Override
            public GrantProvider getGrantProvider() {
                return GrantProvider.CLIENT_CREDENTIALS;
            }
        };

        processRequest(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (useMockServer()) {
            ssg.takeRequest(); //Client Credentials Request

            //Make sure the register request doesn't contain authorization header
            RecordedRequest registerRequest = ssg.takeRequest();
            assertNull(registerRequest.getHeader("authorization"));

            //Make sure the access token request use client_credentials grant type
            RecordedRequest accessTokenRequest = ssg.takeRequest();
            String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
            assertTrue(s.contains("grant_type=" + new ClientCredentials().getGrantType()));
        }
    }

    protected MockResponse noIdTokenRegisterDeviceResponse() {

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
