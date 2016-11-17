/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.http;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.io.ssl.MAGSocketFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

public class MAGHttpClient {

    private SSLSocketFactory sslSocketFactory;

    public MAGHttpClient(Context context) {
        sslSocketFactory = new MAGSocketFactory(context).createSSLSocketFactory();
    }


    public MAGHttpClient() {
    }

    /**
     * Execute a request to the target API.
     *
     * @param request The request to execute
     * @param <T>     The parsed response type
     * @return The response to the request.
     * @throws IOException if any error occur or the connection was aborted.
     */
    public <T> MAGResponse<T> execute(MAGRequest request) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) request.getURL().openConnection();

        if (DEBUG) Log.d(TAG, String.format("API Request Url: %s", request.getURL()));

        try {
            onConnectionObtained(urlConnection);
            if (request.getConnectionListener() != null) {
                request.getConnectionListener().onObtained(urlConnection);
            }
            if (ConfigurationManager.getInstance().getConnectionListener() != null) {
                ConfigurationManager.getInstance().getConnectionListener().onObtained(urlConnection);
            }

            if (urlConnection instanceof HttpsURLConnection && sslSocketFactory != null) {
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslSocketFactory);
            }
            urlConnection.setRequestMethod(request.getMethod());
            urlConnection.setDoInput(true);
            for (String key : request.getHeaders().keySet()) {
                if (request.getHeaders().get(key) != null) {
                    for (String value : request.getHeaders().get(key)) {
                        urlConnection.setRequestProperty(key, value);
                    }
                }
            }

            if (request.getBody() != null) {
                urlConnection.setDoOutput(true);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    if (request.getBody().getContentLength() > 0) {
                        urlConnection.setFixedLengthStreamingMode(request.getBody().getContentLength());
                    } else {
                        urlConnection.setChunkedStreamingMode(0);
                    }
                }
                if (request.getBody().getContentType() != null) {
                    urlConnection.setRequestProperty("Content-Type", request.getBody().getContentType().toString());
                }
                if (request.getConnectionListener() != null) {
                    request.getConnectionListener().onConnected(urlConnection);
                }
                if (ConfigurationManager.getInstance().getConnectionListener() != null) {
                    ConfigurationManager.getInstance().getConnectionListener().onConnected(urlConnection);
                }
                request.getBody().write(urlConnection.getOutputStream());
            } else {
                if (request.getConnectionListener() != null) {
                    request.getConnectionListener().onConnected(urlConnection);
                }
                if (ConfigurationManager.getInstance().getConnectionListener() != null) {
                    ConfigurationManager.getInstance().getConnectionListener().onConnected(urlConnection);
                }
            }

            final MAGResponseBody responseBody = request.getResponseBody();

            int responseCode;
            String responseMessage;
            try {
                responseCode = urlConnection.getResponseCode();
                responseMessage = urlConnection.getResponseMessage();
                if (DEBUG) {
                    Log.d(TAG, String.format("Response code: %d", responseCode));
                    Log.d(TAG, String.format("Response message: %s", responseMessage));
                }
                responseBody.read(urlConnection);
            } catch (SSLHandshakeException e) {
                //Related to MCT-104 & MCT-323
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP){
                    if (DEBUG) Log.w(TAG, "SSLHandshakeException occurs, setting it to response 204");
                    responseCode = HttpsURLConnection.HTTP_NO_CONTENT;
                    responseMessage = null;
                } else {
                    throw e;
                }
            }

            final Map<String, List<String>> headers = urlConnection.getHeaderFields();

            final int finalResponseCode = responseCode;
            final String finalResponseMessage = responseMessage;
            return new MAGResponse<T>() {

                @Override
                public Map<String, List<String>> getHeaders() {
                    return headers;
                }

                @Override
                public int getResponseCode() {
                    return finalResponseCode;
                }

                @Override
                public String getResponseMessage() {
                    return finalResponseMessage;
                }


                @Override
                public MAGResponseBody<T> getBody() {
                    return responseBody;
                }

            };

        } finally {
            urlConnection.disconnect();
        }
    }

    protected void onConnectionObtained(HttpURLConnection connection) {

    }


}
