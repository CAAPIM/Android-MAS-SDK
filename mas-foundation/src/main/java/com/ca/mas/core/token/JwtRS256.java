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
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;


public class JwtRS256 {

    private static final String TAG = JwtRS256.class.getSimpleName();
    private static final String WELL_KNOW_URI = "/.well-known/openid-configuration";
    private static final String JWKS_URI = "jwks_uri";

    private JwtRS256() {
        throw new IllegalAccessError("Utility class");
    }

    /*
     * Validates the signature of JWT which is signed using RS256. The JWKeys are loaded when the SDK is started.
     */

    static boolean validateRS256Signature(String idToken, String kid) throws JWTInvalidSignatureException {


        boolean isSignatureValid = false;
        try {
            JWKSet jwkSet = JWKSet.parse(ConfigurationManager.getInstance().getJwks());
            JWK publicKey = jwkSet.getKeyByKeyId(kid);
            JWSVerifier verifier = new RSASSAVerifier((RSAKey) publicKey);
            SignedJWT signedJWT = SignedJWT.parse(idToken);

            isSignatureValid = signedJWT.verify(verifier);
        } catch (ParseException | JOSEException e) {
            throw new JWTInvalidSignatureException(e.getMessage());
        }

        return isSignatureValid;

    }


    public static void loadJWKS() {
        try {
            MASRequest request_well_know_uri = new MASRequest.MASRequestBuilder(new URL(MASConfiguration.getCurrentConfiguration().getGatewayUrl() +
                    WELL_KNOW_URI)).setPublic().build();

            MAS.invoke(request_well_know_uri, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    JSONObject responseObject = result.getBody().getContent();
                    try {
                        String jwksUri = responseObject.getString(JWKS_URI);
                        MASRequest request_jks_uri = new MASRequest.MASRequestBuilder(new URL(jwksUri
                        )).setPublic().build();
                        MAS.invoke(request_jks_uri, new MASCallback<MASResponse<JSONObject>>() {
                            @Override
                            public void onSuccess(MASResponse<JSONObject> result) {
                                ConfigurationManager.getInstance().setJwks(result.getBody().getContent().toString());
                                Log.d(TAG, "JWT Key Set = " + result.getBody().getContent().toString());
                            }

                            @Override
                            public void onError(Throwable e) {

                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onError(Throwable e) {
                    Log.e(TAG, "Error", e);
                }
            });
        } catch (MalformedURLException e) {
            Log.e(TAG, "Incorrect URL", e);
        }
    }
}
