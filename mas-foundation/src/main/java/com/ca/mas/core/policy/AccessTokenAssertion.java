/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.auth.AuthenticationException;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.foundation.MASAuthCredentials;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.oauth.GrantProvider;
import com.ca.mas.core.oauth.OAuthException;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.core.oauth.OAuthTokenClient;
import com.ca.mas.core.oauth.OAuthTokenResponse;
import com.ca.mas.core.policy.exceptions.CredentialRequiredException;
import com.ca.mas.core.policy.exceptions.RetryRequestException;
import com.ca.mas.core.request.MAGInternalRequest;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.JWTValidationException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * A policy that includes an access token with each outbound request.
 * This policy must run after the DeviceRegistrationPolicy has succeeded.
 */
class AccessTokenAssertion implements MssoAssertion {

    public static final String TOKEN_EXPIRED_ERROR_CODE_SUFFIX = "990";

    private OAuthTokenClient oAuthTokenClient;

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        oAuthTokenClient = new OAuthTokenClient(mssoContext);
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) throws MAGException, MAGServerException {

        if (request.getRequest().getURL() != null) {
            if (request.getRequest().getURL().getHost() == null) {
                throw new IllegalArgumentException("Host is not provided");
            }
            if (!request.getRequest().getURL().getHost().equals(mssoContext.getConfigurationProvider().getTokenHost())) {
                throw new IllegalArgumentException(
                        "This method is valid only for the host that has issued the access_token");
            }
        }

        MAGInternalRequest magInternalRequest = (MAGInternalRequest) request.getRequest();
        String accessToken = findAccessToken(mssoContext, magInternalRequest);
        if (accessToken != null) {
            //Clear any Authorization from the header before adding new one.
            request.getRequest().addHeader("Authorization", "Bearer " + accessToken);
        }

    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MAGResponse response) throws MAGException {
        int errorCode = ServerClient.findErrorCode(response);
        if (errorCode == -1) {
            return;
        }
        String s = Integer.toString(errorCode);
        if (s.endsWith(TOKEN_EXPIRED_ERROR_CODE_SUFFIX)) {
            throw new RetryRequestException("Access token rejected by server") {
                @Override
                public void recover(MssoContext context) throws Exception {
                    context.clearAccessToken();
                }
            };
        }
    }

    @Override
    public void close() {
    }

    private String findAccessToken(MssoContext mssoContext, MAGInternalRequest request) throws CredentialRequiredException, OAuthException, OAuthServerException, AuthenticationException, JWTValidationException, RetryRequestException {
        String accessToken = mssoContext.getAccessToken();
        if (accessToken != null) {
            if (DEBUG) Log.d(TAG, "Validating access token");
            if (isAccessTokenStillValid(mssoContext)) {
                if (DEBUG) Log.d(TAG, "Access Token is still valid.");
                if (isSufficientScope(mssoContext, request)) {
                    //Handle grant flow switching from Client Credential to Password
                    if (request.getGrantProvider() == GrantProvider.PASSWORD) {
                        //The access token is granted by Client Credential if refresh token is null
                        //Please refer to https://tools.ietf.org/html/rfc6749#section-4.4.3 for detail
                        if (mssoContext.getRefreshToken() == null) {
                            mssoContext.clearAccessToken();
                            accessToken = null;
                        } else {
                            return accessToken;
                        }
                    } else {
                        return accessToken;
                    }
                } else {
                    if (DEBUG) Log.d(TAG, "Access Token does not have sufficient scope.");
                    mssoContext.clearAccessToken();
                    accessToken = null;
                }
            } else {
                accessToken = null;
            }

            String refreshToken = mssoContext.getRefreshToken();
            if (refreshToken != null) {
                try {
                    if (DEBUG) Log.d(TAG, "Obtain Access Token using Refresh Token");
                    accessToken = obtainAccessTokenUsingRefreshToken(mssoContext, refreshToken);
                } catch (OAuthServerException tse) {
                    //The access token and refresh token are no longer valid.
                    mssoContext.clearAccessToken();
                    accessToken = null;
                    if (DEBUG) Log.w(TAG,
                            "Refresh token failed, will fall back to ID token or password: " + tse.getMessage(), tse);
                    /* FALLTHROUGH and try again with ID token or password */
                }
            }

            if (accessToken != null)
                return accessToken;
        }

        // Obtain an access token from the token server.
        // If we have an ID token available, try to use that.  Otherwise, we may need to prompt for username and password.
        if (oAuthTokenClient == null)
            throw new IllegalStateException("policy not initialized");

        IdToken idToken = mssoContext.getIdToken();
        if (idToken != null) {
            if (DEBUG) Log.d(TAG, "Try to use id token to get new Access Token");
            return obtainAccessTokenUsingIdToken(mssoContext, idToken, request);
        }

        // We will have to use a username and password.  Ensure they are available.
        Boolean ssoEnabled = mssoContext.getConfigurationProvider().getProperty(ConfigurationProvider.PROP_SSO_ENABLED);
        boolean wantIdToken = ssoEnabled != null && ssoEnabled && mssoContext.getIdToken() == null;
        if (DEBUG) Log.d(TAG, "Obtain access token using Credential");
        return obtainAccessTokenUsingCredential(mssoContext, request, wantIdToken);
    }

    private boolean isAccessTokenStillValid(MssoContext mssoContext) {
        long expiry = mssoContext.getAccessTokenExpiry();
        return expiry <= 0 || System.currentTimeMillis() <= expiry;
    }

    private boolean isSufficientScope(MssoContext mssoContext, MAGRequest request) {
        String rScope = request.getScope();
        String gScope = mssoContext.getGrantedScope();
        if (rScope == null || rScope.trim().length() == 0) {
            return true;
        }
        if (gScope == null || gScope.trim().length() == 0) {
            return false;
        }
        String[] requestScopes = rScope.trim().split("\\s+");
        List<String> rScopeList = new ArrayList<>();
        Collections.addAll(rScopeList, requestScopes);
        rScopeList.remove(ServerClient.OPENID);
        rScopeList.remove(ServerClient.MSSO);
        rScopeList.remove(ServerClient.MSSO_REGISTER);
        rScopeList.remove(ServerClient.MSSO_CLIENT_REGISTER);

        String[] grantedScopes = gScope.split("\\s+");
        List<String> gScopeList = Arrays.asList(grantedScopes);

        return gScopeList.containsAll(rScopeList);

    }


    /**
     * Get username/password credentials from the MssoContext.
     *
     * @param mssoContext the MssoContext to examine.  Required.
     * @param request     the OAuthRequest to examine.  Required.
     * @return a valid non-empty UsernamePassword.  Never null, and the contained username and password are always non-null (but may be empty).
     * @throws CredentialRequiredException if credentials are not available, or if either the username or password was null.
     */
    private static MASAuthCredentials getCredsOrThrow(MssoContext mssoContext, MAGInternalRequest request) throws CredentialRequiredException {
        MASAuthCredentials creds = request.getGrantProvider().getCredentials(mssoContext);
        if (creds == null || !creds.isValid())
            throw new CredentialRequiredException();
        return creds;
    }

    private String obtainAccessTokenUsingIdToken(MssoContext mssoContext, IdToken idToken, MAGInternalRequest request) throws CredentialRequiredException, OAuthException, AuthenticationException, JWTValidationException, OAuthServerException, RetryRequestException {
        String clientId = mssoContext.getClientId();
        String clientSecret = mssoContext.getClientSecret();
        String scope = request.getScope();

        try {
            OAuthTokenResponse response = oAuthTokenClient.obtainAccessTokenUsingIdToken(idToken, clientId, clientSecret, scope);
            String accessToken = response.getAccessToken();
            mssoContext.onAccessTokenAvailable(accessToken, response.getRefreshToken(), response.getExpiresIn(), response.getGrantedScope());
            return accessToken;
        } catch (OAuthServerException e) {
            //The user session is no longer valid.
            mssoContext.clearIdToken();
            mssoContext.clearUserProfile();
            // Obtain credentials and try again
            return obtainAccessTokenUsingCredential(mssoContext, request, true);
        }
    }

    private String obtainAccessTokenUsingCredential(MssoContext mssoContext, MAGInternalRequest request, boolean wantIdToken) throws CredentialRequiredException, AuthenticationException, OAuthServerException, OAuthException, JWTValidationException {
        //Clear user profile before getting AccessToken using different credential.
        mssoContext.clearUserProfile();
        getCredsOrThrow(mssoContext, request);
        String clientId = mssoContext.getClientId();
        String clientSecret = mssoContext.getClientSecret();

        boolean gotToken = false;
        try {
            OAuthTokenResponse response = oAuthTokenClient.obtainTokensUsingCredentials(
                    request, clientId, clientSecret, wantIdToken);
            IdToken idToken = response.getIdToken();
            if (idToken != null) {
                mssoContext.onIdTokenAvailable(idToken);
            }
            String accessToken = response.getAccessToken();
            mssoContext.onAccessTokenAvailable(accessToken, response.getRefreshToken(), response.getExpiresIn(), response.getGrantedScope());
            gotToken = accessToken != null;
            return accessToken;
        } finally {
            if (!gotToken || !request.getGrantProvider().getCredentials(mssoContext).isReusable()) {
                // Clear cached credentials
                mssoContext.setCredentials(null);
            }
        }
    }

    private String obtainAccessTokenUsingRefreshToken(MssoContext mssoContext, String refreshToken) throws OAuthException, OAuthServerException, AuthenticationException, RetryRequestException {
        String clientId = mssoContext.getClientId();
        String clientSecret = mssoContext.getClientSecret();
        OAuthTokenResponse response = oAuthTokenClient.obtainTokenUsingRefreshToken(refreshToken, clientId, clientSecret);
        String accessToken = response.getAccessToken();
        mssoContext.onAccessTokenAvailable(accessToken, response.getRefreshToken(), response.getExpiresIn(), response.getGrantedScope());
        return accessToken;
    }
}
