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
import androidx.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.util.KeyUtilsSymmetric;

import javax.crypto.SecretKey;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;


/**
 * This class supports key storage.  It will require a screen lock,
 * but the screen lock can change.  For Android pre-N, this is
 * managed entirely by checking KeyguardManager.isDeviceSecure()
 * to determine if there is still a screen lock.  If the screen
 * lock, pin/swipe/password, is removed entirely, the keys are
 * deleted.  For Android.N, the keys are also protected inside
 * the AndroidKeyStore requiring a screen lock.
 */
public class ScreenLockEncryptionProvider implements EncryptionProvider {

    protected Context ctx = null;
    private String keyAlias = "com.ca.mas.SCREEN_LOCK_SECRET";
    // only used for pre-M
    private KeyStoreKeyStorageProvider keyStorageProvider = null;

    /**
     * Constructor
     */
    public ScreenLockEncryptionProvider(@NonNull Context ctx) {
        this.ctx = ctx;
        //The Secret key will be encrypted and stored on SharedPreferences for Pre-M
        keyStorageProvider = new SharedPreferencesKeyStorageProvider(ctx);
    }

    /**
     * Constructor for Android Pre-M, allows for setting
     * KeyStoreKeyStorageProvider
     */
    public ScreenLockEncryptionProvider(@NonNull Context ctx, KeyStoreKeyStorageProvider keyStorageProvider) {
        this.ctx = ctx;
        this.keyStorageProvider = keyStorageProvider;
    }


    /**
     * Encrypts the given data.
     *
     * @param data the data to encrypt
     * @return encrypted data as byte[]
     */
    public byte[] encrypt(byte[] data) {
        SecretKey secretKey = getKey(keyAlias);
        return KeyUtilsSymmetric.encrypt(data, secretKey, keyAlias);
    }

    /**
     * Decrypt the given data.
     *
     * @param encryptedData data to be decrypted
     * @return byte[] of decrypted data
     */
    public byte[] decrypt(byte[] encryptedData) {
        // retrieve the key
        SecretKey secretKey = getKey(keyAlias);
        return KeyUtilsSymmetric.decrypt(encryptedData, secretKey, keyAlias);
    }


    /**
     * Retrieve the SecretKey from Storage
     *
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    protected SecretKey getKey(String alias) {

        // if there is no screen lock, then delete the key and return nothing!!!
        if (!deviceHasScreenLock()) {
            Log.w(TAG, "ScreenLockEncryptionProvider getKey there is no screen lock (pin/swipe/password), so the key will be deleted");
            try {
                KeyUtilsSymmetric.deleteKey(alias);
            } catch (Exception x) {
                // continue to delete in storage even if error
            }
            try {
                keyStorageProvider.removeKey(alias);
            } catch (Exception x) { 
                // continue to runtime exception even if error
            }
            if (DEBUG) Log.d(TAG, "ScreenLockEncryptionProvider getKey there is no screen lock (pin/swipe/password), so the encryption key has been deleted");
            throw new RuntimeException("ScreenLockEncryptionProvider getKey there is no screen lock (pin/swipe/password), so the encryption key has been deleted");
        }

        // otherwise, we can get or create one

        return keyStorageProvider.getKey(keyAlias, true);
    }


    /**
     * Determine if there is a screen lock
     *
     * @return true if screen lock present
     */
    protected boolean deviceHasScreenLock() {
        try {
            KeyguardManager km = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (km.isDeviceSecure())
                    return true;
                else
                    return false;
            } else {
                if (km.isKeyguardSecure())
                    return true;
                else
                    return false;
            }
        } catch (Exception x) {
            Log.e(TAG, "Exception determining if screen has a lock (pin/swipe/password), will be assuming it does not", x);
            return false;
        }
    }
}
