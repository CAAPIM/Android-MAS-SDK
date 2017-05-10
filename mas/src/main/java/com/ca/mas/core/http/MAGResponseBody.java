/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.http;

import android.util.Log;

import com.ca.mas.core.io.IoUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * An API HTTP response body.
 *
 * @param <T> The type of the parsed response
 */
public class MAGResponseBody<T> {

    /**
     * Default max response sze.
     */
    private static final int DEFAULT_MAX_RESPONSE_SIZE = 10485760;

    /**
     * The {@link HttpURLConnection} to communicate with MAG
     */
    protected HttpURLConnection httpURLConnection;

    /**
     * The response Content type
     */
    protected String contentType;

    /**
     * The response Content length
     */
    protected int contentLength;

    /**
     * The response content
     */
    protected byte[] buffer = {};

    /**
     * Return the parsed response content.
     *
     * @return The parsed response object
     */
    public T getContent() {
        if (contentType != null) {
            if (contentType.contains("application/json")) {
                try {
                    if (buffer == null || buffer.length == 0) {
                        return (T) new JSONObject();
                    }
                    return (T) new JSONObject(new String(buffer));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
            if (contentType.contains("text/plain")) {
                if (buffer == null || buffer.length == 0) {
                    return (T) "";
                }
                return (T) new String(buffer);
            }
        }
        return (T) buffer;
    }

    /**
     * Return the un-parsed response.
     *
     * @return The raw content of the response body
     */
    public byte[] getRawContent() {
        return buffer;
    }

    /**
     * Returns the content length in bytes specified by the response header field.
     * Please refer to {@link URLConnection#getContentLength()} for detail.
     *
     * @return the value of the response header field content-length.
     */
    public int getContentLength() {
        return contentLength;
    }

    /**
     * Returns the MIME-type of the content specified by the response header field
     * Please refer to {@link URLConnection#getContentType()} for detail.
     * @return the value of the response header field content-type.
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Default implementation of reading the response body. Read the http response as stream and buffer
     * the content as byte[].
     *
     * @param httpURLConnection The HttpURLConnection to communicate with MAG
     * @throws IOException if an IO exception occurs during the reading and buffer the input stream.
     */
    protected void read(HttpURLConnection httpURLConnection) throws IOException {
        this.httpURLConnection = httpURLConnection;
        this.contentType = httpURLConnection.getContentType();
        this.contentLength = httpURLConnection.getContentLength();

        InputStream inputStream = httpURLConnection.getErrorStream();
        if (inputStream == null) {
            inputStream = httpURLConnection.getInputStream();
        }
        buffer = IoUtils.slurpStream(inputStream, DEFAULT_MAX_RESPONSE_SIZE);

        if (DEBUG) {
            String s = "";
            try {
                s = new String(buffer);
                JSONObject j = new JSONObject(s);
                Log.d(TAG, String.format("Response content: %s", j.toString(4)));
            } catch (JSONException ignore) {
            }
            Log.d(TAG, String.format("Response content: %s", s));
        }

    }


    /**
     * @return Return a new ResponseBody with byte[] content.
     */
    public static MAGResponseBody<byte[]> byteArrayBody() {

        return new MAGResponseBody<byte[]>() {

            @Override
            public byte[] getContent() {
                return buffer;
            }
        };
    }

    /**
     * @return Return a new ResponseBody with {@link JSONObject} content.
     */
    public static MAGResponseBody<JSONObject> jsonBody() {

        return new MAGResponseBody<JSONObject>() {

            @Override
            public JSONObject getContent() {
                if (buffer == null || buffer.length == 0) {
                    return new JSONObject();
                }
                try {
                    return new JSONObject(new String(buffer));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    /**
     * @return Return a new ResponseBody with {@link String} content.
     */
    public static MAGResponseBody<String> stringBody() {

        return new MAGResponseBody<String>() {

            @Override
            public String getContent() {
                if (buffer == null || buffer.length == 0) {
                    return "";
                }
                return new String(buffer);
            }
        };
    }

}
