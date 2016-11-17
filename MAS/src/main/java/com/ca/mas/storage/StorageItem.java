/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.foundation.MASTransformable;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ca.mas.core.MAG.DEBUG;

/**
 * <p><b>StorageItem</b> is a class that contains 3 components;
 * <ul>
 * <li>key</li>
 * <li>modifiedDate</li>
 * <li>value - which is a JSON document containing...
 * <ul>
 * <li>key - same as the StorageItem key</li>
 * <li>modifiedDate - same as the StorageItem modifiedDate</li>
 * <li>type - the type of the value contained inside the StorageItem value</li>
 * <li>value - the value itself</li>
 * </ul>
 * </li>
 * </ul>
 * <p>As an example, a StorageItem consisting of the value of pi would have the JSON representation of;</p>
 * <pre>
 *     {"key": "pi", "type":"float", "value":"3.14159"}
 * </pre>
 * When all of the values are read, there is no actual value, on a reference to the single entry. For example;
 * <pre>
 *      {"key":"cs_key4","modifiedDate":"Fri Mar 18 11:30:24 PDT 2016","$ref":"https:\/\/mag-autotest-mysql.l7tech.com:8443\/MASS\/v1\/Client\/0568342e-9c0f-4bce-aa46-96e14cea6f7a\/Data\/cs_key4"}
 * </pre>
 * The '$ref' value is the reference to the actual item. When a single item is ready it contains the value that was posted to the cloud storage as a
 * base64 encoded blob.
 * <pre>
 *      {"key":"key_13","modifiedDate":"Fri Mar 18 13:41:32 PDT 2016","type":"application\/json; charset=UTF-8","value":"eyJrZXkiOiJrZXlfMTMiLCJ0eXBlIjoiZmxvYXQiLCJ2YWx1ZSI6IjMuMTQxNTkifQ=="}
 * </pre>
 * Once decoded, the value will be of the form;
 * <pre>
 *     {"key":"key_13","type":"String","value":"value 13”}
 * </pre>
 * </p>
 */
class StorageItem implements MASTransformable {

    private static String TAG = StorageItem.class.getSimpleName();

    private StorageKey storageKey;

    //Data part
    private String key;
    private byte[] value;
    private String type;


    public String getKey() {
        return key;
    }

    public void setKey(@NonNull final String key) {
        this.key = key;
        storageKey = new StorageKey() {
            public String getDataKey() {
                return key;
            }
        };
    }

    @Override
    public void populate(@NonNull JSONObject jobj) throws JSONException {
        if (DEBUG) Log.d(TAG, "Raw Storage Item: " + jobj.toString());
        // outer
        if (storageKey == null) {
            storageKey = new StorageKey();
        }
        storageKey.populate(jobj);
        key = storageKey.getDataKey();

        // inner
        String dataValue = jobj.optString(StorageConsts.KEY_VALUE);
        String valueStr = new String(Base64.decode(dataValue, Base64.NO_WRAP));
        if (storageKey.getContentType() != null && storageKey.getContentType().contains(StorageConsts.MT_APP_JSON)) {
            JSONObject doc = new JSONObject(valueStr);
            type = doc.optString(StorageConsts.KEY_TYPE, StorageConsts.TYPE_JSON);
            value = Base64.decode(doc.optString(StorageConsts.KEY_VALUE), Base64.NO_WRAP);
        } else {
            type = StorageConsts.DEFAULT_TYPE_STRING;
            value = valueStr.getBytes();
        }
    }

    public byte[] getValue() {
        return value;
    }

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        JSONObject jobj = new JSONObject();
        if (TextUtils.isEmpty(key)) {
            throw new JSONException("Required key value is missing.");
        }
        jobj.put(StorageConsts.KEY_KEY, key);
        jobj.put(StorageConsts.KEY_TYPE, type);
        if (value != null) {
            String val = Base64.encodeToString(value, Base64.NO_WRAP);
            jobj.put(StorageConsts.KEY_VALUE, val);
        }

        return jobj;
    }

    public void setValue(byte[] value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
