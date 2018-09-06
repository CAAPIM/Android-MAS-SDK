package com.ca.mas.foundation;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;

import static com.ca.mas.core.client.ServerClient.findErrorCode;

class DeviceMetadata {
    private static final String ENDPOINT_PATH = MASConfiguration.getCurrentConfiguration().getEndpointPath(MobileSsoConfig.DEVICE_METADATA_PATH);
    private static final int MAG_MAX_METADATA = 1016155;
    private static final int MAG_ATTR_NOT_FOUND = 1016156;

    private DeviceMetadata() {throw new IllegalStateException("Not allowed to instantiate");

    }

    public static void putAttribute(String attr, String value, final MASCallback<Void> callback) {

        checkConditions(attr, callback);
        MASRequest request = null;

        try {
            JSONObject data = new JSONObject();
            data.put("name", attr);
            data.put("value", value);

            request = new MASRequest.MASRequestBuilder(new URI(ENDPOINT_PATH))
                    .put(MASRequestBody.jsonBody(data))
                    .build();

        } catch (URISyntaxException | JSONException e) {
            Callback.onError(callback, e);
            return;
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(Throwable e) {

                int errorCode = getSpecialError(e);

                if (errorCode == MAG_MAX_METADATA) {
                    Callback.onError(callback, new MASDeviceAttributeOverflowException(e));
                } else {
                    Callback.onError(callback, e);
                }
            }
        });
    }

    private static int getSpecialError(Throwable e) {
        int error = -1;
        try {
            if (((MASException) e).getRootCause() instanceof TargetApiException){
                TargetApiException tException = (TargetApiException) ((MASException) e).getRootCause();
                error = findErrorCode(tException.getResponse());
            }
        } catch (Exception e1) {
        }

        return error;
    }

    public static void getAttribute(String name, final MASCallback<JSONObject> callback) {

        checkConditions(name, callback);

        String route = ENDPOINT_PATH + "/" +name;
        MASRequest request = null;

        try {
            request = new MASRequest.MASRequestBuilder(new URI(route))
                    .responseBody(MASResponseBody.jsonBody())
                    .get()
                    .build();
        } catch (URISyntaxException e) {
            Callback.onError(callback, e);
            return;
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                Callback.onSuccess(callback, response.getBody().getContent());
            }

            @Override
            public void onError(Throwable e) {
                int errorCode = getSpecialError(e);
                if (errorCode == MAG_ATTR_NOT_FOUND) {
                    // - return empty object if that resource does not exist
                    Callback.onSuccess(callback, new JSONObject());
                } else {
                    Callback.onError(callback, e);
                }

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
            return;
        }

        MAS.invoke(request, new MASCallback<MASResponse<JSONArray>>() {
            @Override
            public void onSuccess(MASResponse<JSONArray> response) {
                Callback.onSuccess(callback, response.getBody().getContent());
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public static void deleteAttribute(String attr, final MASCallback<Void> callback) {
        checkConditions(attr, callback);

        MASRequest request = null;
        String route = ENDPOINT_PATH + "/" +attr;

        try {
            request = new MASRequest.MASRequestBuilder(new URI(route))
                    .delete(MASRequestBody.stringBody(attr))
                    .build();
        } catch (URISyntaxException e) {
            callback.onError(e);
            return;
        }

        MAS.invoke(request, new MASCallback<MASResponse<String>>() {
            @Override
            public void onSuccess(MASResponse<String> response) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(Throwable e) {
                int errorCode = getSpecialError(e);
                if (errorCode == MAG_ATTR_NOT_FOUND) {
                    callback.onSuccess(null);
                }

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
            return;
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

    private static void checkConditions(String name, MASCallback callback){
        if (name == null || !MASDevice.getCurrentDevice().isRegistered()) {
            Callback.onError(callback, new IllegalArgumentException());
        }
    }
}
