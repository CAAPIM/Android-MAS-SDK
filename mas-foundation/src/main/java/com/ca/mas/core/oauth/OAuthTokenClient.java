/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.oauth;

import android.support.annotation.NonNull;

import android.util.Log;
import android.util.Pair;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.foundation.MASAuthCredentials;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.request.MAGInternalRequest;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponseBody;

import org.json.JSONException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Utility class that encapsulates talking to the token server into Java method calls.
 * This handles just the network protocol for communicating with the MAG server to obtain an access token.
 * It does not deal with state management, token persistence, looking up credentials in the context, or anything other
 * higher-level issue.
 */
public class OAuthTokenClient extends ServerClient {

    public OAuthTokenClient(MssoContext mssoContext) {
        super(mssoContext);
    }

    /**
     * Exchange a username and password for an access token that can be used to access APIs.
     *
     * @param request        the oauth request. Required.
     * @param clientId       the client ID of the app seeking access. Required.
     * @param clientSecret   the client secret of the app seeking access. Required.
     * @param requestIdToken if true, then "msso" will be added to the requested scope, and the response may include an ID token as well as an access token.
     * @return a successful JSON response guaranteed to contain a non-empty accessToken and that may additionally contain a new ID token.  Never null.
     * @throws OAuthServerException    if there is an error response from the token server
     * @throws OAuthException          if there is an error other than a valid error JSON response from the token server
     */
    public OAuthTokenResponse obtainTokensUsingCredentials(@NonNull MAGInternalRequest request,
                                                           @NonNull String clientId,
                                                           @NonNull String clientSecret, boolean requestIdToken) throws OAuthException, OAuthServerException {

        MASAuthCredentials credentials = request.getGrantProvider().getCredentials(mssoContext);
        if (credentials == null)
            throw new NullPointerException("credentials");

        String scope = request.getScope();
        if (scope == null)
            scope = OPENID;
        if (requestIdToken && !scope.contains(MSSO))
            scope = scope + " " + MSSO;

        List<Pair<String, String>> form = new ArrayList<>();

        List<Pair<String, String>> params = credentials.getParams();
        if (params != null) {
            for (Pair<String, String> param : params) {
                form.add(new Pair<>(param.first, param.second));
            }
        }
        form.add(new Pair<>(CLIENT_ID, clientId));
        form.add(new Pair<>(CLIENT_SECRET, clientSecret));
        form.add(new Pair<>(SCOPE, scope));
        form.add(new Pair<>(GRANT_TYPE, credentials.getGrantType()));

        URI uri = conf.getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN);
        MASRequest tokenRequest = new MASRequest.MASRequestBuilder(uri)
                .post(MASRequestBody.urlEncodedFormBody(form))
                .responseBody(MASResponseBody.stringBody())
                .build();


        OAuthTokenResponse tokenResponse = null;
        try {
            tokenResponse = new OAuthTokenResponse(obtainServerResponseToPostedForm(tokenRequest));
            validate(tokenResponse);

        } catch (JSONException | MAGException e) {
            if (DEBUG)
                Log.d(TAG,"MAGErrorCode.ACCESS_TOKEN_INVALID"+e.getMessage()+"\n"+e.getStackTrace());
            throw new OAuthException(MAGErrorCode.ACCESS_TOKEN_INVALID, e);
        }

        return tokenResponse;
    }

    /**
     * Exchange an ID token for an access token that can be used to access APIs.
     *
     * @param idToken      an ID token, presumably a JWT.  Required.
     * @param clientId     the client ID of the app seeking access. Required.
     * @param clientSecret the client secret of the app seeking access. Required.
     * @param scope        Scope of access to request.  If null, defaults to "openid msso".
     * @return a successful JSON response guaranteed to contain a non-empty accessToken.  Never null.
     * @throws OAuthServerException    if there is an error response from the token server
     * @throws OAuthException          if there is an error other than a valid error JSON response from the token server
     */
    public OAuthTokenResponse obtainAccessTokenUsingIdToken(@NonNull IdToken idToken,
                                                            @NonNull String clientId,
                                                            @NonNull String clientSecret, String scope) throws OAuthException, OAuthServerException {
        if (scope == null)
            scope = OPENID_PHONE_EMAIL;

        List<Pair<String, String>> form = new ArrayList<>();
        form.add(new Pair<String, String>(ASSERTION, idToken.getValue()));
        form.add(new Pair<String, String>(CLIENT_ID, clientId));
        form.add(new Pair<String, String>(CLIENT_SECRET, clientSecret));
        form.add(new Pair<String, String>(SCOPE, scope));
        form.add(new Pair<String, String>(GRANT_TYPE, idToken.getType()));

        URI uri = conf.getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN_SSO);
        MASRequest tokenRequest = new MASRequest.MASRequestBuilder(uri)
                .post(MASRequestBody.urlEncodedFormBody(form))
                .responseBody(MASResponseBody.stringBody())
                .build();


        OAuthTokenResponse tokenResponse = null;
        try {
            tokenResponse = new OAuthTokenResponse(obtainServerResponseToPostedForm(tokenRequest));
            validate(tokenResponse);
        } catch (JSONException | MAGException e) {
            throw new OAuthException(MAGErrorCode.ACCESS_TOKEN_INVALID, e);
        }
        return tokenResponse;

   }

    private void validate(OAuthTokenResponse tokenResponse) throws OAuthException {
        if (!tokenResponse.isBearer()) {
            if (DEBUG)
                Log.d(TAG,"MAGErrorCode.ACCESS_TOKEN_INVALID"+"request_token response was token_type other than bearer");
            throw new OAuthException(MAGErrorCode.ACCESS_TOKEN_INVALID, "request_token response was token_type other than bearer");
        }
        final String accessToken = tokenResponse.getAccessToken();
        if (accessToken == null || accessToken.length() < 1) {
            if (DEBUG)
                Log.d(TAG,"MAGErrorCode.ACCESS_TOKEN_INVALID"+"request_token response did not include an access_token");
            throw new OAuthException(MAGErrorCode.ACCESS_TOKEN_INVALID, "request_token response did not include an access_token");
        }

    }

    /**
     * Exchange a refresh token for a new access token that can be used to access APIs.
     *
     * @param refreshToken a refresh token.  Required.
     * @param clientId     the client ID of the app seeking access. Required.
     * @param clientSecret the client secret of the app seeking access. Required.
     * @return a successful JSON response guaranteed to contain a non-empty accessToken.  Never null.
     * @throws OAuthServerException    if there is an error JSON response from the token server
     * @throws OAuthException          if there is an error other than a valid error JSON response from the token server
     */
    public OAuthTokenResponse obtainTokenUsingRefreshToken(@NonNull String refreshToken,
                                                           @NonNull String clientId,
                                                           @NonNull String clientSecret) throws OAuthException, OAuthServerException {

        List<Pair<String, String>> form = new ArrayList<>();
        form.add(new Pair<String, String>(REFRESH_TOKEN, refreshToken));
        form.add(new Pair<String, String>(CLIENT_ID, clientId));
        form.add(new Pair<String, String>(CLIENT_SECRET, clientSecret));
        form.add(new Pair<String, String>(GRANT_TYPE, REFRESH_TOKEN));

        URI uri = conf.getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN);
        MASRequest tokenRequest = new MASRequest.MASRequestBuilder(uri)
                .post(MASRequestBody.urlEncodedFormBody(form))
                .responseBody(MASResponseBody.stringBody())
                .build();

        OAuthTokenResponse tokenResponse = null;

        //Remove refresh token once we get a proper response from the server, no matter success or not.
        mssoContext.takeRefreshToken();

        try {
            tokenResponse = new OAuthTokenResponse(obtainServerResponseToPostedForm(tokenRequest));
            validate(tokenResponse);
        } catch (JSONException | MAGException e) {
            if (DEBUG)
                Log.d(TAG,"MAGErrorCode.ACCESS_TOKEN_INVALID"+e.getMessage());
            throw new OAuthException(MAGErrorCode.ACCESS_TOKEN_INVALID, e);
        } catch (MAGServerException e) {
            if (DEBUG)
                Log.d(TAG,"OAuthServerException"+e.getMessage());
            throw new OAuthServerException(e);
        }
        return tokenResponse;
    }



}
