/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.util.Log;

import com.ca.mas.core.io.IoUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * An API HTTP response body.
 *
 * @param <T> The type of the parsed response
 */
public class MASResponseBody<T> {

    /**
     * Default max response sze.
     */
    private static final int DEFAULT_MAX_RESPONSE_SIZE = 10485760;

    /**
     * The response Content type
     */
    private String contentType;

    /**
     * The response Content length
     */
    private int contentLength;

    /**
     * The response content
     */
    protected byte[] buffer = {};

    protected File file;

    /**
     * Return the parsed response content.
     *
     * @return The parsed response object
     */
    public T getContent() {

        String retValue = "";
        if (contentType != null) {

            if (contentType.contains("application/json")) {
                try {

                    if (buffer == null || buffer.length == 0) {
                        return null;
                    }
                    retValue = new String(buffer);
                    return (T) new JSONObject(retValue);
                } catch (JSONException e) {

                    try {
                        return (T) new JSONArray(retValue);
                    } catch (JSONException e1) {
                        throw new RuntimeException(e1);
                    }
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
    public void read(HttpURLConnection httpURLConnection) throws IOException {
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
     * Immplementation for reading the response body containing a file. Read the http response as stream write into a file.
     *
     * @param httpURLConnection The HttpURLConnection to communicate with MAG
     * @throws IOException if an IO exception occurs during the reading and storing the input stream.
     */

    public String read(HttpURLConnection httpURLConnection, MASRequest request) throws IOException {
        this.contentType = httpURLConnection.getContentType();
        this.contentLength = httpURLConnection.getContentLength();

        if(contentLength <= 0){
            throw new IOException("Content lenght : "+ contentLength);
        }
        MASFileObject downloadFile = request.getDownloadFile();
        String fileName = downloadFile.getFileName();

        file = new File(downloadFile.getFilePath(), fileName);


        InputStream input = httpURLConnection.getInputStream();

        // Output stream to write file
        OutputStream output = new FileOutputStream(file);

        byte data[] = new byte[4069];

        long total = 0;

        int count=0;
        while ((count = input.read(data)) != -1) {
            total += count;
            // publishing the progress....
            int progress = (int) ((total * 100) / contentLength);
            if(request.getProgressListener() != null) {
                request.getProgressListener().onProgress("" + progress);
            }
            // writing data to file
            output.write(data, 0, count);
        }
        // flushing output
        output.flush();
        // closing streams
        output.close();
        input.close();
        if(request.getProgressListener() != null) {
            request.getProgressListener().onComplete();
        }
        return "Downloaded at: " + file.getAbsolutePath();
    }



    /**
     * @return Return a new ResponseBody with byte[] content.
     */
    public static MASResponseBody<byte[]> byteArrayBody() {

        return new MASResponseBody<byte[]>() {

            @Override
            public byte[] getContent() {
                return buffer;
            }
        };
    }

    /**
     * @return Return a new ResponseBody with {@link JSONObject} content.
     */
    public static MASResponseBody<JSONObject> jsonBody() {

        return new MASResponseBody<JSONObject>() {

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
     * @return Return a new ResponseBody with {@link JSONObject} content.
     */
    public static MASResponseBody<JSONArray> jsonArrayBody() {

        return new MASResponseBody<JSONArray>() {

            @Override
            public JSONArray getContent() {
                if (buffer == null || buffer.length == 0) {
                    return new JSONArray();
                }
                try {
                    return new JSONArray(new String(buffer));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }


    /**
     * @return Return a new ResponseBody with {@link String} content.
     */
    public static MASResponseBody<String> stringBody() {

        return new MASResponseBody<String>() {

            @Override
            public String getContent() {
                if (buffer == null || buffer.length == 0) {
                    return "";
                }
                return new String(buffer);
            }
        };
    }

    /**
     * @return Return a new ResponseBody with {@link File} content.
     */
    public static MASResponseBody<File> fileBody() {

        return new MASResponseBody<File>() {

            @Override
            public File getContent() {
                if (file == null || file.length() == 0) {
                    return null;
                }
                return file;
            }
        };
    }

}
