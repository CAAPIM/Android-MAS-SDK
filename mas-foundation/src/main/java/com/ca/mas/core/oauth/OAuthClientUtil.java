/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */


package com.ca.mas.core.oauth;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import static com.ca.mas.foundation.MAS.DEBUG;

public class OAuthClientUtil {

    private OAuthClientUtil() {
    }

    public static PKCE generateCodeChallenge() {
        int encodeFlags = Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE;
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        String codeVerifier = Base64.encodeToString(randomBytes, encodeFlags);
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(codeVerifier.getBytes("ISO_8859_1"));
            byte[] digestBytes = messageDigest.digest();
            return new PKCE("S256", Base64.encodeToString(digestBytes, encodeFlags), codeVerifier);
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) Log.w("SHA-256 not supported", e);
            return new PKCE("plain", codeVerifier, codeVerifier);
        } catch (UnsupportedEncodingException e) {
            if (DEBUG) Log.e("PKCE not supported", e.getMessage(), e);
            return null;
        }
    }


}
