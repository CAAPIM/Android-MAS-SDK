/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.http;

import android.util.Log;
import android.util.Pair;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.util.List;

import static com.ca.mas.core.io.Charsets.UTF8;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;
/**
 * Base class for http api request.
 */
public abstract class MAGRequestBody {

    /**
     * @return Returns the content type of the POST or PUT body.
     */
    public abstract ContentType getContentType();

    /**
     * @return Returns the content length of the POST or PUT body.
     */
    public abstract long getContentLength();

    /**
     * Writing data to the http url connection, the output stream is retrieved by
     * {@link HttpURLConnection#getOutputStream()}
     *
     * @param outputStream The output stream to write data.
     * @throws IOException if an error occurs while writing to this stream.
     */
    public abstract void write(OutputStream outputStream) throws IOException;

    /**
     * @param body The request body as byte[]
     * @return A new request body with content of byte[]
     */
    public static MAGRequestBody byteArrayBody(final byte[] body) {
        return new MAGRequestBody() {

            private final byte[] content = body;

            @Override
            public ContentType getContentType() {
                return null;
            }

            @Override
            public long getContentLength() {
                return content.length;
            }

            @Override
            public void write(OutputStream outputStream) throws IOException {
                outputStream.write(content);
            }
        };
    }


    /**
     * @param body The request body as {@link String}
     * @return A new request body with content of {@link String}
     */
    public static MAGRequestBody stringBody(final String body) {
        return new MAGRequestBody() {

            private final byte[] content = body.getBytes(getContentType().getCharset());

            @Override
            public ContentType getContentType() {
                return ContentType.TEXT_PLAIN;
            }

            @Override
            public long getContentLength() {
                return content.length;
            }

            @Override
            public void write(OutputStream outputStream) throws IOException {
                if (DEBUG) Log.d(TAG, String.format("Content: %s", body));
                outputStream.write(content);
            }
        };
    }

    /**
     * @param jsonObject The request body as {@link JSONObject}
     * @return A new request body with content of {@link JSONObject}
     */
    public static MAGRequestBody jsonBody(final JSONObject jsonObject) {
        return new MAGRequestBody() {

            private final byte[] content = jsonObject.toString().getBytes(getContentType().getCharset());

            @Override
            public ContentType getContentType() {
                return ContentType.APPLICATION_JSON;
            }

            @Override
            public long getContentLength() {
                return content.length;
            }

            @Override
            public void write(OutputStream outputStream) throws IOException {
                if (DEBUG) {
                    try {
                        Log.d(TAG, String.format("Content: %s", jsonObject.toString(4)));
                    } catch (JSONException ignore) {
                    }
                }
                outputStream.write(content);
            }
        };
    }

    /**
     * @param form The request body as url encoded form body.
     * @return A new request with content of a url encoded form.
     */
    public static MAGRequestBody urlEncodedFormBody(final List<? extends Pair<String, String>> form) {

        return new MAGRequestBody() {

            private final byte[] content = getContent();

            private byte[] getContent() {
                StringBuilder sb = new StringBuilder();
                for (Pair<String, String> pair : form) {
                    String name;
                    try {
                        name = URLEncoder.encode(pair.first, UTF8.name());
                        String value = pair.second == null ? null : URLEncoder.encode(pair.second, UTF8.name());
                        if (sb.length() > 0) {
                            sb.append("&");
                        }
                        sb.append(name);
                        if (value != null) {
                            sb.append("=");
                            sb.append(value);
                        }
                    } catch (UnsupportedEncodingException e) {
                        throw new RuntimeException(e);
                    }
                }
                return sb.toString().getBytes(getContentType().getCharset());
            }

            @Override
            public ContentType getContentType() {
                return ContentType.APPLICATION_FORM_URLENCODED;
            }

            @Override
            public long getContentLength() {
                return content.length;
            }

            @Override
            public void write(OutputStream outputStream) throws IOException {
                if (DEBUG) Log.d(TAG, String.format("Content: %s", new String(getContent())));
                outputStream.write(content);
            }
        };
    }


}
