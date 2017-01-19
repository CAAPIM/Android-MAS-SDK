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
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

class DefaultKeyGenerator implements KeyGenerator {

    private int mKeyLength;
    private String mAlgorithm;
    private static final int DEFAULT_KEY_LENGTH = 256;
    private static final String DEFAULT_ALGORITHM = "AES";

    public DefaultKeyGenerator(String algorithm, int keyLength) {
        if (keyLength < 0) {
            if (DEBUG) Log.d(TAG, "key length is less than zero, assigning default");
            mKeyLength = DEFAULT_KEY_LENGTH;
        } else
            mKeyLength = keyLength;

        if (algorithm != null && algorithm.trim().length() == 0) {
            if (DEBUG) Log.d(TAG, "Algorithm is either null or zero length, assigning default");
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


    @TargetApi(Build.VERSION_CODES.M)
    public SecretKey createSymmetricKeyAndroidM(String SYMMETRIC_KEY_ALIAS)
               throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        keyGenerator.init(
                    new KeyGenParameterSpec.Builder(SYMMETRIC_KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setKeySize(256)
                            .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_GCM)
                            .setDigests(DIGEST_NONE, DIGEST_MD5, DIGEST_SHA1, DIGEST_SHA256, DIGEST_SHA384, DIGEST_SHA512)
                            .setEncryptionPaddings(ENCRYPTION_PADDING_NONE, ENCRYPTION_PADDING_PKCS7, ENCRYPTION_PADDING_RSA_OAEP, ENCRYPTION_PADDING_RSA_PKCS1)
                            .setRandomizedEncryptionRequired(false)
                            .setUserAuthenticationRequired(false)
                            .build());
        SecretKey key = keyGenerator.generateKey();
        return key;
    }

}
