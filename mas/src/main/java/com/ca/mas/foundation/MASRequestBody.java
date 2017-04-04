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
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import net.minidev.json.reader.JsonWriter;
import net.minidev.json.reader.JsonWriterI;

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

    static MASRequestBody jwtClaimsBody(final MASClaims claims, final MAGRequestBody body) {

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
                MASClaims.MASClaimsBuilder builder = new MASClaims.MASClaimsBuilder(claims);
                builder.claim(MASClaims.CONTENT, body.getContentAsJsonValue());
                if (body.getContentType() != null) {
                    builder.claim(MASClaims.CONTENT_TYPE, body.getContentType().getMimeType());
                }
                MASClaims masClaims = builder.build();

                String compactJws = null;
                try {
                    compactJws = MAS.sign(masClaims);
                } catch (MASException e) {
                    throw new IOException(e);
                }

                outputStream.write(compactJws.getBytes(getContentType().getCharset()));
            }
        };
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

            @Override
            public Object getContentAsJsonValue() {
                return requestBody.getContentAsJsonValue();
            }
        };
    }

}
