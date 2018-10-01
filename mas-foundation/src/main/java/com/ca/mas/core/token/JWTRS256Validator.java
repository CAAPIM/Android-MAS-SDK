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

import com.ca.mas.core.MASCallbackFuture;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.error.MAGErrorCode;
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
import java.util.concurrent.ExecutionException;


public class JWTRS256Validator implements JWTValidator {

    public static final String TAG = JWTRS256Validator.class.getSimpleName();
    private static final String WELL_KNOW_URI = "/.well-known/openid-configuration";
    private static final String JWKS_URI = "jwks_uri";
    public static final String KID = "kid"; //Key ID


    /*
     * Validates the signature of JWT which is signed using RS256. The JWKeys are loaded when the SDK is started.
     */
    @Override
    public boolean validate(final IdToken idToken) throws JWTInvalidSignatureException {

        boolean isSignatureValid = false;

        MASCallbackFuture<String> masCallbackFuture = new MASCallbackFuture();
        IdTokenDef idTokenDef = new IdTokenDef(idToken);
        String kid = null;
        try {
            kid = getKid(new String(Base64.decode(idTokenDef.getHeader(), Base64.URL_SAFE)));
        } catch (JWTValidationException e) {
            throw new JWTInvalidSignatureException(e);
        }

        loadJWKS(masCallbackFuture);
        try {
            JWK publicKey = getJwk(kid);
            if (publicKey == null) {
                ConfigurationManager.getInstance().setJwks(null);
                publicKey = getJwk(kid);
            }
            JWSVerifier verifier = new RSASSAVerifier((RSAKey) publicKey);
            SignedJWT signedJWT = SignedJWT.parse(idToken.getValue());
            isSignatureValid = signedJWT.verify(verifier);
        } catch (InterruptedException | ExecutionException | ParseException | JOSEException e) {
            throw new JWTInvalidSignatureException(e);
        }
        return isSignatureValid;
    }

    private JWK getJwk(String kid) throws InterruptedException, ExecutionException, ParseException {
        MASCallbackFuture<String> masCallbackFuture = new MASCallbackFuture<>();
        loadJWKS(masCallbackFuture);
        String result = masCallbackFuture.get();
        JWKSet jwkSet = JWKSet.parse(result);
        return jwkSet.getKeyByKeyId(kid);
    }


    /**
     * Loads JSON Web Key Set (JWKS) using well-know url whose response gives jwks-uri.
     *
     * @param callback a callback object of class implementing Future.  Required.
     */
    public static void loadJWKS(final MASCallbackFuture<String> callback) {
        try {

            if(ConfigurationManager.getInstance().getJwks() != null){
                callback.onSuccess(ConfigurationManager.getInstance().getJwks());
                return;
            }
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
                                if(callback != null)
                                    callback.onSuccess(result.getBody().getContent().toString());
                                Log.d(TAG, "JWT Key Set = " + result.getBody().getContent().toString());

                            }

                            @Override
                            public void onError(Throwable e) {
                                if(callback != null)
                                    callback.onError(e);

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


    private String getKid(String header) throws JWTValidationException {
        try {
            JSONObject jsonObject = new JSONObject(header);
            return jsonObject.getString(KID);
        } catch (JSONException e) {
            Log.w(TAG, "JWT header is not JSON Object");
            throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, e.getMessage(), e);
        }
    }

}


