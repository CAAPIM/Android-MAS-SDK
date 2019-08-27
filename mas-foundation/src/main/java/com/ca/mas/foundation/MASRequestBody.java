/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.util.Base64;
import android.util.Log;
import android.util.Pair;

import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.util.FileUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.security.PrivateKey;
import java.util.List;
import java.util.Map;

import static com.ca.mas.core.io.Charsets.UTF8;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public abstract class MASRequestBody {

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


    public Object getContentAsJsonValue() {
        return null;
    }

    /**
     * @param body The request body as byte[]
     * @return A new request body with content of byte[]
     */
    public static MASRequestBody byteArrayBody(final byte[] body) {
        return new MASRequestBody() {

            private final byte[] content = body;

            @Override
            public ContentType getContentType() {
                return null;
                //return ContentType.APPLICATION_OCTET_STREAM;
            }

            @Override
            public long getContentLength() {
                return content.length;
            }

            @Override
            public void write(OutputStream outputStream) throws IOException {
                outputStream.write(content);
            }

            @Override
            public Object getContentAsJsonValue() {
                return Base64.encodeToString(body, Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE);
            }
        };
    }


    /**
     * @param body The request body as {@link String}
     * @return A new request body with content of {@link String}
     */
    public static MASRequestBody stringBody(final String body) {
        return new MASRequestBody() {

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

            @Override
            public Object getContentAsJsonValue() {
                return body;
            }
        };
    }

    /**
     * @param jsonObject The request body as {@link JSONObject}
     * @return A new request body with content of {@link JSONObject}
     */
    public static MASRequestBody jsonBody(final JSONObject jsonObject) {
        return new MASRequestBody() {

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

            @Override
            public Object getContentAsJsonValue() {
                return jsonObject;
            }
        };
    }

    public static MASRequestBody jsonArrayBody(final JSONArray jsonArray) {
        MASRequestBody result = new MASRequestBody() {
            private byte[] content = jsonArray.toString().getBytes(getContentType().getCharset());

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
                outputStream.write(content);
            }
        };

        return result;
    }

    /**
     * @param form The request body as url encoded form body.
     * @return A new request with content of a url encoded form.
     */
    public static MASRequestBody urlEncodedFormBody(final List<? extends Pair<String, String>> form) {

        return new MASRequestBody() {

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

            @Override
            public Object getContentAsJsonValue() {
                JSONObject jsonObject = new JSONObject();
                for (Pair<String, String> pair : form) {
                    if (pair.first != null) {
                        try {
                            JSONArray jsonArray = (JSONArray) jsonObject.opt(pair.first);
                            if (jsonArray == null) {
                                jsonArray = new JSONArray();
                                jsonObject.put(pair.first, jsonArray);
                            }
                            if (pair.second != null) {
                                jsonArray.put(pair.second);
                            }
                        } catch (JSONException e) {
                            //ignore
                        }
                    }
                }
                return jsonObject;
            }
        };
    }

    static MASRequestBody jwtClaimsBody(final MASClaims claims, final PrivateKey privateKey, final MASRequestBody body) {

        return new MASRequestBody() {

            @Override
            public ContentType getContentType() {
                return ContentType.TEXT_PLAIN;
            }

            @Override
            public long getContentLength() {
                return -1; //Size it unknown
            }

            @Override
            public void write(OutputStream outputStream) throws IOException {
                MASClaimsBuilder builder = new MASClaimsBuilder(claims);
                builder.claim(MASClaimsConstants.CONTENT, body.getContentAsJsonValue());
                if (body.getContentType() != null) {
                    builder.claim(MASClaimsConstants.CONTENT_TYPE, body.getContentType().getMimeType());
                }
                MASClaims masClaims = builder.build();

                String compactJws = null;
                try {
                    if (privateKey == null) {
                        compactJws = MAS.sign(masClaims);
                    } else {
                        compactJws = MAS.sign(masClaims, privateKey);
                    }
                } catch (MASException e) {
                    throw new IOException(e);
                }

                outputStream.write(compactJws.getBytes(getContentType().getCharset()));
            }
        };
    }



    /**
     * @param multipart The multipart/form-data as request body.
     * @return A new request with content of a url encoded form.
     */
    public static MASRequestBody multipartBody(final MultiPart multipart, final MASProgressListener progressListener) throws MASException {
        if(multipart == null){
            throw new MASException(new Throwable("Multipart is null"));
        }

        try {
            return new MASRequestBody() {

                public final String twoHyphens = "--";
                public final String lineEnd = "\r\n";
                public String multipart_separator = twoHyphens+MASConstants.MAS_BOUNDARY+lineEnd;
                private final byte[] content = getContent();

                private byte[] getContent() throws MASException, IOException {
                    StringBuilder formParams = new StringBuilder();
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    if(!multipart.getFormFields().isEmpty()) {

                        for (Map.Entry<String, String> entry : multipart.getFormFields().entrySet()) {
                            formParams.append(multipart_separator);
                            formParams.append("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"" + lineEnd);
                            formParams.append(lineEnd);
                            formParams.append(entry.getValue());
                            formParams.append(lineEnd);
                        }
                    }

                    //formParams.append(multipart_separator);
                    output.write(formParams.toString().getBytes());

                    if(multipart.getFilePart() != null) {

                        for (int i= 0; i< multipart.getFilePart().size(); i++) {
                            output.write(multipart_separator.getBytes());
                            output.write(("Content-Disposition: form-data; name=\"" + multipart.getFilePart().get(i).getFieldName() + "\"; filename=\"" + multipart.getFilePart().get(i).getFileName() + "\"" + lineEnd).getBytes());
                            output.write(("Content-Type: " + multipart.getFilePart().get(i).getFileType() + lineEnd).getBytes());
                            output.write(("Content-Transfer-Encoding: binary" + lineEnd).getBytes());
                            output.write((lineEnd).getBytes());

                            try {

                                byte[] bytes = FileUtils.getBytesFromPath(multipart.getFilePart().get(i).getFilePath());
                                output.write(bytes);
                                output.write(lineEnd.getBytes());
                                output.write((lineEnd).getBytes());

                            } catch (IOException e) {
                                progressListener.onError(new MAGError(e));
                                throw new MASException(e);
                            }

                        }
                        output.write((twoHyphens + MASConstants.MAS_BOUNDARY + twoHyphens + lineEnd).getBytes());

                    }

                    return output.toByteArray();
                }

                @Override
                public ContentType getContentType() {
                    return ContentType.MULTIPART_FORM_DATA;
                }

                @Override
                public long getContentLength() {
                    return content.length+multipart_separator.length();
                }

                @Override
                public void write(OutputStream outputStream) throws IOException {

                    InputStream stream = new ByteArrayInputStream(content);

                    byte buf[] = new byte[1024];
                    int progress = 0;
                    int bytesRead = 0;
                    BufferedInputStream bufInput = new BufferedInputStream(stream);

                    while ((bytesRead = bufInput.read(buf)) != -1) {
                        // write output
                        outputStream.write(buf, 0, bytesRead);
                        outputStream.flush();
                        progress += bytesRead;
                        if(progressListener != null) {
                            progressListener.onProgress("" + (int) ((progress * 100) / content.length)); // sending progress percent to publishProgress
                        }
                    }
                    outputStream.write((multipart_separator).getBytes());
                    outputStream.flush();
                    if(progressListener != null) {
                        progressListener.onComplete();
                    }
                    outputStream.close();

                }
            };
        } catch (MASException | IOException e) {
            if(progressListener != null) {
                progressListener.onError(new MAGError(e));
            }
            throw new MASException(e);
        }
    }




}
