/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.oauth;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.creds.AuthorizationCodeCredentials;
import com.ca.mas.core.creds.Credentials;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.service.MssoService;
import com.ca.mas.core.test.BaseTest;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

@RunWith(AndroidJUnit4.class)
public class AuthorizationCodeFlowTest extends BaseTest {

    private final String AUTH_CODE = "dummy_code";

    @Test
    public void testAccessProtectedEndpointWithAuthCode() throws URISyntaxException, InterruptedException, IOException {

        assumeMockServer();

        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {
                Credentials authorizationCodeCreds = new AuthorizationCodeCredentials(AUTH_CODE);
                Intent intent = new Intent(MssoIntents.ACTION_CREDENTIALS_OBTAINED, null,
                        InstrumentationRegistry.getInstrumentation().getTargetContext(), MssoService.class);
                intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
                intent.putExtra(MssoIntents.EXTRA_CREDENTIALS, authorizationCodeCreds);
                InstrumentationRegistry.getInstrumentation().getTargetContext().startService(intent);
            }

            @Override
            public void onOtpAuthenticationRequest(OtpAuthenticationHandler otpAuthenticationHandler) {

            }
        });


        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts").toURL())
                .password()
                .build();

        processRequest(request);

        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        ssg.takeRequest(); //Authorize Request
        ssg.takeRequest(); //Client Credentials Request

        //Make sure the register request contain authorization header
        RecordedRequest registerRequest = ssg.takeRequest();
        assertNotNull(registerRequest.getHeader("authorization"));
        String authHeader = registerRequest.getHeader("authorization");
        assertEquals(authHeader, "Bearer " + AUTH_CODE );

        //Make sure the access token request use id-token grant type
        RecordedRequest accessTokenRequest = ssg.takeRequest();
        String s = new String(accessTokenRequest.getBody().readByteArray(),"US-ASCII");
        assertTrue(s.contains("assertion=" + getIdToken()));
        assertTrue(s.contains("grant_type=" + getIdTokenType()));
    }
}
