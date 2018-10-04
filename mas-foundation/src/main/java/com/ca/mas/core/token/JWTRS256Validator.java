/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import android.content.Context;
import android.content.SharedPreferences;
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
import com.ca.mas.foundation.notify.Callback;
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
import java.util.concurrent.TimeUnit;

import static com.ca.mas.foundation.MAS.DEBUG;


public class JWTRS256Validator implements JWTValidator {

    public static final String TAG = JWTRS256Validator.class.getSimpleName();
    private static final String WELL_KNOW_URI = "/.well-known/openid-configuration";
    private static final String JWKS_URI = "jwks_uri";
    public static final String KID = "kid"; //Key ID
    public static final String JWT_KEY_SET_FILE = "jwks_store";

    private static String jwks;

    /*
     * Validates the signature of JWT which is signed using RS256. The JWKeys are loaded when the SDK is started.
     */
    @Override
    public boolean validate(final IdToken idToken) throws JWTValidationException {

        boolean isSignatureValid = false;

        MASCallbackFuture<String> masCallbackFuture = new MASCallbackFuture();
        IdTokenDef idTokenDef = new IdTokenDef(idToken);
        String kid = null;
        try {
            kid = getKid(new String(Base64.decode(idTokenDef.getHeader(), Base64.URL_SAFE)));
        } catch (JWTValidationException e) {
            throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, e);
        }
        try {
            JWK publicKey = getJwk(kid);
            if (publicKey == null) {
                jwks = null;
                resetPrefs();
                publicKey = getJwk(kid);
            }
            JWSVerifier verifier = new RSASSAVerifier((RSAKey) publicKey);
            SignedJWT signedJWT = SignedJWT.parse(idToken.getValue());
            isSignatureValid = signedJWT.verify(verifier);
        } catch (InterruptedException | ExecutionException | ParseException | JOSEException e) {
            throw new JWTValidationException(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_SIGNATURE, e);
        }
        return isSignatureValid;
    }


    private JWK getJwk(String kid) throws InterruptedException, ExecutionException, ParseException{
        MASCallbackFuture<String> masCallbackFuture = new MASCallbackFuture<>();
        loadJWKS(masCallbackFuture);
        String result = masCallbackFuture.get(3000, TimeUnit.MILLISECONDS);
        JWKSet jwkSet = JWKSet.parse(result);
        return jwkSet.getKeyByKeyId(kid);
    }


    /**
     * Loads JSON Web Key Set (JWKS) using well-know url whose response gives jwks-uri.
     *
     * @param callback a callback object of class implementing Future.  Required.
     */
    public static void loadJWKS(final MASCallbackFuture<String> callback)  {
        try {

            if(jwks != null){
                Callback.onSuccess(callback, jwks);
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
                                jwks = result.getBody().getContent().toString();
                                writeJwtKeySetToPrefs(jwks);
                                if(DEBUG)
                                    Log.d(TAG, "JWT Key Set = " + result.getBody().getContent().toString());
                                Callback.onSuccess(callback, result.getBody().getContent().toString());

                            }

                            @Override
                            public void onError(Throwable e) {

                                Callback.onError(callback, e);
                            }
                        });
                    } catch (JSONException | MalformedURLException e) {
                        Callback.onError(callback, e);
                    }
                }
                @Override
                public void onError(Throwable e) {
                   Callback.onError(callback, e);
                }
            });
        } catch (MalformedURLException e) {
            Callback.onError(callback, e);
        }
    }

    private static void writeJwtKeySetToPrefs(String jwks) {
        SharedPreferences prefs = MAS.getContext().getSharedPreferences(JWT_KEY_SET_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ConfigurationManager.getInstance().getConnectedGateway().getHost(), jwks);
        editor.commit();
    }

    private void resetPrefs() {
        SharedPreferences prefs = MAS.getContext().getSharedPreferences(JWT_KEY_SET_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(ConfigurationManager.getInstance().getConnectedGateway().getHost(), null);
        editor.commit();
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

    public static String getJwks() {
        return jwks;
    }

    public static void setJwks(String jwks) {
        JWTRS256Validator.jwks = jwks;
    }
}


