/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import android.util.Base64;
import android.util.Log;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

public class JWTHmac {


    public JWTHmac () {

    }

    public static  boolean validateHMacSignature(byte[] header, byte[] payload, byte[] clientSecret, byte[] signature) throws JWTValidationException {

        byte[] signToCompare = JWTHmac.signData(header, payload, clientSecret);
        byte[] decodedSignature = Base64.decode(signature, Base64.URL_SAFE);

        if (!new String(signToCompare).equals(new String(decodedSignature))) {
            String error = logComparisonFailureError("Token Validation Failed: The signatures do not match", header, payload, signature, signToCompare);
            throw new JWTInvalidSignatureException(error);
        }

        return true;
    }

    private static byte[] signData(byte[] header, byte[] payload, byte[] secret) throws JWTValidationException {
        try{

            if (secret == null || secret.length == 0) {
                return null;
            }

            Mac mac;
            mac = Mac.getInstance("HMACSHA256");
            mac.init(new SecretKeySpec(secret, "HMACSHA256"));

            byte[] jwsSecuredInput = createSecuredInput(header, payload);

            byte[] signature;
            signature = mac.doFinal(jwsSecuredInput);
            return signature;

        } catch (Exception e) {
            throw new JWTInvalidSignatureException("Sign Data failed: " + e.getMessage(), e);
        }
    }

    private static byte[] createSecuredInput(byte[] header, byte[] payload) {

        byte separator[] = ".".getBytes();
        byte jwsSecuredInput[] = new byte[header.length + separator.length + payload.length];

        System.arraycopy(header, 0, jwsSecuredInput, 0, header.length);
        System.arraycopy(separator, 0, jwsSecuredInput, header.length, separator.length);
        System.arraycopy(payload, 0, jwsSecuredInput, header.length + separator.length, payload.length);

        return jwsSecuredInput;
    }

    private static String logComparisonFailureError(String error, byte[] header, byte[] payload, byte[] signature, byte[] comparedSignature) {
        StringBuilder errorResponse = new StringBuilder();
        errorResponse.append(error).append("\n  Received Token Header: [").
                append(header == null ? " (No header)" : new String(header)).
                append("]").append("\n    Payload: [").
                append(payload == null ? "(No payload)" : new String(payload)).append("]").append("\n    Signature: [").
                append(signature == null ? "(No signature)" : new String(signature)).append("]").
                append("\n    Generated Signature: [").
                append(comparedSignature == null ? "(No comparison signature)" : new String(Base64.encodeToString(comparedSignature, Base64.URL_SAFE))).
                append("]");
        if (DEBUG) Log.w(TAG, error + errorResponse);
        return errorResponse.toString();
    }

}
