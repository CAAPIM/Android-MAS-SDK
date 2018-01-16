/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.http;

import android.os.Build;
import android.util.Log;

import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASResponseBody;
import com.ca.mas.foundation.MASSecurityConfiguration;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSocketFactory;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class MAGHttpClient {

    public <T> MASResponse<T> execute(MASRequest request, MASSecurityConfiguration securityConfiguration) throws IOException {
        return execute(request, SSLSocketFactoryProvider.getInstance().createSSLSocketFactory(securityConfiguration));
    }

    public <T> MASResponse<T> execute(MASRequest request) throws IOException {
        return execute(request, SSLSocketFactoryProvider.getInstance().get(request.getURL()));
    }

    /**
     * Execute a request to the target API.
     *
     * @param request The request to execute
     * @param <T>     The parsed response type
     * @return The response to the request.
     * @throws IOException if any error occur or the connection was aborted.
     */
    public <T> MASResponse<T> execute(MASRequest request, SSLSocketFactory sslSocketFactory) throws IOException {
        final HttpURLConnection urlConnection = (HttpURLConnection) request.getURL().openConnection();

        if (DEBUG) {
            Log.d(TAG, String.format("API Request Url: %s", request.getURL()));
            Log.d(TAG, String.format("API Request Method: %s", request.getMethod()));
        }

        try {
            if (!request.isPublic()) {
                StorageProvider sp = StorageProvider.getInstance();
                TokenManager tm = sp.getTokenManager();
                String magIdentifier = tm.getMagIdentifier();
                if (magIdentifier != null) {
                    urlConnection.setRequestProperty(ServerClient.MAG_IDENTIFIER, magIdentifier);
                }
            }

            onConnectionObtained(urlConnection);
            if (request.getConnectionListener() != null) {
                request.getConnectionListener().onObtained(urlConnection);
            }

            //If not found in the MASSecurityConfiguration, the socket factory will be null
            if (urlConnection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) urlConnection).setSSLSocketFactory(sslSocketFactory);
            }

            if (ConfigurationManager.getInstance().getConnectionListener() != null) {
                ConfigurationManager.getInstance().getConnectionListener().onObtained(urlConnection);
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

            MASRequestBody body = request.getBody();
            if (body != null) {
                urlConnection.setDoOutput(true);

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                    if (body.getContentLength() > 0) {
                        urlConnection.setFixedLengthStreamingMode(body.getContentLength());
                    } else {
                        urlConnection.setChunkedStreamingMode(0);
                    }
                }
                if (body.getContentType() != null) {
                    urlConnection.setRequestProperty("Content-Type", body.getContentType().toString());
                }
                if (request.getConnectionListener() != null) {
                    request.getConnectionListener().onConnected(urlConnection);
                }
                if (ConfigurationManager.getInstance().getConnectionListener() != null) {
                    ConfigurationManager.getInstance().getConnectionListener().onConnected(urlConnection);
                }
                body.write(urlConnection.getOutputStream());
            } else {
                if (request.getConnectionListener() != null) {
                    request.getConnectionListener().onConnected(urlConnection);
                }
                if (ConfigurationManager.getInstance().getConnectionListener() != null) {
                    ConfigurationManager.getInstance().getConnectionListener().onConnected(urlConnection);
                }
            }

            final MASResponseBody responseBody = request.getResponseBody();

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
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                    //TODO While making a secure connection, android was falling back to SSLv3 from TLSv1 . It is a bug in android versions < 4.4
                    if (e.getCause() != null && e.getCause() instanceof SSLProtocolException) {
                        if (DEBUG)
                            Log.w(TAG, "SSLHandshakeException occurs, setting it to response 204");
                        responseCode = HttpsURLConnection.HTTP_NO_CONTENT;
                        responseMessage = null;
                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }

            final Map<String, List<String>> headers = urlConnection.getHeaderFields();

            final int finalResponseCode = responseCode;
            final String finalResponseMessage = responseMessage;
            return new MASResponse<T>() {

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
                public MASResponseBody<T> getBody() {
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
