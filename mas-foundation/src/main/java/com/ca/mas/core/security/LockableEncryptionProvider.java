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

import static com.ca.mas.core.util.KeyUtilsAsymmetric.generateRsaPrivateKey;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;


public class LockableEncryptionProvider implements EncryptionProvider {

    protected Context ctx = null;
    private String keyAlias = "com.ca.mas.LOCKABLE_KEY";
    private static final int KEY_SIZE = 2048;

    public LockableEncryptionProvider(@NonNull Context ctx) {
        this.ctx = ctx;
    }

    public LockableEncryptionProvider(@NonNull Context ctx, @NonNull String keyAlias) {
        this.ctx = ctx;
        this.keyAlias = keyAlias;
    }

    /**
     * Encrypts the given data.
     *
     * @param data the data to encrypt
     * @return encrypted data as byte[]
     */
    public byte[] encrypt(byte[] data) {
        try {
            // retrieve the key if it exists
            PublicKey pubkey = KeyUtilsAsymmetric.getRsaPublicKey(keyAlias);
            if (pubkey == null) {
                //Do not remove, this will generate private/public key in keychain
                generateRsaPrivateKey(
                        ctx, KEY_SIZE, keyAlias, "cn=" + keyAlias,
                        true, true, 4, false);
                pubkey = KeyUtilsAsymmetric.getRsaPublicKey(keyAlias);
            }
            return KeyUtilsAsymmetric.encrypt(pubkey, KEY_SIZE, data);
        } catch (Exception x) {
            if (DEBUG) Log.e(TAG, "Unable to encrypt given data");
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
            return KeyUtilsAsymmetric.decrypt(privkey, KEY_SIZE, encryptedData);

        } catch (Exception x) {
            if (DEBUG) Log.e(TAG, "Unable to decrypt given data.");
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
        KeyUtilsAsymmetric.deletePrivateKey(keyAlias);
        return true;
    }




}
