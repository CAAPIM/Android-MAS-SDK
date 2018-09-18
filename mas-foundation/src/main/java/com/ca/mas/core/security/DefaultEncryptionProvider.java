/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.security;

import android.app.KeyguardManager;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.ca.mas.core.util.KeyUtilsSymmetric;
import com.ca.mas.foundation.MAS;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import static android.content.Context.KEYGUARD_SERVICE;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class DefaultEncryptionProvider implements EncryptionProvider {
    private KeyStorageProvider ksp;
    private static final String KEY_ALIAS = "secret";

    KeyguardManager keyguardManager = (KeyguardManager) MAS.getContext().getSystemService(KEYGUARD_SERVICE);

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
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public byte[] encrypt(byte[] data) {
        if (data == null) {
            return null;
        }

        byte[] encryptedData = null;
        try {
            if (keyguardManager.isDeviceSecure()) {
                SecretKey secretKey = ksp.getKey(getKeyAlias(), true);
                encryptedData = KeyUtilsSymmetric.encrypt(data, secretKey, getKeyAlias());
            }  else {
                throw new RuntimeException("Not secured, active PIN");
            }
        } catch (Exception e) {

            if (e instanceof android.security.keystore.UserNotAuthenticatedException) {
                KeyUtilsSymmetric.authenticate();
            } else {
                if (DEBUG) Log.e(TAG, "inside exception of encrypt function: ", e);
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return encryptedData;
    }

    /**
     * @param encryptedData : data to be decrypted
     * @return byte[] of decrypted data
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public byte[] decrypt(byte[] encryptedData) {
        try {
            if (keyguardManager.isDeviceSecure()) {
                SecretKey secretKey = ksp.getKey(getKeyAlias(), true);
                return KeyUtilsSymmetric.decrypt(encryptedData, secretKey, getKeyAlias());
            } else {
                throw new RuntimeException("Not secured, active PIN");
            }
        } catch (Exception e) {

            if (e instanceof android.security.keystore.UserNotAuthenticatedException) {
                KeyUtilsSymmetric.authenticate();
            } else {
                if (DEBUG) Log.i(TAG, "Error while decrypting an cipher instance", e);
                throw new RuntimeException(e.getMessage(), e);
            }
        }

        return null;
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
