package com.ca.mas;

import com.ca.mas.core.io.IoUtils;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.GregorianCalendar;

public class TestUtils {

    private static final int DEFAULT_MAX = 10485760;

    public static JSONObject getJSONObject(String path) throws IOException, JSONException {
        //Support window machine file path.
        path = path.replace(":", ".");
        if (!path.endsWith(".json")) {
            path = path + ".json";
        }
        return new JSONObject(getString(path));
    }

    public static String getString(String path) throws IOException {
        byte[] bytes = IoUtils.slurpStream(TestUtils.class.getResourceAsStream(path), DEFAULT_MAX);
        return new String(bytes);
    }

    public static byte[] getBytes(String path) throws IOException {
        return  IoUtils.slurpStream(TestUtils.class.getResourceAsStream(path), DEFAULT_MAX);

    }
    public static String jwtGenerator() throws JOSEException {

        ////////Code snippet reference at https://connect2id.com/products/nimbus-jose-jwt/examples/jwt-with-rsa-signature /////////////////
        RSAKey rsaJWK = new RSAKeyGenerator(2048)
                .keyID("default_ssl_key")
                .generate();

        // Create RSA-signer with the private key
        JWSSigner signer = new RSASSASigner(rsaJWK);

        // Prepare JWT with claims set
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("1234567890")
                .issuer("https://c2id.com")
                .expirationTime(new GregorianCalendar(2020, 12, 30).getTime())
                .audience("dummy")
                .claim("azp", "f473525d-c130-4bba-86cc-db26d8875386")
                .build();

        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(rsaJWK.getKeyID()).type(JOSEObjectType.JWT).build(),
                claimsSet);

        // Compute the RSA signature
        signedJWT.sign(signer);

        return signedJWT.serialize();

    }

}
