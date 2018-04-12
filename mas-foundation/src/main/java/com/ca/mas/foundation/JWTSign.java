/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import net.minidev.json.JSONStyle;
import net.minidev.json.JSONValue;
import net.minidev.json.reader.JsonWriterI;

import org.json.JSONObject;

import java.io.IOException;
import java.security.PrivateKey;
import java.util.Map;

class JWTSign {

    private JWTSign() {}

    /**
     * Signs the provided JWT {@link MASClaims} object with the provided RSA private key using SHA-256 hash algorithm
     * and injects JWT claims based on the user information.
     * This method will use a default value of 5 minutes for the JWS 'exp' claim if not provided.
     *
     * @param masClaims  The JWT Claims
     * @param privateKey The private RSA key.
     * @return The JWT format consisting of Base64URL-encoded parts delimited by period ('.') characters.
     * @throws MASException Failed to sign
     */
    static String sign(MASClaims masClaims, PrivateKey privateKey) throws MASException {

        JsonWriterI i = JSONValue.defaultWriter.getWriterByInterface(JSONObject.class);
        if (i == null) {
            JSONValue.defaultWriter.registerWriter(new JsonWriterI<JSONObject>() {
                public void writeJSONString(JSONObject value, Appendable out, JSONStyle compression) throws IOException {
                    out.append(value.toString());
                }
            }, JSONObject.class);
        }

        JWSSigner signer = new RSASSASigner(privateKey);
        JWTClaimsSet.Builder claimBuilder = new JWTClaimsSet.Builder();

        claimBuilder.jwtID(masClaims.getJwtId())
                .issuer(masClaims.getIssuer())
                .notBeforeTime(masClaims.getNotBefore())
                .expirationTime(masClaims.getExpirationTime())
                .issueTime(masClaims.getIssuedAt())
                .audience(masClaims.getAudience())
                .subject(masClaims.getSubject());

        for (Map.Entry<String, Object> entry : masClaims.getClaims().entrySet()) {
            claimBuilder.claim(entry.getKey(), entry.getValue());
        }

        JWSHeader rs256Header = new JWSHeader(JWSAlgorithm.RS256);
        SignedJWT claimsToken = new SignedJWT(rs256Header, claimBuilder.build());
        try {
            claimsToken.sign(signer);
            return claimsToken.serialize();
        } catch (JOSEException e) {
            throw new MASException(e);
        }
    }


}
