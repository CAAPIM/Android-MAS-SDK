/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.util.KeyUtilsSymmetric;

import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;


/**
 * If this is used for locking a session, be sure to lock when 
 *    removing the unencrypted token and recycle the existing token
 */
public class EncryptionProviderLockableSymmetric implements EncryptionProvider {
    protected Context ctx = null;
    protected String keyAlias = "secret";

    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;

    /**
     * Save the secret key so we don't have to get it out of the AndroidKeyStore.
     *   This way, we can generate the key in memory and use it without authentication,
     *   but then require authentication when using for unlock.
     */
    protected ConcurrentHashMap<String, SecretKey> secretKeys = new ConcurrentHashMap<String, SecretKey>();


    public EncryptionProviderLockableSymmetric(@NonNull Context ctx) {
        this.ctx = ctx;
    }

    public EncryptionProviderLockableSymmetric(@NonNull Context ctx, @NonNull String alias) {
        this.ctx = ctx;
        this.keyAlias = alias;
    }


    /**
     * Encrypts the given data.
     *
     * @param data  the data to encrypt
     * @return encrypted data as byte[]
     */
    public byte[] encrypt(byte[] data) {

        // retrieve the key: local copy if possible
        SecretKey secretKey = secretKeys.get(keyAlias);
        if (secretKey == null) {
            secretKey = KeyUtilsSymmetric.retrieveKey(keyAlias);
            if (secretKey == null) {
                secretKey = KeyUtilsSymmetric.generateKey(keyAlias, "AES", 256,
                                                 true, true, 4, false);
                secretKeys.put(keyAlias, secretKey);
            }
        }

        return KeyUtilsSymmetric.encrypt(data, secretKey, keyAlias);
    }

    /**
     * Decrypt the given data.
     *
     * @param encryptedData data to be decrypted
     * @return byte[] of decrypted data
     */
    public byte[] decrypt(byte[] encryptedData) {

        // retrieve the key: local copy if possible
        SecretKey secretKey = secretKeys.get(keyAlias);
        if (secretKey == null) {
            secretKey = KeyUtilsSymmetric.retrieveKey(keyAlias);
            if (secretKey == null) {
                secretKey = KeyUtilsSymmetric.generateKey(keyAlias, "AES", 256,
                                                 true, true, 10, false);
                secretKeys.put(keyAlias, secretKey);
            }
        }

        return KeyUtilsSymmetric.decrypt(encryptedData, secretKey, keyAlias);
    }


    /**
     * Clear the lock completely, including removing the key.
     *   This is not inherited from EncryptionProvider.
     *
     * @return true if removed
     */
    public boolean clear()
    {
        secretKeys.remove(keyAlias);
        KeyUtilsSymmetric.deleteKey(keyAlias);
        return true;
    }



    /**
     * Lock the key.
     *   This is not inherited from EncryptionProvider.
     */
    public void lock() {
        SecretKey secretKey = secretKeys.remove(keyAlias);
        if (secretKey != null) {
            if (secretKey instanceof Destroyable) {
                Destroyable destroyable = (Destroyable) secretKey;
                try {
                    destroyable.destroy();
                } catch (DestroyFailedException e) {
                    if (DEBUG) Log.e(TAG, "Could not destroy key");
                }
            }
        }
    }

}
