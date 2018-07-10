/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

// TODO : name and package for this class, this will perform the API call to the server part, and it shouldt be in the same package as the public interface ??
public class MASDeviceMetadata {

    static String endpointPath = MASConfiguration.getCurrentConfiguration().getEndpointPath(MobileSsoConfig.DEVICE_METADATA_PATH);

    public static void putAttribute(String attr, String value, final MASCallback<Void> callback) {

        MASRequest request = null;
        if (attr == null) {
            return;
        }

        try {
            JSONObject requestData = new JSONObject();
            requestData.put(attr, value);

            request = new MASRequest.MASRequestBuilder(new URI(endpointPath))
                    .put(MASRequestBody.jsonBody(requestData))
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        MAS.invoke(request, new MASCallback<MASResponse<String>>() {
            @Override
            public void onSuccess(MASResponse<String> response) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public static void getAttribute(final MASCallback<String> callback) {

        MASRequest request = null;

        try {
            request = new MASRequest.MASRequestBuilder(new URI(endpointPath))
                    .get()
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                Callback.onSuccess(callback, response.getBody().getContent().toString());
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public static void getAttributes(final MASCallback<JSONArray> callback){

        MASRequest request = null;
        try {
            request = new MASRequest.MASRequestBuilder(new URI(endpointPath))
                    .get()
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONArray>>() {
            @Override
            public void onSuccess(MASResponse<JSONArray> response) {
                if (response != null && response.getBody()!= null) {
                   callback.onSuccess(response.getBody().getContent());
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public static void deleteAttribute(final MASCallback<Void> callback) {
        MASRequest request = null;
        try {
            request = new MASRequest.MASRequestBuilder(new URI(endpointPath))
                    .delete(MASRequestBody.jsonBody(new JSONObject()))
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        MAS.invoke(request, new MASCallback<MASResponse<String>>() {
            @Override
            public void onSuccess(MASResponse<String> response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public static void deleteAttributes(final MASCallback<Void> callback) {
        MASRequest request = null;
        try {
            request = new MASRequest.MASRequestBuilder(new URI(endpointPath))
                    .delete(MASRequestBody.jsonArrayBody(new JSONArray()))
                    .build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                callback.onSuccess(null);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }
}
