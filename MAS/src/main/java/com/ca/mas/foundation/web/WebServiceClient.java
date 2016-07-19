/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.web;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGRequestBody;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.foundation.MASResultReceiver;
import com.ca.mas.foundation.util.FoundationUtil;

import org.json.JSONObject;

import java.util.Map;

/**
 * <p><b>WebServiceClient</b> is the concrete implementation of the {@link MASWebServiceClient} 'CRUD' interface and
 * leverage the MAG SDK to perform web service calls via the MAG server.</p>
 */
public class WebServiceClient implements MASWebServiceClient {

    private static String TAG = WebServiceClient.class.getSimpleName();
    private final Context mContext;

    /**
     * <b>Description:</b> Required constructor.
     *
     * @param context the Android runtime environment context for this app.
     */
    public WebServiceClient(Context context) {
        mContext = context.getApplicationContext();
    }

    @Override
    public void post(WebServiceRequest request, MASResultReceiver<JSONObject> result) {
        // CREATE
        Uri uri = request.getUri();
        MAGRequest.MAGRequestBuilder builder = new MAGRequest.MAGRequestBuilder(uri);
        builder.post(MAGRequestBody.jsonBody(request.getBody())).responseBody(MAGResponseBody.jsonBody());
        processRequest(builder, result);
    }

    @Override
    public void get(WebServiceRequest request, MASResultReceiver<JSONObject> result) {
        // READ
        processRequest(request, result);
    }

    @Override
    public void put(WebServiceRequest request, MASResultReceiver<JSONObject> result) {
        // UPDATE
        Uri uri = request.getUri();
        MAGRequest.MAGRequestBuilder builder = new MAGRequest.MAGRequestBuilder(uri);
        builder.put(MAGRequestBody.jsonBody(request.getBody())).responseBody(MAGResponseBody.jsonBody());
        processRequest(builder, result);
    }

    @Override
    public void delete(WebServiceRequest request, MASResultReceiver<JSONObject> result) {
        // DELETE
        Uri uri = request.getUri();
        MAGRequest.MAGRequestBuilder builder = new MAGRequest.MAGRequestBuilder(uri);
        builder.delete(null);
        processRequest(builder, result);
    }

    /*
    Helper method that does the actual MAG server call.
     */
    private void processRequest(MAGRequest.MAGRequestBuilder builder, final MASResultReceiver<JSONObject> result) {
        MobileSso mobileSso = FoundationUtil.getMobileSso();
        MAGRequest magRequest = builder.build();
        Log.d(TAG, "Request URL: " + magRequest.getURL().toString());
        mobileSso.processRequest(magRequest, result);
    }

    /*
    Helper method that does the actual MAG server call.
     */
    private void processRequest(WebServiceRequest request, MASResultReceiver<JSONObject> result) {
        MobileSso mobileSso = FoundationUtil.getMobileSso();
        MAGRequest.MAGRequestBuilder builder = createWebServiceCall(request);
        MAGRequest magRequest = builder.build();
        Log.d(TAG, "Request URL: " + magRequest.getURL().toString());
        mobileSso.processRequest(magRequest, result);
    }

    /*
    Helper method that constructs the actual web service call.
     */
    private MAGRequest.MAGRequestBuilder createWebServiceCall(WebServiceRequest request) {
        Uri uri = request.getUri();
        MAGRequest.MAGRequestBuilder builder = new MAGRequest.MAGRequestBuilder(uri);
        builder.responseBody(MAGResponseBody.jsonBody());
        addHeaders(request.getHeaders(), builder);
        return builder;
    }

    /*
    Helper method for adding any necessary headers.
     */
    private void addHeaders(Map<String, String> headers, MAGRequest.MAGRequestBuilder builder) {
        if (headers != null && !headers.isEmpty()) {
            // add any headers that have been added.
            for (String key : headers.keySet()) {
                String value = headers.get(key);
                builder.header(key, value);
                Log.d(TAG, "Request Header: " + key + " = " + value);
            }
        }
    }
}
