/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.oauth;

import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.creds.AuthorizationCodeCredentials;
import com.ca.mas.core.creds.Credentials;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.oauth.CodeVerifierCache;
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

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

@Deprecated
@RunWith(AndroidJUnit4.class)
public class AuthorizationCodeFlowTest extends BaseTest {

    private final String AUTH_CODE = "dummy_code";

    @Deprecated
    @Test
    public void testAccessProtectedEndpointWithAuthCodeWithPKCE() throws URISyntaxException, InterruptedException, IOException {

        assumeMockServer();
        final String[] codeVerifier = new String[]{null};

        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {

                if (!mobileSso.isDeviceRegistered()) {
                    try {
                        ssg.takeRequest(); //initialize
                    } catch (InterruptedException e) {
                        fail();
                    }
                }
                RecordedRequest authorizeRequest = null;
                try {
                    authorizeRequest = ssg.takeRequest(); //authorize
                } catch (InterruptedException e) {
                    fail();
                }
                Uri uri = Uri.parse(authorizeRequest.getPath());
                String codeChallenge = uri.getQueryParameter("code_challenge");
                String codeChallengeMethod = uri.getQueryParameter("code_challenge_method");
                String state = uri.getQueryParameter("state");
                codeVerifier[0] = getValue(CodeVerifierCache.getInstance(), "codeVerifier", String.class);
                assertNotNull(codeChallenge);
                assertNotNull(codeChallengeMethod);
                assertNotNull(state);

                Credentials authorizationCodeCreds = new AuthorizationCodeCredentials(AUTH_CODE, state);
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

        ssg.takeRequest(); //api

        mobileSso.logout(true);
        ssg.takeRequest(); //logout

        //Invoke again to test the /token endpoint
        processRequest(request);

        accessTokenRequest = ssg.takeRequest(); //token
        String body = new String(accessTokenRequest.getBody().readByteArray(),"US-ASCII");
        assertTrue(body.contains("code_verifier="+codeVerifier[0]));

    }

    @Deprecated
    @Test
    public void testAccessProtectedEndpointWithAuthCodeWithoutPKCE() throws URISyntaxException, InterruptedException, IOException {

        assumeMockServer();
        ConfigurationManager.getInstance().enablePKCE(false);
        final String[] codeVerifier = new String[]{null};

        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {

                if (!mobileSso.isDeviceRegistered()) {
                    try {
                        ssg.takeRequest(); //initialize
                    } catch (InterruptedException e) {
                        fail();
                    }
                }
                RecordedRequest authorizeRequest = null;
                try {
                    authorizeRequest = ssg.takeRequest(); //authorize
                } catch (InterruptedException e) {
                    fail();
                }
                Uri uri = Uri.parse(authorizeRequest.getPath());
                String codeChallenge = uri.getQueryParameter("code_challenge");
                String codeChallengeMethod = uri.getQueryParameter("code_challenge_method");
                String state = uri.getQueryParameter("state");
                assertNull(codeChallenge);
                assertNull(codeChallengeMethod);
                assertNull(state);

                Credentials authorizationCodeCreds = new AuthorizationCodeCredentials(AUTH_CODE, state);
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

        ssg.takeRequest(); //api

        mobileSso.logout(true);
        ssg.takeRequest(); //logout

        //Invoke again to test the /token endpoint
        processRequest(request);

        accessTokenRequest = ssg.takeRequest(); //token
        String body = new String(accessTokenRequest.getBody().readByteArray(),"US-ASCII");
        assertFalse(body.contains("code_verifier="+codeVerifier[0]));
        //Reset
        ConfigurationManager.getInstance().enablePKCE(true);

    }

    @Deprecated
    @Test
    public void testRegistrationWithAuthCodeWithPKCE() throws URISyntaxException, InterruptedException, IOException {

        assumeMockServer();
        final String[] codeVerifier = new String[]{null};

        mobileSso.setMobileSsoListener(new MobileSsoListener() {
            @Override
            public void onAuthenticateRequest(long requestId, AuthenticationProvider provider) {

                if (!mobileSso.isDeviceRegistered()) {
                    try {
                        ssg.takeRequest(); //initialize
                    } catch (InterruptedException e) {
                        fail();
                    }
                }
                RecordedRequest authorizeRequest = null;
                try {
                    authorizeRequest = ssg.takeRequest(); //authorize
                } catch (InterruptedException e) {
                    fail();
                }
                Uri uri = Uri.parse(authorizeRequest.getPath());
                String codeChallenge = uri.getQueryParameter("code_challenge");
                String codeChallengeMethod = uri.getQueryParameter("code_challenge_method");
                String state = uri.getQueryParameter("state");
                codeVerifier[0] = getValue(CodeVerifierCache.getInstance(), "codeVerifier", String.class);
                assertNotNull(codeChallenge);
                assertNotNull(codeChallengeMethod);
                assertNotNull(state);

                Credentials authorizationCodeCreds = new AuthorizationCodeCredentials(AUTH_CODE, state);
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

        //Make sure the register request contain authorization header
        RecordedRequest registerRequest = ssg.takeRequest();
        assertNotNull(registerRequest.getHeader("authorization"));
        String authHeader = registerRequest.getHeader("authorization");
        String registerCodeVerifier = registerRequest.getHeader("code-verifier");
        assertEquals(authHeader, "Bearer " + AUTH_CODE );
        assertEquals(registerCodeVerifier, codeVerifier[0] );

    }
}
