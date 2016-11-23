/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.util.Pair;

import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.http.MAGRequestBody;

import org.json.JSONObject;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public abstract class MASRequestBody extends MAGRequestBody {

    /**
     * @param body The request body as byte[]
     * @return A new request body with content of byte[]
     */
    public static MASRequestBody byteArrayBody(final byte[] body) {
        return transform(MAGRequestBody.byteArrayBody(body));
    }


    /**
     * @param body The request body as {@link String}
     * @return A new request body with content of {@link String}
     */
    public static MASRequestBody stringBody(final String body) {
        return transform(MAGRequestBody.stringBody(body));
    }

    /**
     * @param jsonObject The request body as {@link JSONObject}
     * @return A new request body with content of {@link JSONObject}
     */
    public static MASRequestBody jsonBody(final JSONObject jsonObject) {
        return transform(MAGRequestBody.jsonBody(jsonObject));
    }

    /**
     * @param form The request body as url encoded form body.
     * @return A new request with content of a url encoded form.
     */
    public static MASRequestBody urlEncodedFormBody(final List<? extends Pair<String, String>> form) {
        return transform(MAGRequestBody.urlEncodedFormBody(form));

    }

    private static MASRequestBody transform(final MAGRequestBody requestBody) {
        return new MASRequestBody() {

            @Override
            public ContentType getContentType() {
                return requestBody.getContentType();
            }

            @Override
            public long getContentLength() {
                return requestBody.getContentLength();
            }

            @Override
            public void write(OutputStream outputStream) throws IOException {
                requestBody.write(outputStream);
            }
        };
    }

}
