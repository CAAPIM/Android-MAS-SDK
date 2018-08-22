package com.ca.mas.foundation;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

class DeviceMetadata {
    private static final String ENDPOINT_PATH = MASConfiguration.getCurrentConfiguration().getEndpointPath(MobileSsoConfig.DEVICE_METADATA_PATH);

    private DeviceMetadata() {throw new IllegalStateException("Not allowed to instantiate");

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
                    .put(MASRequestBody.jsonBody(data))
                    .build();

        } catch (URISyntaxException | JSONException e) {
            Callback.onError(callback, e);
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public static void getAttribute(String name, final MASCallback<JSONObject> callback) {

        String route = ENDPOINT_PATH + "/" +name;
        MASRequest request = null;

        try {
            request = new MASRequest.MASRequestBuilder(new URI(route))
                    .responseBody(MASResponseBody.jsonBody())
                    .get()
                    .build();
        } catch (URISyntaxException e) {
            Callback.onError(callback, e);
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                Callback.onSuccess(callback, response.getBody().getContent());
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
                    .get()
                    .build();
        } catch (URISyntaxException e) {
            Callback.onError(callback, e);
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONArray>>() {
            @Override
            public void onSuccess(MASResponse<JSONArray> response) {
                callback.onSuccess(response.getBody().getContent());
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
                    .delete(MASRequestBody.stringBody(attr))
                    .build();
        } catch (URISyntaxException e) {
            callback.onError(e);
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
            request = new MASRequest.MASRequestBuilder(new URI(ENDPOINT_PATH))
                    .delete(MASRequestBody.stringBody(""))
                    .build();
        } catch (URISyntaxException e) {
            Callback.onError(callback, e);
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
}
