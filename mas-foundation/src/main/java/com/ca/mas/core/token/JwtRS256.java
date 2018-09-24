/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import android.util.Log;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.foundation.MAS;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;


class JwtRS256 {

    private static final String TAG = JwtRS256.class.getSimpleName();

    private JwtRS256() {
        throw new IllegalAccessError("Utility class");
    }

    /*
    * https://connect2id.com/products/nimbus-jose-jwt/examples/validating-jwt-access-tokens for details
    */

    static boolean validateRS256Signature(String idToken) throws JWTInvalidSignatureException {

        ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
        File file = null;
        if (ConfigurationManager.getInstance().getJwks() != null) {
            file = createFileFromString(ConfigurationManager.getInstance().getJwks());
        } else {
           //TODO  MAS.loadJWKS();
        }

        JWKSet jwkSet = null;
        try {
            jwkSet = JWKSet.load(file);
        } catch (IOException | ParseException e) {
            throw new JWTInvalidSignatureException(e.getLocalizedMessage());
        }

        JWKSource keySource = new ImmutableJWKSet(jwkSet);

        JWSAlgorithm expectedJWSAlg = JWSAlgorithm.RS256;

        JWSKeySelector keySelector = new JWSVerificationKeySelector(expectedJWSAlg, keySource);
        jwtProcessor.setJWSKeySelector(keySelector);

        SecurityContext ctx = null; // optional context parameter, not required here
        JWTClaimsSet claimsSet = null;

        try {
            claimsSet = jwtProcessor.process(idToken, ctx);
        } catch (ParseException | BadJOSEException | JOSEException e) {
            throw new JWTInvalidSignatureException(e.getLocalizedMessage());
        }
        Log.d(TAG, claimsSet.toString());
        return true;
    }

    private static File createFileFromString(String s) {

        try {

            File directory = MAS.getContext().getFilesDir();
            File outFile = new File(directory, "keys.json");
            FileOutputStream out = new FileOutputStream(outFile);
            out.write(s.getBytes());
            return outFile;
        } catch (IOException e) {
            Log.d("TAG", e.getMessage());
        }

        return null;
    }


}
