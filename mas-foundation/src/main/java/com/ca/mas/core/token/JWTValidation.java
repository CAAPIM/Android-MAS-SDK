/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.error.MAGErrorCode;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class JWTValidation {

    private static final String TAG = JWTValidation.class.getName();
    public static final String ALG = "alg";
    public static final String EXP = "exp";
    public static final String AUD = "aud";
    public static final String AZP = "azp";

    public enum Algorithm {
        HS256(1), RSA(2);
        private int value;

        private Algorithm(int value) {
            this.value = value;
        }
    }

    public static boolean isIdTokenExpired(@NonNull IdToken idToken) {
        if (idToken.getType().equals(IdToken.JWT_DEFAULT)) {
            IdTokenDef tokenDef = new IdTokenDef(idToken);
            try {
                JSONObject jsonObject = tokenDef.getPayloadAsJSONObject();
                String expireDateString = jsonObject.getString(EXP);
                if (Long.valueOf(expireDateString) < new Date().getTime() / 1000) {
                    return true;
                }
                return false;
            } catch (JSONException e) {
                //Assume the token is expired.
                return true;
            }
        }
        return false;
    }

    public static boolean validateIdToken(IdToken idToken, String deviceIdentifier, String clientId, String clientSecret) throws JWTValidationException {

        boolean isValid = false;

        IdTokenDef idTokenDef = new IdTokenDef(idToken);

        boolean payloadValid = validateJwtPayload(idTokenDef, deviceIdentifier, clientId);

        String algorithm = getAlgorithm(new String(Base64.decode(idTokenDef.getHeader(), Base64.URL_SAFE)));
        boolean signatureValid = false;
        if (algorithm != null) {
            if (algorithm.equals(Algorithm.HS256.toString())) {
                signatureValid = JWTHmac.validateHMacSignature(idTokenDef.getHeader(),
                        idTokenDef.getPayload(),
                        clientSecret.getBytes(),
                        idTokenDef.getSignature());
            } else {
                // - US514785
                signatureValid = true;
                Log.w(TAG, algorithm +" not supported");
            }
        }

        isValid = payloadValid & signatureValid;

        return isValid;
    }


    private static String getAlgorithm(String header) throws JWTValidationException {
        try {
            JSONObject jsonObject = new JSONObject(header);
            return jsonObject.getString(ALG);
        } catch (JSONException e) {
            Log.w(TAG, "JWT header is not JSON Object");
            throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, e.getMessage(), e);
        }
    }

    private static boolean validateJwtPayload(IdTokenDef idTokenDef, String deviceIdentifier, String clientId) throws JWTValidationException {

        try {
            JSONObject jsonObject = idTokenDef.getPayloadAsJSONObject();
            String expireDateString = jsonObject.getString(EXP);
            String audString = jsonObject.getString(AUD);
            String azpString = jsonObject.getString(AZP);

            if (!audString.equals(clientId)) {
                Log.w(TAG, "JWT aud is invalid");
                throw new JWTInvalidAUDException("Failed to validate JWT Token: \"aud\" doesn't match client_id!");
            }

            if (!azpString.equals(deviceIdentifier)) {
                Log.w(TAG, "JWT azp is invalid");
                throw new JWTInvalidAZPException("Failed to validate JWT Token: \"azp\" doesn't match device identifier!");
            }

            if (Long.valueOf(expireDateString) < new Date().getTime() / 1000) {
                Log.w(TAG, "JWT expired");
                throw new JWTExpiredException("Failed to validate JWT Token: token expired!");
            }

        } catch (JSONException e) {
            Log.w(TAG, "JWT payload is not valid JSON object");
            throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, e.getMessage(), e);
        }

        return true;
    }

    private static class IdTokenDef {
        byte[] header;
        byte[] payload;
        byte[] signature;

        public IdTokenDef(@NonNull IdToken idToken) {
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
                return null;
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

}
