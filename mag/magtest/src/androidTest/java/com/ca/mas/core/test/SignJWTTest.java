/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.test;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.Server;
import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.core.util.KeyUtils;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.util.DateUtils;

import net.minidev.json.reader.JsonWriterI;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SignJWTTest {
    @Test
    public void testSignAndVerify() throws Exception {
        //Client
        PrivateKey privateKey = KeyUtils.generateRsaPrivateKey(InstrumentationRegistry.getInstrumentation().getTargetContext(), 2048, "TEST", "dn=test", false, false, -1, false);
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256),
                new Payload("Hello, world!"));
        jwsObject.sign(new RSASSASigner(privateKey));
        String compactJws = jwsObject.serialize();

        //Server
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) KeyUtils.getRsaPublicKey("TEST"));

        assertTrue(jwsObject.verify(verifier));

        assertEquals("Hello, world!", jwsObject.getPayload().toString());

        KeyUtils.deletePrivateKey("TEST");
    }

    @Test
    public void name() throws Exception {

        JWTClaimsSet.Builder claimBuilder = new JWTClaimsSet.Builder();
        claimBuilder.claim("content", true);
        claimBuilder.claim("content-type", ContentType.APPLICATION_JSON.getMimeType());

        String s = claimBuilder.build().toJSONObject().toJSONString();
        System.out.print(s);

    }

    private static class Test2 {
        private String name;
        private String value;

        public Test2(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }

    @Test
    public void endToEnd() throws Exception {

        //===================== Enroll ======================================
        //The client enroll the public key with JWK
        PrivateKey privateKey = KeyUtils.generateRsaPrivateKey(InstrumentationRegistry.getInstrumentation().getTargetContext(), 2048, "TEST", "dn=test", false, false, -1, false);
        PublicKey publicKey = KeyUtils.getRsaPublicKey("TEST");

        JWK jwk = new RSAKey.Builder((RSAPublicKey) publicKey)
                .keyID(UUID.randomUUID().toString()) // Give the key some ID (optional)
                .build();

        //Send the JWK to server
        String s = jwk.toJSONString();
        System.out.println(s);

        //======================= Access API ======================================

        //Client
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.RS256),
                new Payload("Hello, world!"));
        jwsObject.sign(new RSASSASigner(privateKey));

        //Server
        JWK result = JWK.parse(jwk.toJSONObject());
        PublicKey pk = ((RSAKey) result).toRSAPublicKey();

        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) pk);
        assertTrue(jwsObject.verify(verifier));
        assertEquals("Hello, world!", jwsObject.getPayload().toString());

        //Clean up for the test
        KeyUtils.deletePrivateKey("TEST");
    }
}
