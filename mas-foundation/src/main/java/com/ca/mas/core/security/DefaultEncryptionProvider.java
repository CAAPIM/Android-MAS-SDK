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

import com.ca.mas.core.util.KeyUtilsSymmetric;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class DefaultEncryptionProvider implements EncryptionProvider {
    private KeyStorageProvider ksp;
    private static final String KEY_ALIAS = "secret";

    public DefaultEncryptionProvider(@NonNull Context ctx) {
        //The Secret key will be encrypted and stored on SharedPreferences for Pre-M
        this(ctx, new SharedPreferencesKeyStorageProvider(ctx));
    }

    public DefaultEncryptionProvider(Context ctx, KeyStorageProvider keyStorageProvider) {
        ksp = keyStorageProvider;
    }

    protected String getKeyAlias() {
        return KEY_ALIAS;
    }

    /**
     * Encrypts the given data.
     *
     * @param data : the data to encrypt
     * @return encrypted data as byte[]
     */
    @Override
    public byte[] encrypt(byte[] data) {
        if (data == null) {
            return null;
        }

        byte[] encryptedData;
        try {
            SecretKey secretKey = ksp.getKey(getKeyAlias(), false);
            encryptedData = KeyUtilsSymmetric.encrypt(data, secretKey, getKeyAlias());
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "inside exception of encrypt function: ", e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return encryptedData;
    }

    /**
     * @param encryptedData : data to be decrypted
     * @return byte[] of decrypted data
     */
    @Override
    public byte[] decrypt(byte[] encryptedData) {
        try {
            SecretKey secretKey = ksp.getKey(getKeyAlias(), false);
            return KeyUtilsSymmetric.decrypt(encryptedData, secretKey, getKeyAlias());
        } catch (Exception e) {
            if (DEBUG) Log.i(TAG, "Error while decrypting an cipher instance", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }


    /**
     * Destroys the ephemeral key, in this case the Secret key generated for MAC, if the key implements Destroyable
     *
     * @param key
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void destroyKey(SecretKey key) {
        if (key instanceof Destroyable) {
            Destroyable destroyable = (Destroyable) key;
            try {
                destroyable.destroy();
            } catch (DestroyFailedException e) {
                if (DEBUG) Log.e(TAG, "Could not destroy key");
            }
        }
    }
}
