/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import android.util.Base64;

import com.ca.mas.core.context.MssoContext;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

class JWTHmacValidator implements  JWTValidator{

    @Override
    public boolean validate(MssoContext context,IdToken token) throws JWTValidationException {

        IdTokenDef idTokenDef = new IdTokenDef(token);
        byte[] header = idTokenDef.getHeader();
        byte[] payload = idTokenDef.getPayload();
        byte[] signature = idTokenDef.getSignature();
        String clientSecret = context.getClientSecret();
        byte[] signToCompare = signData(header, payload, clientSecret.getBytes());
        byte[] decodedSignature = Base64.decode(signature, Base64.URL_SAFE);

        if (!new String(signToCompare).equals(new String(decodedSignature))) {
            throw new JWTInvalidSignatureException("Token Validation Failed: The signatures do not match");
        }

        return true;
    }

    private  byte[] signData(byte[] header, byte[] payload, byte[] secret) throws JWTValidationException {
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

    private byte[] createSecuredInput(byte[] header, byte[] payload) {

        byte separator[] = ".".getBytes();
        byte jwsSecuredInput[] = new byte[header.length + separator.length + payload.length];

        System.arraycopy(header, 0, jwsSecuredInput, 0, header.length);
        System.arraycopy(separator, 0, jwsSecuredInput, header.length, separator.length);
        System.arraycopy(payload, 0, jwsSecuredInput, header.length + separator.length, payload.length);

        return jwsSecuredInput;
    }

}
