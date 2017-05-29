/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.oauth;

import com.ca.mas.core.client.ServerResponse;
import com.ca.mas.core.token.IdToken;

import org.json.JSONException;

/**
 * Represents a JSON response from the token server.
 * <p/>
 * This is either a JSON success or error response from the request_token or request_token_sso method,
 * or a JSON error response from the register_device method.
 */
public class OAuthTokenResponse extends ServerResponse {

    public OAuthTokenResponse(int status, String json) throws JSONException {
        super(status, json);
    }

    public OAuthTokenResponse(int status, int errorCode, String json) throws JSONException {
        super(status, errorCode, json);
    }

    public OAuthTokenResponse(ServerResponse response) throws JSONException{
        super(response.getStatus(), response.getErrorCode(), response.getJson());
    }

    /**
     * @return the contents of a string field named "access_token", or null.
     */
    public String getAccessToken() {
        return parsed.optString("access_token", null);
    }

    /**
     * @return the contents of a string field named "refresh_token", or null.
     */
    public String getRefreshToken() {
        return parsed.optString("refresh_token", null);
    }

    public String getGrantedScope() {
        return parsed.optString("scope");
    }

    /**
     * @return true if the JSON object includes a field named "token_type" with the value "bearer".
     */
    public boolean isBearer() {
        return "bearer".equalsIgnoreCase(parsed.optString("token_type"));
    }

    /**
     * @return the contents of a field named "expires_in" as a string, or 0.
     */
    public long getExpiresIn() {
        try {
            return parsed.getLong("expires_in");
        } catch (JSONException e) {
            return 0;
        }
    }

    /**
     * @return the contents of a string field named "id_token", or null.
     */
    public IdToken getIdToken() {
        String idToken = parsed.optString("id_token", null);
        if (idToken != null) {
            return new IdToken(idToken, parsed.optString("id_token_type", null));
        }
        return null;
    }

}
