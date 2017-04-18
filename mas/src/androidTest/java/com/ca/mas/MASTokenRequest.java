/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import android.net.Uri;
import android.util.Log;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.http.MAGRequestBody;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.oauth.GrantProvider;
import com.ca.mas.core.request.internal.LocalRequest;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.foundation.MASRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MASTokenRequest implements MASRequest, LocalRequest {

    private static final String TAG = MASTokenRequest.class.getCanonicalName();

    private static final String ACCESSTOKEN = "accesstoken";
    private static final String REFRESHTOKEN = "refreshtoken";
    private static final String EXPIRY = "expiry";
    private static final String ID_TOKEN = "idToken";
    private static final String ID_TOKEN_TYPE = "idTokenType";
    private static final String CLIENT_ID = "clientId";
    private static final String CLIENT_SECRET = "clientSecret";
    private static final String CLIENT_EXPIRY = "clientExpiry";

    private MASRequest request;

    public MASTokenRequest() {
        request = new MASRequestBuilder(new Uri.Builder().appendPath("dummy").build()).build();
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
                if (getResponseCode() == HttpURLConnection.HTTP_OK) {
                    return new MAGResponseBody<JSONObject>() {
                        @Override
                        public JSONObject getContent() {
                            JSONObject entity = new JSONObject();
                            try {
                                entity.put(ACCESSTOKEN, context.getAccessToken());
                                entity.put(REFRESHTOKEN, context.getRefreshToken());
                                entity.put(EXPIRY, context.getAccessTokenExpiry());

                                IdToken token = context.getIdToken();
                                if (token != null) {
                                    entity.put(ID_TOKEN, token.getValue());
                                    entity.put(ID_TOKEN_TYPE, token.getType());
                                }
                                entity.put(CLIENT_ID, context.getClientId());
                                entity.put(CLIENT_SECRET, context.getClientSecret());
                                entity.put(CLIENT_EXPIRY, context.getClientExpiration());
                            } catch (JSONException e) {
                                Log.i(TAG, e.getMessage(), e);
                                return new JSONObject();
                            }
                            return entity;
                        }
                    };
                } else {
                    return new MAGResponseBody<JSONObject>() {
                        @Override
                        public JSONObject getContent() {
                            return new JSONObject();
                        }
                    };
                }
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
