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
import android.util.Log;

import com.ca.mas.core.util.KeyUtilsSymmetric;

import javax.crypto.SecretKey;

import static com.ca.mas.core.MAG.TAG;


/**
 * This class supports key storage.  It will require a screen lock,
 *   but the screen lock can change.  For Android pre-N, this is
 *   managed entirely by checking KeyguardManager.isDeviceSecure()
 *   to determine if there is still a screen lock.  If the screen
 *   lock, pin/swipe/password, is removed entirely, the keys are
 *   deleted.  For Android.N, the keys are also protected inside
 *   the AndroidKeyStore requiring a screen lock.
 */
public class EncryptionProviderScreenLockCanChange implements EncryptionProvider {

    protected Context ctx = null;
    protected String keyAlias = "SCREEN_LOCK";

    /**
     * Constructor
     */
    public EncryptionProviderScreenLockCanChange(@NonNull Context ctx) {
        this.ctx = ctx;
    }


    /**
     * Encrypts the given data.
     *
     * @param data  the data to encrypt
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
            Log.w(TAG, "EncryptionProviderScreenLockCanChange getKey there is no screen lock (pin/swipe/password), so the key will be deleted");
            KeyUtilsSymmetric.deleteKey(alias);
            throw new RuntimeException("EncryptionProviderScreenLockCanChange getKey there is no screen lock (pin/swipe/password), so the encryption key has been deleted");
        }

        // otherwise, we can get or create one
        SecretKey secretKey = KeyUtilsSymmetric.retrieveKey(alias);
        if (secretKey == null) {
            secretKey = KeyUtilsSymmetric.generateKey(alias, "AES", 256,
                                     false, true, 100000, false);
        }

        return secretKey;
    }


    /**
     * Determine if there is a screen lock
     *
     * @return true if screen lock present
     */
    private boolean deviceHasScreenLock()
    {
        try {
            KeyguardManager km = (KeyguardManager) ctx.getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return km.isDeviceSecure();
            } else {
                return km.isKeyguardSecure();
            }
        } catch (Exception x) {
            Log.e(TAG, "Exception determining if screen has a lock (pin/swipe/password), will be assuming it does not", x);
            return false;
        }
    }

}
