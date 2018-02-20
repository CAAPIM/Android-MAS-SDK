/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.token;

import android.support.annotation.NonNull;
import android.util.Base64;

import org.json.JSONException;
import org.json.JSONObject;

public class IdTokenParser {

    private byte[] header;
    private byte[] payload;
    private byte[] signature;

    public IdTokenParser(@NonNull IdToken idToken) {
        byte[] token = idToken.getValue().getBytes();
        byte[][] splitToken = split(token);

        header = splitToken[0];
        payload = splitToken[1];
        signature = null;
        if (splitToken.length == 3) {
            signature = splitToken[2];
        }
    }

    public byte[] getHeader() {
        return header;
    }

    public byte[] getSignature() {
        return signature;
    }

    public byte[] getPayload() {
        return payload;
    }

    public JSONObject getPayloadAsJSONObject() throws JSONException {
        byte[] decodedPayload = Base64.decode(payload, Base64.URL_SAFE);
        String payloadData = new String(decodedPayload);
        return new JSONObject(payloadData);
    }

    private byte[][] split(byte[] token) {
        // We can "cheat".  We know the token is base64 URL encoded.
        // Instead of going through a crapload of bytes, we can convert the token into a string
        // and use a regex split on it.
        String tokenString = new String(token);
        String[] tokenParts = tokenString.split("[.]");

        if ((tokenParts.length < 2) || (tokenParts.length > 3)) {
            // The token is invalid, there's less than two parts or more than three parts of it.
            throw new IllegalArgumentException("Invalid token format");
        }

        // We use .getBytes().length on the strings to handle any UTF8 chars that are more than a single byte
        // representation, otherwise we overflow or truncate.
        byte[][] splitBytes = new byte[tokenParts.length][];
        splitBytes[0] = new byte[tokenParts[0].getBytes().length];
        splitBytes[1] = new byte[tokenParts[1].getBytes().length];
        System.arraycopy(tokenParts[0].getBytes(), 0, splitBytes[0], 0, tokenParts[0].getBytes().length);
        System.arraycopy(tokenParts[1].getBytes(), 0, splitBytes[1], 0, tokenParts[1].getBytes().length);

        if (splitBytes.length == 3) {
            splitBytes[2] = new byte[tokenParts[2].getBytes().length];
            System.arraycopy(tokenParts[2].getBytes(), 0, splitBytes[2], 0, tokenParts[2].getBytes().length);
        }

        return splitBytes;
    }
}
