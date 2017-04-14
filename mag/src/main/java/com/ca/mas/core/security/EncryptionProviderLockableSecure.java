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

import com.ca.mas.core.util.KeyUtilsAsymmetric;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;
import static com.ca.mas.core.util.KeyUtilsAsymmetric.generateRsaPrivateKey;


public class EncryptionProviderLockableSecure extends EncryptionProviderLockable {

    protected Context ctx = null;
    protected String keyAlias = "lockable";
    protected static final int KEY_SIZE = 2048;

    /**
     * Save the secret key so we don't have to get it out of the AndroidKeyStore.
     *   This way, we can generate the key in memory and use it without authentication,
     *   but then require authentication when using for unlock.
     */
    protected ConcurrentHashMap<String, SecretKey> secretKeys = new ConcurrentHashMap<String, SecretKey>();


    public EncryptionProviderLockableSecure(@NonNull Context ctx) {
        this.ctx = ctx;
    }

    public EncryptionProviderLockableSecure(@NonNull Context ctx, @NonNull String alias) {
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
        try {
            // retrieve the key if it exists
            PublicKey pubkey = KeyUtilsAsymmetric.getRsaPublicKey(keyAlias);
            if (pubkey == null) {
                PrivateKey privkey = generateRsaPrivateKey(
                        ctx, KEY_SIZE, keyAlias, "cn=" + keyAlias,
                        //true, true, 4, false);
                        true, true, 4, false);
                pubkey = KeyUtilsAsymmetric.getRsaPublicKey(keyAlias);
            }
            byte bytesEncrypted[] = KeyUtilsAsymmetric.encrypt(pubkey, KEY_SIZE, data);
            return bytesEncrypted;
        } catch (Exception x) {
            if (DEBUG) Log.e(TAG, "Unable to encrypt given data " + data);
            throw new RuntimeException(x);
        }
    }

    /**
     * Decrypt the given data.
     *
     * @param encryptedData data to be decrypted
     * @return byte[] of decrypted data
     */
    public byte[] decrypt(byte[] encryptedData) {
        try {
            // retrieve the key if it exists
            PrivateKey privkey = KeyUtilsAsymmetric.getRsaPrivateKey(keyAlias);
            byte bytesDecrypted[] = KeyUtilsAsymmetric.decrypt(privkey, KEY_SIZE, encryptedData);
            return bytesDecrypted;

        } catch (Exception x) {
            if (DEBUG) Log.e(TAG, "Unable to decrypt given data " + encryptedData);
            throw new RuntimeException(x);
        }
    }


    /**
     * This does not clear the key, since it is protected
     *
     * @return true if removed
     */
    public boolean clear()
    {
        return true;
    }



    /**
     * Lock the key.
     *   This is not inherited from EncryptionProvider.
     */
    public void lock() {
    }

}
