/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.net.Uri;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.oauth.CodeVerifierCache;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASAuthorizationCodeFlowTest extends MASStartTestBase {

    private final String AUTH_CODE = "dummy_code";

    @Test
    public void testAccessProtectedEndpointWithAuthCodeWithoutPKCE() throws URISyntaxException, InterruptedException, IOException, ExecutionException {

        MAS.enablePKCE(false);
        assertFalse(MAS.isPKCEEnabled());
        final boolean[] success = {false};
        final String[] codeVerifier = new String[]{null};
        final MASCallbackFuture<MASUser> loginCallback = new MASCallbackFuture<>();

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                try {
                    RecordedRequest authorizeRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_AUTHORIZE);
                    Uri uri = Uri.parse(authorizeRequest.getPath());
                    String codeChallenge = uri.getQueryParameter("code_challenge");
                    String codeChallengeMethod = uri.getQueryParameter("code_challenge_method");
                    String state = uri.getQueryParameter("state");
                    codeVerifier[0] = getValue(CodeVerifierCache.getInstance(), "codeVerifier", String.class);
                    if (codeChallenge == null && codeChallengeMethod == null && state == null) {
                        success[0] = true;
                    }

                    MASUser.login(new MASAuthorizationResponse(AUTH_CODE, state), loginCallback);

                } catch (InterruptedException e) {
                }
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        callback.get();

        //Make sure the register request contain authorization header
        RecordedRequest registerRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER);
        assertNotNull(registerRequest.getHeader("authorization"));
        String authHeader = registerRequest.getHeader("authorization");
        assertEquals(authHeader, "Bearer " + AUTH_CODE);

        //Make sure the access token request use id-token grant type
        RecordedRequest accessTokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
        assertTrue(s.contains("assertion=" + GatewayDefaultDispatcher.ID_TOKEN));
        assertTrue(s.contains("grant_type=" + GatewayDefaultDispatcher.ID_TOKEN_TYPE));

        loginCallback.get();
        MASCallbackFuture<Void> logoutCallback = new MASCallbackFuture<>();
        MASUser.getCurrentUser().logout(logoutCallback);
        logoutCallback.get();

        MASRequest request2 = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);
        callback2.get();

        accessTokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String body = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
        assertFalse(body.contains("code_verifier=" + codeVerifier[0]));
        assertTrue(success[0]);

        MAS.enablePKCE(true);


    }

    @Test
    public void testAccessProtectedEndpointWithAuthCodeWithPKCE() throws URISyntaxException, InterruptedException, IOException, ExecutionException {

        MAS.enablePKCE(true);
        final String[] codeVerifier = new String[]{null};
        final boolean[] success = {false};
        final MASCallbackFuture<MASUser> loginCallback = new MASCallbackFuture<>();

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                try {
                    RecordedRequest authorizeRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_AUTHORIZE);
                    Uri uri = Uri.parse(authorizeRequest.getPath());
                    String codeChallenge = uri.getQueryParameter("code_challenge");
                    String codeChallengeMethod = uri.getQueryParameter("code_challenge_method");
                    String state = uri.getQueryParameter("state");
                    codeVerifier[0] = getValue(CodeVerifierCache.getInstance(), "codeVerifier", String.class);
                    if (codeChallenge != null && codeChallengeMethod != null && state != null) {
                        success[0] = true;
                    }

                    MASUser.login(new MASAuthorizationResponse(AUTH_CODE, state), loginCallback);

                } catch (InterruptedException e) {
                }
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        callback.get();

        //Make sure the register request contain authorization header
        RecordedRequest registerRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER);
        assertNotNull(registerRequest.getHeader("authorization"));
        String authHeader = registerRequest.getHeader("authorization");
        assertEquals(authHeader, "Bearer " + AUTH_CODE);

        //Make sure the access token request use id-token grant type
        RecordedRequest accessTokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
        assertTrue(s.contains("assertion=" + GatewayDefaultDispatcher.ID_TOKEN));
        assertTrue(s.contains("grant_type=" + GatewayDefaultDispatcher.ID_TOKEN_TYPE));

        loginCallback.get();
        MASCallbackFuture<Void> logoutCallback = new MASCallbackFuture<>();
        MASUser.getCurrentUser().logout(logoutCallback);
        logoutCallback.get();

        MASRequest request2 = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);
        callback2.get();

        accessTokenRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_TOKEN);
        String body = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
        assertTrue(body.contains("code_verifier=" + codeVerifier[0]));
        assertTrue(success[0]);

    }

    @Test
    public void testRegistrationWithAuthCodeWithPKCE() throws URISyntaxException, InterruptedException, IOException, ExecutionException {

        MAS.enablePKCE(true);
        final boolean[] success = {false};
        final String[] codeVerifier = new String[]{null};
        final MASCallbackFuture<MASUser> loginCallback = new MASCallbackFuture<>();


        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                try {
                    RecordedRequest authorizeRequest = getRecordRequest(GatewayDefaultDispatcher.AUTH_OAUTH_V2_AUTHORIZE);
                    Uri uri = Uri.parse(authorizeRequest.getPath());
                    String codeChallenge = uri.getQueryParameter("code_challenge");
                    String codeChallengeMethod = uri.getQueryParameter("code_challenge_method");
                    String state = uri.getQueryParameter("state");
                    codeVerifier[0] = getValue(CodeVerifierCache.getInstance(), "codeVerifier", String.class);
                    if (codeChallenge != null && codeChallengeMethod != null && state != null) {
                        success[0] = true;
                    }

                    MASUser.login(new MASAuthorizationResponse(AUTH_CODE, state), loginCallback);

                } catch (InterruptedException e) {
                }
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        callback.get();

        loginCallback.get();
        //Make sure the register request contain authorization header
        RecordedRequest registerRequest = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER);
        assertNotNull(registerRequest.getHeader("authorization"));
        String authHeader = registerRequest.getHeader("authorization");
        assertEquals(authHeader, "Bearer " + AUTH_CODE);
        String registerCodeVerifier = registerRequest.getHeader("code-verifier");
        assertEquals(registerCodeVerifier, codeVerifier[0] );
        assertTrue(success[0]);

    }

}
