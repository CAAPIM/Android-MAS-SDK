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
import android.os.Bundle;
import android.provider.Browser;
import android.support.customtabs.CustomTabsIntent;
import android.util.ArrayMap;
import android.util.Log;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.oauth.CodeVerifierCache;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.foundation.MASAuthorizationRequest;
import com.ca.mas.foundation.MASAuthorizationRequestHandler;
import com.ca.mas.foundation.MASConfiguration;

import net.openid.appauth.AppAuthConfiguration;
import net.openid.appauth.AuthorizationRequest;
import net.openid.appauth.AuthorizationService;
import net.openid.appauth.AuthorizationServiceConfiguration;
import net.openid.appauth.CodeVerifierUtil;
import net.openid.appauth.browser.BrowserBlacklist;
import net.openid.appauth.browser.Browsers;
import net.openid.appauth.browser.VersionRange;
import net.openid.appauth.browser.VersionedBrowserMatcher;

import java.net.URI;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Handler class for browser based user authorization
 * Using <a href="https://github.com/openid/AppAuth-Android">https://github.com/openid/AppAuth-Android</a> to perform user authorization.
 */
public class MASAppAuthAuthorizationRequestHandler implements MASAuthorizationRequestHandler {

    private Context context;

    public MASAppAuthAuthorizationRequestHandler(Context context) {
        this.context = context;
    }

    /**
     * Performs user authorization by redirecting to the authorization redirect URL in ChromeTabs using AppAuth.
     * This will launch the browser login page.
     */
    @Override
    public void authorize(MASAuthorizationRequest request) {
        try {
            String clientId = request.getClientId();
            Uri redirectUri = request.getRedirectUri();
            String scope = request.getScope();
            String display = request.getDisplay();
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
                        .setDisplay(display)
                        .setScopes(scope);

                //PKCE social login support for MAG
                String codeVerifier = CodeVerifierUtil.generateRandomCodeVerifier();
                builder.setCodeVerifier(
                        //The code verifier is stored on the MAG Server;
                        //this is only for passing the code verifier check for AppAuth.
                        //The code verifier will not be used for retrieving the Access Token.
                        codeVerifier,
                        CodeVerifierUtil.deriveCodeVerifierChallenge(codeVerifier),
                        CodeVerifierUtil.getCodeVerifierChallengeMethod());

                CodeVerifierCache.getInstance().store(state, codeVerifier);

                ArrayMap<String, String> arrayMap = new ArrayMap<String, String>();

                TokenManager tokenManager = StorageProvider.getInstance().getTokenManager();
                String magIdentifier = tokenManager.getMagIdentifier();

                AuthorizationRequest req = builder.build();

                Intent postAuthIntent = getWebLoginCompleteIntent();
                Intent authCanceledIntent = getWebLoginCancelIntent();

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

                if (magIdentifier != null && !"".equals(magIdentifier)) {
                    arrayMap.put("mag-identifier", magIdentifier);
                    builder.setAdditionalParameters(arrayMap);
                    CustomTabsIntent customTabsIntent = service.createCustomTabsIntentBuilder().build();

                    Bundle headers = new Bundle();
                    headers.putString("mag-identifier", magIdentifier);
                    customTabsIntent.intent.putExtra(Browser.EXTRA_HEADERS, headers);

                    service.performAuthorizationRequest(req,
                            PendingIntent.getActivity(context, req.hashCode(), postAuthIntent, 0),
                            PendingIntent.getActivity(context, req.hashCode(), authCanceledIntent, 0),
                            customTabsIntent);
                } else {
                    service.performAuthorizationRequest(req,
                            PendingIntent.getActivity(context, req.hashCode(), postAuthIntent, 0),
                            PendingIntent.getActivity(context, req.hashCode(), authCanceledIntent, 0));
                }
            } else {
                if (DEBUG) Log.d(TAG, "No redirect URL detected.");
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Launching Social Login with AppAuth failed.", e);
        }
    }

    /**
     * Returns the MASAppAuthRedirectHandlerActivity if the MASUI library is included in the classpath.
     *
     * @return A MASOAuthRedirectActivity
     */
    private Intent getWebLoginCompleteIntent() {
        return new Intent(context, MASAppAuthRedirectHandlerActivity.class);
    }

    /**
     * Returns the MASFinishActivity if the MASUI library is included in the classpath.
     *
     * @return A MASFinishActivity
     */
    private Intent getWebLoginCancelIntent() {
        return new Intent(context, MASFinishActivity.class);
    }

}
