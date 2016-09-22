/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.util.Log;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

class DefaultKeyGenerator implements KeyGenerator {

    private int mKeyLength;
    private String mAlgorithm;
    private static final int DEFAULT_KEY_LENGTH = 256;
    private static final String DEFAULT_ALGORITHM = "AES";
    private static final String TAG = DefaultKeyGenerator.class.getCanonicalName();

    public DefaultKeyGenerator(String algorithm, int keyLength) {
        if (keyLength < 0) {
            Log.i(TAG, "key length is less than zero, assigning default");
            mKeyLength = DEFAULT_KEY_LENGTH;
        } else
            mKeyLength = keyLength;

        if (algorithm != null && algorithm.trim().length() == 0) {
            Log.i(TAG, "Algorithm is either null or zero length, assigning default");
            mAlgorithm = DEFAULT_ALGORITHM;
        } else
            mAlgorithm = algorithm;

    }

    /**
     * This method generates an SecretKey
     * @return SecretKey
     */
    @Override
    public SecretKey generateKey() throws NoSuchAlgorithmException {

        javax.crypto.KeyGenerator kg = javax.crypto.KeyGenerator.getInstance(mAlgorithm);
        kg.init(mKeyLength);
        return kg.generateKey();

    }
}
