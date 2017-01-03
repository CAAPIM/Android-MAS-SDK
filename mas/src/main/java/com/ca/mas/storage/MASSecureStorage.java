/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import android.net.Uri;
import android.support.annotation.NonNull;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.core.util.Functions;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.foundation.util.FoundationConsts;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.HttpsURLConnection;

/**
 *
 */
public class MASSecureStorage extends AbstractMASStorage {

    public MASSecureStorage() {
        setDefaultDataMarshallers();
    }

    private void setDefaultDataMarshallers() {
        register(new StringDataMarshaller());
        register(new ByteArrayDataMarshaller());
        register(new JsonDataMarshaller());
        register(new BitmapDataMarshaller());
    }

    @Override
    public void save(@NonNull final String key, final Object value, @MASStorageSegment final int segment, final MASCallback<Void> callback) {
        checkNull(key, value);

        DataMarshaller relevantM;
        byte[] data;
        try {
            relevantM = findMarshaller(value);
            data = relevantM.marshall(value);
        } catch (Exception e) {
            Callback.onError(callback, e);
            return;
        }
        StorageItem item = new StorageItem();
        item.setKey(key);
        item.setType(relevantM.getTypeAsString());
        item.setValue(data);

        final MASRequest.MASRequestBuilder builder = getRequestBuilder(key, segment);
        JSONObject itemJson = null;
        try {
            itemJson = item.getAsJSONObject();
        } catch (JSONException je) {
            Callback.onError(callback, je);
            return;
        }

        builder.put(MASRequestBody.jsonBody(itemJson));
        final JSONObject finalItemJson = itemJson;


        //Try update first
        MAS.invoke(builder.build(), new MASCallback<MASResponse<Void>>() {

            @Override
            public void onSuccess(MASResponse<Void> result) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(Throwable e) {
                if (e.getCause() instanceof TargetApiException) {
                    if (((TargetApiException) e.getCause()).getResponse().getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                        //Create the data
                        builder.post(MASRequestBody.jsonBody(finalItemJson));
                        MAS.invoke(builder.build(), new MASCallback<MASResponse<Void>>() {
                            @Override
                            public void onSuccess(MASResponse<Void> result) {
                                Callback.onSuccess(callback, null);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Callback.onError(callback, e);
                            }
                        });
                    } else {
                        Callback.onError(callback, e);
                    }
                } else {
                    Callback.onError(callback, e);
                }
            }
        });

    }

    @Override
    public void findByKey(@NonNull final String key, @MASStorageSegment final int segment, final MASCallback callback) {
        checkNull(key);

        MASRequest request = getRequestBuilder(key, segment).build();
        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                // add the storage items to the model
                final StorageItem item = new StorageItem();
                try {
                    JSONObject jobj = response.getBody().getContent();
                    item.populate(jobj);

                    DataMarshaller relevantM = findMarshaller(item.getType());
                    Object result = relevantM.unmarshall(item.getValue());
                    Callback.onSuccess(callback, result);
                } catch (Exception je) {
                    Callback.onError(callback, je);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (e.getCause() instanceof TargetApiException) {
                    if (((TargetApiException) e.getCause()).getResponse().getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                        //Return null if server cannot find the data.
                        Callback.onSuccess(callback, null);
                    } else {
                        Callback.onError(callback, e);
                    }
                } else {
                    Callback.onError(callback, e);
                }
            }
        });
    }

    @Override
    public void keySet(@MASStorageSegment final int segment, final MASCallback<Set<String>> callback) {
        // ----- Storage Url
        MASRequest request = getRequestBuilder(null, segment).build();
        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                final Set<String> itemsL = new HashSet<String>();
                try {
                    JSONObject items = response.getBody().getContent();
                    JSONArray itemArr = items.getJSONArray(StorageConsts.KEY_RESULTS);
                    for (int i = 0; i < itemArr.length(); i++) {
                        JSONObject jobj = itemArr.getJSONObject(i);
                        StorageItem storageKey = new StorageItem();
                        storageKey.populate(jobj);
                        itemsL.add(storageKey.getKey());
                    }
                    Callback.onSuccess(callback, itemsL);
                } catch (JSONException je) {
                    Callback.onError(callback, je);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });

    }

    @Override
    public void delete(@NonNull final String key, @MASStorageSegment final int segment, final MASCallback callback) {
        checkNull(key);
        MASRequest request = getRequestBuilder(key, segment).delete(null).build();
        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(Throwable e) {
                if (e.getCause() instanceof TargetApiException) {
                    if (((TargetApiException) e.getCause()).getResponse().getResponseCode() == HttpsURLConnection.HTTP_NOT_FOUND) {
                        //Ignore it if the server cannot find the data.
                        Callback.onSuccess(callback, null);
                    } else {
                        Callback.onError(callback, e);
                    }
                } else {
                    Callback.onError(callback, e);
                }
            }
        });

    }

    /**
     * <p>Generate a String representing the cloud storage URL without a dataKey; i.e.;
     * <b>https://magserver:8443/Client/{clientId}/Data/{dataKey}</b></p>
     *
     * @param dataKey could be null signifying that all storage items should be retrieved.
     * @param segment
     * @return String representing the cloud storage URL.
     */
    private MASRequest.MASRequestBuilder getRequestBuilder(String dataKey, @MASStorageSegment int segment) {
        Uri.Builder uriBuilder = new Uri.Builder();

        String storagePath = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider()
                .getProperty(FoundationConsts.KEY_CONFIG_CLOUD_STORAGE_PATH);
        if (storagePath == null) {
            uriBuilder.appendPath(StorageConsts.MAS_STORAGE_MASS).appendPath(StorageConsts.MAS_STORAGE_VERSION);
        } else {
            String[] paths = storagePath.split(FoundationConsts.FSLASH);
            for (String p : paths) {
                //This is bad, we should change the path.
                if (!p.equalsIgnoreCase(StorageConsts.KEY_COMPONENT_CLIENT)) {
                    uriBuilder.appendPath(p);
                }
            }
        }

        boolean userSessionRequired = false;
        switch (segment) {
            case MASConstants.MAS_USER:
                uriBuilder.appendPath(StorageConsts.KEY_COMPONENT_USER);
                userSessionRequired = true;
                break;
            case MASConstants.MAS_APPLICATION:
                uriBuilder.appendPath(StorageConsts.KEY_COMPONENT_CLIENT);
                break;
            case MASConstants.MAS_USER | MASConstants.MAS_APPLICATION:
                uriBuilder.appendPath(StorageConsts.KEY_COMPONENT_CLIENT).appendPath(StorageConsts.KEY_COMPONENT_USER);
                userSessionRequired = true;
                break;
            default:
                throw new IllegalArgumentException("Storage segment is not supported");

        }
        uriBuilder.appendPath(StorageConsts.KEY_COMPONENT_DATA);

        if (dataKey != null) {
            uriBuilder.appendPath(dataKey);
        }

        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(uriBuilder.build());
        if (userSessionRequired) {
            return requestBuilder.password();
        }
        return requestBuilder;
    }

}
