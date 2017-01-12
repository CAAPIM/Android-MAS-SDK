/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.net.Uri;

/**
 * Response to an authorization request
 * {@link com.ca.mas.foundation.auth.MASAuthenticationProviders#getAuthenticationProviders(MASCallback)}
 *
 * @see <a href="https://tools.ietf.org/html/rfc6749#section-4.1.2">
 */
public class MASAuthorizationResponse {

    //The returned authorization code parameter from the redirect uri
    private String authorizationCode;
    //The returned state parameter from the redirect uri
    private String state;


    public MASAuthorizationResponse(String authorizationCode, String state) {
        this.authorizationCode = authorizationCode;
        this.state = state;
    }

    /**
     * Extracts an authorization response from redirect uri.
     * The {@link MASAuthorizationResponse} can be used for {@link MASUser#login(MASAuthorizationResponse, MASCallback)}
     * to authenticate the user.
     */
    public static MASAuthorizationResponse fromUri(Uri uri) {
        return new MASAuthorizationResponse(uri.getQueryParameter("code"), uri.getQueryParameter("state"));
    }

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getState() {
        return state;
    }
}
