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
import com.ca.mas.core.request.internal.LocalRequest;
import com.ca.mas.core.request.internal.MAGRequestProxy;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MASTokenRequest extends MAGRequestProxy implements LocalRequest<JSONObject> {

    private static final String ACCESS_TOKEN = "access_token";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String EXPIRES_IN = "expires_in";

    public MASTokenRequest() {
        //The path is not required for LocalRequest
        request = new MASRequestBuilder(new Uri.Builder().appendPath("token").build()).build();
    }

    public MASTokenRequest(MASRequest request) {
        this.request = request;
    }

    @Override
    public MASResponse<JSONObject> send(final MssoContext context) throws IOException {
        return new MASResponse<JSONObject>() {

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
            public MASResponseBody<JSONObject> getBody() {
                return new MASResponseBody<JSONObject>() {
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
    public MASProgressListener getProgressListener() {
        return request.getProgressListener();
    }

    @Override
    public FileDownload getDownloadFile() {
        return request.getDownloadFile();
    }
}
