/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import android.util.Base64;

import com.ca.mas.core.http.ContentType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class MemoryStorage {

    //Data Segment
    private Map<String, JSONObject> user = new HashMap<>();
    private Map<String, JSONObject> userApp = new HashMap<>();
    private Map<String, JSONObject> app = new HashMap<>();

    private static MemoryStorage instance = new MemoryStorage();

    private MemoryStorage() {
    }

    public static MemoryStorage getInstance() {
        return instance;
    }

    public void save(String key, JSONObject value) {
        String contentType = value.optString("type");
        if (contentType != null && (!contentType.contains("text/plain"))) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("key", value.get("key"));
                jsonObject.put("type", "application/json");
                jsonObject.put("value", Base64.encodeToString(value.toString().getBytes(), Base64.DEFAULT));
                getSegment(key).put(key, jsonObject);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        } else {
            getSegment(key).put(key, value);
        }

    }

    public JSONObject keySet(String url) {
        JSONObject result = new JSONObject();
        Map<String, JSONObject> segment = getSegment(url);
        try {
            result.put("totalItems", segment.size());
            JSONArray jsonArray = new JSONArray();

            for (Map.Entry<String, JSONObject> entry : segment.entrySet()) {
                JSONObject elem = new JSONObject();
                elem.put("key", entry.getKey().substring(entry.getKey().lastIndexOf("/") + 1));
                elem.put("modifiedDate", new Date().toString());
                elem.put("$ref", url);
                jsonArray.put(elem);
            }
            result.put("results", jsonArray);
            return result;

        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject findByKey(String key) {
        return getSegment(key).get(key);
    }

    public JSONObject delete(String url) {
        return getSegment(url).remove(url);
    }

    private Map<String, JSONObject> getSegment(String url) {
        if (url.startsWith(StorageDispatcher.USER_DATA)) return user;
        if (url.startsWith(StorageDispatcher.CLIENT_USER_DATA)) return userApp;
        if (url.startsWith(StorageDispatcher.CLIENT_DATA)) return app;
        throw new IllegalArgumentException("Invalid url");
    }

}
