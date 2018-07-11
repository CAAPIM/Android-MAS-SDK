/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;
import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

public class MASDeviceMetadata {

    private static final String ENDPOINT_PATH = MASConfiguration.getCurrentConfiguration().getEndpointPath(MobileSsoConfig.DEVICE_METADATA_PATH);
    private static final String MAG_IDENTIFIER = StorageProvider.getInstance().getTokenManager().getMagIdentifier();
    private static final String HEADER_KEY = "mag-identifier";

    private MASDeviceMetadata() {throw new IllegalStateException("Not allowed to instantiate");

    }

    public static void putAttribute(String attr, String value, final MASCallback<Void> callback) {

        MASRequest request = null;
        if (attr == null || !MASDevice.getCurrentDevice().isRegistered()) {
            Callback.onError(callback, new Throwable());
        }

        try {
            JSONObject data = new JSONObject();
            data.put("name", attr);
            data.put("value", value);

            request = new MASRequest.MASRequestBuilder(new URI(ENDPOINT_PATH))
                    .header(HEADER_KEY, MAG_IDENTIFIER)
                    .put(MASRequestBody.jsonBody(data))
                    .build();

        } catch (URISyntaxException | JSONException e) {
            Callback.onError(callback, e);
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                if (checkResponse(response)) {
                    Callback.onSuccess(callback, null);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public static void getAttribute(String name, final MASCallback<JSONArray> callback) {

        String route = ENDPOINT_PATH + "/" +name;
        MASRequest request = null;

        try {
            request = new MASRequest.MASRequestBuilder(new URI(route))
                    .header(HEADER_KEY, MAG_IDENTIFIER)
                    .responseBody(MASResponseBody.jsonArrayBody())
                    .get()
                    .build();
        } catch (URISyntaxException e) {
            Callback.onError(callback, e);
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONArray>>() {
            @Override
            public void onSuccess(MASResponse<JSONArray> response) {
                if (checkResponse(response)) {
                    Callback.onSuccess(callback, response.getBody().getContent());
                }
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
            request = new MASRequest.MASRequestBuilder(new URI(ENDPOINT_PATH))
                    .header(HEADER_KEY, MAG_IDENTIFIER)
                    .get()
                    .build();
        } catch (URISyntaxException e) {
            Callback.onError(callback, e);
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONArray>>() {
            @Override
            public void onSuccess(MASResponse<JSONArray> response) {
                if (checkResponse(response)) {
                   callback.onSuccess(response.getBody().getContent());
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public static void deleteAttribute(String attr, final MASCallback<Void> callback) {
        MASRequest request = null;
        String route = ENDPOINT_PATH + "/" +attr;

        try {
            request = new MASRequest.MASRequestBuilder(new URI(route))
                    .header(HEADER_KEY, MAG_IDENTIFIER)
                    .delete(MASRequestBody.stringBody(attr))
                    .build();
        } catch (URISyntaxException e) {
            callback.onError(e);
        }

        MAS.invoke(request, new MASCallback<MASResponse<String>>() {
            @Override
            public void onSuccess(MASResponse<String> response) {
                if (checkResponse(response) && response.getBody().getContent().equalsIgnoreCase("removed")) {
                    callback.onSuccess(null);
                }
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
            request = new MASRequest.MASRequestBuilder(new URI(ENDPOINT_PATH))
                    .header(HEADER_KEY, MAG_IDENTIFIER)
                    .delete(MASRequestBody.stringBody(""))
                    .build();
        } catch (URISyntaxException e) {
            Callback.onError(callback, e);
        }

        MAS.invoke(request, new MASCallback<MASResponse<String>>() {
            @Override
            public void onSuccess(MASResponse<String> response) {
                if (checkResponse(response) && response.getBody().getContent().equalsIgnoreCase("removed")) {
                    callback.onSuccess(null);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    private static boolean checkResponse(MASResponse response) {
        if(response == null) {
            return false;
        }
        int respCode = response.getResponseCode();
        boolean range = respCode >= 200 && respCode < 300;

        return range && response.getBody()!= null;
    }
}
