/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import android.util.Base64;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class JWTRs256 {

    private JWTRs256() {
        throw new IllegalAccessError("Utility class");
    }

    static  boolean validateRS256Signature(byte[] header, byte[] payload, byte[] clientSecret, byte[] signature) throws JWTValidationException {

        byte[] signToCompare = JWTRs256.signData(header, payload, clientSecret);
        byte[] decodedSignature = Base64.decode(signature, Base64.URL_SAFE);

        if (!new String(signToCompare).equals(new String(decodedSignature))) {
            throw new JWTInvalidSignatureException("Token Validation Failed: The signatures do not match");
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

}
