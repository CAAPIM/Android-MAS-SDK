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
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.MASCallbackFuture;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASResponseBody;
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

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.util.concurrent.ExecutionException;

import static com.ca.mas.foundation.MAS.DEBUG;


/**
 * This class loads JWKS if not already loaded and stores in SharedPreferences.
 * Validates signature of the JWT Id Token which is signed by RS256
 */
public class JWTRS256Validator implements JWTValidator {

    public static final String TAG = JWTRS256Validator.class.getSimpleName();
    private static final String WELL_KNOW_URI = "/.well-known/openid-configuration";
    private static final String JWKS_URI = "jwks_uri";
    private static final String KID = "kid"; //Key ID
    public static final String JWT_KEY_SET_FILE = "jwks_store";

    private static String jwks;


    public JWTRS256Validator() {

        if (jwks == null) {
            SharedPreferences prefs = MAS.getContext().getSharedPreferences(JWTRS256Validator.JWT_KEY_SET_FILE, Context.MODE_PRIVATE);
            String keySet = prefs.getString(ConfigurationManager.getInstance().getConnectedGateway().getHost(), null);
            jwks = keySet;
        }

    }

    private static void resetPrefs() {
        final SharedPreferences prefs = MAS.getContext().getSharedPreferences(JWT_KEY_SET_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }

    /*
     * Validates the signature of JWT which is signed using RS256 algorithm.
     *
     * @param idToken The Id Token whose signature is to be validated.
     *
     * @return boolean True if signature is valid else false.
     */
    @Override
    public boolean validate(final @NonNull IdToken idToken) throws JWTValidationException {

        boolean isSignatureValid;
        IdTokenDef idTokenDef = new IdTokenDef(idToken);
        String kid;
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
            if(publicKey == null)
                throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN);

            JWSVerifier verifier = new RSASSAVerifier((RSAKey) publicKey);
            SignedJWT signedJWT = SignedJWT.parse(idToken.getValue());
            isSignatureValid = signedJWT.verify(verifier);
        } catch (InterruptedException | ExecutionException | ParseException | JOSEException e) {
            throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, e);
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
     * @param callback a callback object to return result.  Required.
     */
    public void loadJWKS(final @NonNull MASCallbackFuture<String> callback) {

        if (jwks != null) {
            Callback.onSuccess(callback, jwks);
            return;
        }
        new JwksLoadAsynTask().execute(callback);

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

    static class JwksLoadAsynTask extends AsyncTask<MASCallback<String>, Void, Void> {

        private static void writeJwtKeySetToPrefs(String jwks) {
            SharedPreferences prefs = MAS.getContext().getSharedPreferences(JWT_KEY_SET_FILE, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(ConfigurationManager.getInstance().getConnectedGateway().getHost(), jwks);
            editor.apply();
        }

        @Override
        protected Void doInBackground(MASCallback<String>... params) {
            final MASCallback<String> callback = params[0];

            try {
                URL request_well_know_uri = new URL(MASConfiguration.getCurrentConfiguration().getGatewayUrl() +
                        WELL_KNOW_URI);

                MAGHttpClient client = new MAGHttpClient();
                MASRequest request = new MASRequest.MASRequestBuilder(request_well_know_uri).
                        responseBody(MASResponseBody.jsonBody()).setPublic().build();

                MASResponse<JSONObject> response = client.execute(request);

                JSONObject jsonObject = response.getBody().getContent();
                String jwksUri = jsonObject.getString(JWKS_URI);
                MASRequest request_jks_uri = new MASRequest.MASRequestBuilder(new URL(jwksUri
                )).setPublic().build();

                response = client.execute(request_jks_uri);
                jwks =response.getBody().getContent().toString();
                writeJwtKeySetToPrefs(jwks);
                if (DEBUG)
                    Log.d(TAG, "JWT Key Set = " + jwks);
                Callback.onSuccess(callback, jwks);
            } catch (IOException e) {
                Callback.onError(callback, e);
            } catch (JSONException e) {
                Callback.onError(callback, e);
            }
            return null;
        }
    }

    public static String getJwks() {
        return JWTRS256Validator.jwks;
    }

    public static void setJwks(String jwks) {
        JWTRS256Validator.jwks = jwks;
    }
}


