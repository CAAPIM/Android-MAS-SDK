/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.net.Uri;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.http.MAGRequestBody;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.oauth.GrantProvider;
import com.ca.mas.core.request.internal.LocalRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MASTokenRequest implements MASRequest, LocalRequest {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String EXPIRES_IN = "expires_in";

    private MASRequest request;

    public MASTokenRequest() {
        //The path is not required for LocalRequest
        request = new MASRequestBuilder(new Uri.Builder().appendPath("token").build()).build();
    }

    public MASTokenRequest(MASRequest request) {
        this.request = request;
    }

    @Override
    public MAGResponse send(final MssoContext context) throws IOException {
        return new MAGResponse<JSONObject>() {

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }

            @Override
            public int getResponseCode() {
                return HttpURLConnection.HTTP_OK;
            }

            @Override
            public String getResponseMessage() {
                return null;
            }

            @Override
            public MAGResponseBody<JSONObject> getBody() {
                return new MAGResponseBody<JSONObject>() {
                    @Override
                    public JSONObject getContent() {
                        JSONObject entity = new JSONObject();
                        try {
                            entity.put(ACCESS_TOKEN, context.getAccessToken());
                            entity.put(REFRESH_TOKEN, context.getRefreshToken());
                            entity.put(EXPIRES_IN, context.getAccessTokenExpiry());
                        } catch (JSONException e) {
                            return new JSONObject();
                        }
                        return entity;
                    }
                };
            }
        };
    }

    @Override
    public URL getURL() {
        return request.getURL();
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return request.getHeaders();
    }

    @Override
    public GrantProvider getGrantProvider() {
        return request.getGrantProvider();
    }

    @Override
    public MAGRequestBody getBody() {
        return request.getBody();
    }

    @Override
    public MAGConnectionListener getConnectionListener() {
        return request.getConnectionListener();
    }

    @Override
    public MAGResponseBody<?> getResponseBody() {
        return request.getResponseBody();
    }

    @Override
    public String getScope() {
        return request.getScope();
    }

    @Override
    public boolean isPublic() {
        return false;
    }

    @Override
    public boolean notifyOnCancel() {
        return false;
    }
}
