/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.UiThread;
import android.util.Log;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.auth.MASAuthenticationProvider;

import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.CodeVerifierUtil;

import java.net.MalformedURLException;
import java.net.URL;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * MASCustomTabs is a Custom Tabs implementation utilizing the AppAuth library (https://openid.github.io/AppAuth-Android/).
 */
public class MASCustomTabs {

    /**
     * Converts query redirect parameters from the gateway to construct an AppAuth request
     * for the social provider.
     *
     * @param context
     * @param provider
     */
    @UiThread
    public static void socialLogin(final Context context, MASAuthenticationProvider provider) {
        provider.getAuthConfiguration(context, provider, new MASCallback<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                //Extract request parameters from the redirect URL
                String clientId = uri.getQueryParameter("client_id");
                String redirectUri = uri.getQueryParameter("redirect_uri");
                String scope = uri.getQueryParameter("scope");
                //This is the gateway state that will be provided to AppAuth
                String state = uri.getQueryParameter("state");
                String responseType = uri.getQueryParameter("response_type");
                String codeChallenge = uri.getQueryParameter("code_challenge");
                String codeChallengeMethod = uri.getQueryParameter("code_challenge_method");

                try {
                    URL url = new URL(uri.toString());
                    String configuration = url.getProtocol() + "://" + url.getAuthority() + url.getPath();
                    Uri authEndpoint = Uri.parse(configuration);
                    Uri tokenEndpoint = Uri.parse("");

                    AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
                            authEndpoint, tokenEndpoint, null);
                    if (redirectUri != null) {
                        AuthorizationRequest.Builder builder = new AuthorizationRequest
                                .Builder(config, clientId, responseType, Uri.parse(redirectUri))
                                .setState(state)
                                .setScopes(scope);

                        //PKCE social login support for MAG
                        if (codeChallenge != null) {
                            builder.setCodeVerifier(
                                    //The code verifier is stored on the MAG Server,
                                    //this is only for passing the code verifier check for AppAuth.
                                    //The code verifier will not be used for retrieving the Access Token.
                                    CodeVerifierUtil.generateRandomCodeVerifier(),
                                    codeChallenge,
                                    codeChallengeMethod);
                        } else {
                            builder.setCodeVerifier(null);
                        }

                        AuthorizationRequest req = builder.build();

                        Intent postAuthIntent = new Intent(context, MASOAuthRedirectActivity.class);
                        Intent authCanceledIntent = new Intent(context, MASFinishActivity.class);

                        AuthorizationService service = new AuthorizationService(context);
                        service.performAuthorizationRequest(req,
                                PendingIntent.getActivity(context, req.hashCode(), postAuthIntent, 0),
                                PendingIntent.getActivity(context, req.hashCode(), authCanceledIntent, 0));
                    } else {
                        if (DEBUG) Log.d(TAG, "No redirect URL detected.");
                    }
                } catch (MalformedURLException e) {
                    if (DEBUG) Log.d(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {
                if (DEBUG) Log.d(TAG, e.getMessage());
            }
        });
    }
}
