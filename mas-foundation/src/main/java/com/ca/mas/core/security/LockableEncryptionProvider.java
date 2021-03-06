/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.security;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import android.util.Log;

import com.ca.mas.core.util.KeyUtilsAsymmetric;
import com.ca.mas.core.util.KeyUtilsSymmetric;

import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.SecretKey;

import static com.ca.mas.core.util.KeyUtilsAsymmetric.generateRsaPrivateKey;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class LockableEncryptionProvider implements EncryptionProvider {

    protected Context ctx = null;
    private String keyAlias = "com.ca.mas.LOCKABLE_KEY";

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
    @RequiresApi(Build.VERSION_CODES.M)
    public byte[] encrypt(byte[] data) {
        try {
            //Do not remove, this will generate private/public key in keychain
            generateRsaPrivateKey(
                    keyAlias, "cn=" + keyAlias,
                    true, true, 4, false);
            PublicKey pubkey = KeyUtilsAsymmetric.getRsaPublicKey(keyAlias);
            return KeyUtilsAsymmetric.encrypt(pubkey, data);
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
    @RequiresApi(Build.VERSION_CODES.M)
    public byte[] decrypt(byte[] encryptedData) {
        try {
            //If we find the keystore key is a PrivateKey, we decrypt with the asymmetric key logic.
            //Asymmetric keys are preferred as they're more secure.
            Key keyStoreKey = KeyUtilsAsymmetric.getKeystoreKey(keyAlias);
            if (keyStoreKey instanceof PrivateKey) {
                PrivateKey privkey = KeyUtilsAsymmetric.getRsaPrivateKey(keyAlias);
                return KeyUtilsAsymmetric.decrypt(privkey, encryptedData);
                //However, if we find that the keystore key is a SecretKey and not a PrivateKey,
                //it means it was encrypted in an older SDK version with a symmetric key.
                //We decrypt this with the symmetric key logic.
            } else {
                SecretKey secretKey = (SecretKey) keyStoreKey;
                return KeyUtilsSymmetric.decrypt(encryptedData, secretKey, keyAlias);
            }
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
    public boolean clear() {
        KeyUtilsAsymmetric.deletePrivateKey(keyAlias);
        return true;
    }

}
