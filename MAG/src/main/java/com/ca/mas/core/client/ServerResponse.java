/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.client;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Represents a JSON response from the token server.
 * <p/>
 * This is either a JSON success or error response from the request_token or request_token_sso method,
 * or a JSON error response from the register_device method.
 */
public class ServerResponse {
    private static final String TAG = ServerResponse.class.getCanonicalName();

    protected int status;
    protected int errorCode;


    protected final String json;
    protected final JSONObject parsed;

    public ServerResponse(int status, String json) throws JSONException {
        this(status, 0, json);
    }

    /**
     * Create a JSON response from the specified HTTP status code and JSON string.
     *
     * @param status HTTP status code, eg 401.
     * @param errorCode Header x-ca-err code
     * @param json string respresenting a single JSON object, eg <pre>{ "error":"invalid_request", "error_description":"Validation error" }</pre>
     * @throws JSONException if the JSON string cannot be parsed as a JSON object
     */

    public ServerResponse(int status, int errorCode, String json) throws JSONException {
        this.errorCode = errorCode;
        this.status = status;
        if (json == null)
            throw new NullPointerException("json");
        this.json = json;
        Object got = new JSONTokener(json).nextValue();
        if (got == null)
            throw new JSONException("JSON response parsed to NULL");
        if (got instanceof JSONObject) {
            this.parsed = (JSONObject) got;
        } else {
            Log.d(TAG, "JSON response was not of type JSONObject: type=" + got.getClass());
            throw new JSONException("JSON response did not contain a JSON object");
        }
    }

    /**
     * @return the HTTP status.
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the raw JSON string.
     */
    public String getJson() {
        return json;
    }

    /**
     * Check for an error field in the JSON response.
     * <b>Note: you should probably also check for a non-2xx {@link #getStatus()} value.</b>
     * @return true if the JSON object includes a string field named "error".
     */
    public boolean isError() {
        return 0 != errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }

}
