/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.request.internal;

import android.util.Log;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * A request to retrieve the OAuth Tokens as a JSON Message
 */
public class OAuthTokenRequest extends MAGRequestProxy implements LocalRequest {

    public static final String ACCESSTOKEN = "accesstoken";
    public static final String REFRESHTOKEN = "refreshtoken";
    public static final String EXPIRY = "expiry";

    public OAuthTokenRequest() {
        request = new MAGRequestBuilder((URL)null).password().build();
    }

    @Override
    public MAGResponse send(final MssoContext context) throws IOException {

        return new MAGResponse<JSONObject>() {

            String accessToken = context.getAccessToken();
            String refreshToken = context.getRefreshToken();
            long expiry = context.getAccessTokenExpiry();


            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }

            @Override
            public int getResponseCode() {
                if (accessToken == null && refreshToken == null && expiry == 0) {
                    return HttpURLConnection.HTTP_NOT_FOUND;
                } else {
                    return HttpURLConnection.HTTP_OK;
                }
            }

            @Override
            public String getResponseMessage() {
                return null;
            }

            @Override
            public MAGResponseBody<JSONObject> getBody() {
                if (getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return new MAGResponseBody<JSONObject>() {
                        @Override
                        public JSONObject getContent() {
                            JSONObject entity = new JSONObject();
                            try {
                                entity.put(ACCESSTOKEN, accessToken);
                                entity.put(REFRESHTOKEN, refreshToken);
                                entity.put(EXPIRY, expiry);
                            } catch (JSONException e) {
                                if (DEBUG) Log.i(TAG, e.getMessage(), e);
                                return new JSONObject();
                            }
                            return entity;
                        }
                    };
                }
                return new MAGResponseBody<JSONObject>() {
                    @Override
                    public JSONObject getContent() {
                        return new JSONObject();
                    }
                };
            }
        };
    }
}
