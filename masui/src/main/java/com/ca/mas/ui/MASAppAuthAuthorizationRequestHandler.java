/*
 *
 *  * Copyright (c) 2016 CA. All rights reserved.
 *  *
 *  * This software may be modified and distributed under the terms
 *  * of the MIT license.  See the LICENSE file for details.
 *  *
 *
 */

package com.ca.mas.ui;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.ArrayMap;
import android.util.Log;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.oauth.CodeVerifierCache;
import com.ca.mas.core.oauth.PKCE;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;

import com.ca.mas.foundation.MASAuthorizationRequest;
import com.ca.mas.foundation.MASAuthorizationRequestHandler;
import com.ca.mas.foundation.MASConfiguration;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.browser.BrowserBlacklist;
import net.openid.appauth.browser.Browsers;
import net.openid.appauth.browser.VersionRange;
import net.openid.appauth.browser.VersionedBrowserMatcher;

import java.net.URI;

import static com.ca.mas.core.oauth.OAuthClientUtil.generateCodeChallenge;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;


public class MASAppAuthAuthorizationRequestHandler implements MASAuthorizationRequestHandler {

    Context context;
    Intent completeIntent, cancelIntent;

    public MASAppAuthAuthorizationRequestHandler(Context context, Intent completeIntent, Intent cancelIntent) {

        this.context = context;
        this.completeIntent = completeIntent;
        this.cancelIntent = cancelIntent;

    }

    @Override
    public void authorize(MASAuthorizationRequest request) {

        {
            try {
                String clientId = request.getClientId();
                Uri redirectUri = request.getRedirectUri();
                String scope = request.getScope();

                //This is the gateway state that will be provided to AppAuth

                String state = request.getState();
                String responseType = request.getResponseType();

                URI authEndpoint =
                ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider()
                        .getUri(MASConfiguration.getCurrentConfiguration()
                                .getEndpointPath(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_AUTHORIZE));

                Uri tokenEndpoint = Uri.parse("");

                AuthorizationServiceConfiguration config = new AuthorizationServiceConfiguration(
                        Uri.parse(authEndpoint.toString()), tokenEndpoint, null);
                if (redirectUri != null) {
                    AuthorizationRequest.Builder builder = new AuthorizationRequest
                            .Builder(config, clientId, responseType, redirectUri)
                            .setState(state)
                            .setDisplay("page")
                            .setScopes(scope);

                    PKCE codeChallenge = generateCodeChallenge();
                    //ConfigurationManager.getInstance().enablePKCE(false);
                    //PKCE social login support for MAG
                    if (codeChallenge != null) {
                        //String codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier();
                        AuthorizationRequest.Builder codeverifierBuilder = builder.setCodeVerifier(
                                //The code verifier is stored on the MAG Server;
                                //this is only for passing the code verifier check for AppAuth.
                                //The code verifier will not be used for retrieving the Access Token.
                                codeChallenge.codeVerifier,
                                codeChallenge.codeChallenge,
                                codeChallenge.codeChallengeMethod);
                        CodeVerifierCache.getInstance().store(state, codeChallenge.codeVerifier);
                    } else {
                        builder.setCodeVerifier(null);
                    }
                    ArrayMap<String, String> arrayMap = new ArrayMap<String, String>();

                    TokenManager tokenManager = StorageProvider.getInstance().getTokenManager();
                    String magIdentifier = tokenManager.getMagIdentifier();

                    if (magIdentifier != null && !"".equals(magIdentifier)) {
                        arrayMap.put("mag-identifier", magIdentifier);
                        builder.setAdditionalParameters(arrayMap);
                    }

                    AuthorizationRequest req = builder.build();


                    Intent postAuthIntent = completeIntent;
                    Intent authCanceledIntent = cancelIntent;

                    // Workaround for Samsung's SBrowser
                    // As described in https://github.com/openid/AppAuth-Android/issues/157
                    VersionedBrowserMatcher matcher = new VersionedBrowserMatcher(
                            Browsers.SBrowser.PACKAGE_NAME,
                            Browsers.SBrowser.SIGNATURE_SET,
                            true, // uses custom tab
                            VersionRange.ANY_VERSION);
                    BrowserBlacklist blacklist = new BrowserBlacklist(matcher);

                    AuthorizationService service = new AuthorizationService(context,
                            new AppAuthConfiguration.Builder()
                                    .setBrowserMatcher(blacklist)
                                    .build());

                    service.performAuthorizationRequest(req,
                            PendingIntent.getActivity(context, req.hashCode(), postAuthIntent, 0),
                            PendingIntent.getActivity(context, req.hashCode(), authCanceledIntent, 0));
                } else {
                    if (DEBUG) Log.d(TAG, "No redirect URL detected.");
                }
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "Launching Social Login with AppAuth failed.", e);
            }
        }
    }

}
