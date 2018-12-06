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

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.foundation.MAS;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public class JWTValidation {

    private static final String TAG = JWTValidation.class.getName();
    public static final String ALG = "alg";
    public static final String EXP = "exp";
    public static final String AUD = "aud";
    public static final String AZP = "azp";


    public static boolean isIdTokenExpired(IdToken idToken) {
        if (idToken.getType().equals(IdToken.JWT_DEFAULT)) {
            IdTokenDef tokenDef = new IdTokenDef(idToken);
            try {
                JSONObject jsonObject = tokenDef.getPayloadAsJSONObject();
                String expireDateString = jsonObject.getString(EXP);
                if (Long.valueOf(expireDateString) < new Date().getTime() / 1000) {
                    return true;
                }
                return false;
            } catch (JSONException e) {
                //Assume the token is expired.
                return true;
            }
        }
        return false;
    }

    public static boolean validateIdToken(MssoContext context,IdToken idToken, String deviceIdentifier, String clientId, String clientSecret) throws JWTValidationException {

        boolean isValid = false;

        IdTokenDef idTokenDef = new IdTokenDef(idToken);

        boolean payloadValid = validateJwtPayload(idTokenDef, deviceIdentifier, clientId);

        String algorithm = getAlgorithm(new String(Base64.decode(idTokenDef.getHeader(), Base64.URL_SAFE)));

        boolean signatureValid = false;

        // - if validation is enabled check the algorithms encryption
        boolean idTokenValidationEnabled = MAS.isIdTokenValidationEnabled();

        if (algorithm != null && idTokenValidationEnabled) {
            signatureValid = JWTValidatorFactory.getValidator(algorithm).validate(context,idToken);
        }

        if (!idTokenValidationEnabled){
            signatureValid = true;
        }

        isValid = payloadValid & signatureValid;

        return isValid;
    }


    private static String getAlgorithm(String header) throws JWTValidationException {
        try {
            JSONObject jsonObject = new JSONObject(header);
            return jsonObject.getString(ALG);
        } catch (JSONException e) {
            Log.w(TAG, "JWT header is not JSON Object");
            throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, e.getMessage(), e);
        }
    }





    private static boolean validateJwtPayload(IdTokenDef idTokenDef, String deviceIdentifier, String clientId) throws JWTValidationException {

        try {
            JSONObject jsonObject = idTokenDef.getPayloadAsJSONObject();
            String expireDateString = jsonObject.getString(EXP);
            String audString = jsonObject.getString(AUD);
            String azpString = jsonObject.getString(AZP);

            if (!audString.equals(clientId)) {
                Log.w(TAG, "JWT aud is invalid");
                throw new JWTInvalidAUDException("Failed to validate JWT Token: \"aud\" doesn't match client_id!");
            }

            if (!azpString.equals(deviceIdentifier)) {
                Log.w(TAG, "JWT azp is invalid");
                throw new JWTInvalidAZPException("Failed to validate JWT Token: \"azp\" doesn't match device identifier!");
            }

            if (Long.valueOf(expireDateString) < new Date().getTime() / 1000) {
                Log.w(TAG, "JWT expired");
                throw new JWTExpiredException("Failed to validate JWT Token: token expired!");
            }

        } catch (JSONException e) {
            Log.w(TAG, "JWT payload is not valid JSON object");
            throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, e.getMessage(), e);
        }

        return true;
    }
}
