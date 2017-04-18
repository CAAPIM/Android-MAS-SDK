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
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.util.KeyUtils;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import static com.ca.mas.core.MAG.DEBUG;
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

class KeyStorageScreenLockCanChange extends SharedPreferencesKeyStorageProvider {

    public static final String PREFS_NAME = "SECRET_PREFS";
    private SharedPreferences sharedpreferences;

    /**
     * Constructor to KeyStorageProvider
     *
     * @param ctx requires context of the calling application
     */
    public KeyStorageScreenLockCanChange(@NonNull Context ctx) {
        super(ctx);

        // Symmetric Key Manager creates symmetric keys,
        //   stored inside AndroidKeyStore for Android.M+
        keyMgr = new DefaultKeySymmetricManager("AES", 256, false, false, 100000, false);
    }


    /**
     * Determine if there is a screen lock
     */
    protected boolean deviceHasScreenLock()
    {
        try {
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ( km.isDeviceSecure() )
                    return true;
                else
                    return false;
            } else {
                if ( km.isKeyguardSecure() )
                    return true;
                else
                    return false;
            }
        } catch (Exception x) {
            Log.e(TAG, "Exception determining if screen has a lock (pin/swipe/password), will be assuming it does not", x);
            return false;
        }
    }


    /**
     * Retrieve the SecretKey from Storage
     *
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    @Override
    public SecretKey getKey(String alias) {

        // if there is no screen lock, then delete the key and return nothing!!!
        if (! deviceHasScreenLock()) {
            Log.w(TAG, "KeyStorageScreenLockCanChange getKey there is no screen lock (pin/swipe/password), so the key will be deleted");
            removeKey(alias);
            throw new RuntimeException("KeyStorageScreenLockCanChange getKey there is no screen lock (pin/swipe/password), so the encryption key has been deleted");
        }

        // For Android.M+, if this key was created we'll find it here
        SecretKey secretKey = keyMgr.retrieveKey(alias);

        if (secretKey == null) {

            // check if the key is present locally
            byte encryptedSecretKey[] = getEncryptedSecretKey(alias);
            if (encryptedSecretKey != null) {
                try {
                    secretKey = decryptSecretKey(encryptedSecretKey);
                } catch (Exception unableToDecrypt) {
                    if (DEBUG)
                        Log.e(TAG, "Error while decrypting SecretKey, deleting it", unableToDecrypt);

                    deleteSecretKeyLocally(alias);
                    encryptedSecretKey = null;
                }
            }

            // if still no key, generate one
            if (secretKey == null) {
                secretKey = keyMgr.generateKey(alias);

                // if this is Pre- Android.M, we need to store it locally
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    encryptedSecretKey = encryptSecretKey(secretKey);
                    storeSecretKeyLocally(alias, encryptedSecretKey);
                }
            } else {
                // if this is Android.M+, check if the operating system was upgraded
                //   and we can now store the SecretKey in the AndroidKeyStore
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    keyMgr.storeKey(alias, secretKey);
                    deleteSecretKeyLocally(alias);
                }
            }
        }

        return secretKey;
    }


    /**
     * This method encrypts a SecretKey using an RSA key.
     * This is intended for Pre-Android.M, where the
     * SecretKey cannot be stored in the AndroidKeyStore
     *
     * @param secretKey SecretKey to encrypt
     */
    protected byte[] encryptSecretKey(SecretKey secretKey) {
        try {
            PublicKey publicKey = KeyUtils.getRsaPublicKey(ASYM_KEY_ALIAS);
            if (publicKey == null) {
                KeyUtils.generateRsaPrivateKey(context, 2048,
                        ASYM_KEY_ALIAS, String.format("CN=%s, OU=%s", ASYM_KEY_ALIAS, "com.ca"),
                        false, false, -1, false);
                publicKey = KeyUtils.getRsaPublicKey(ASYM_KEY_ALIAS);
            }

            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(secretKey.getEncoded());

        } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException
                | NoSuchPaddingException | NoSuchAlgorithmException | NoSuchProviderException
                | IOException | InvalidParameterException | KeyStoreException
                | InvalidAlgorithmParameterException | CertificateException
                | UnrecoverableKeyException e) {
            if (DEBUG) Log.e(TAG, "Error while encrypting SecretKey", e);
            throw new RuntimeException("Error while encrypting SecretKey", e);
        }
    }



}
