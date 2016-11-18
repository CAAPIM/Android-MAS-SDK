/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.os.Build;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

@RequiresApi(api = Build.VERSION_CODES.M)
public class LockableKeyStorageProvider implements KeyStorageProvider {
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private SecretKey secretKey;

    @Override
    public void storeKey(String alias, SecretKey sk) {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            if (DEBUG) Log.e(TAG, "Error instantiating Android KeyStore.", e);
            throw new RuntimeException("Error instantiating Android KeyStore.", e);
        }

        KeyProtection kp = new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(5)
                .build();

        try {
            this.secretKey = sk;
            ks.setEntry(alias, new KeyStore.SecretKeyEntry(sk), kp);
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error setting entry into Android KeyStore.", e);
            throw new RuntimeException("Error setting entry into Android KeyStore.", e);
        }
    }

    @Override
    public SecretKey getKey(String alias) {
        if (secretKey != null) {
            return secretKey;
        }

        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(alias, null);
            return entry == null ? null : entry.getSecretKey();
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error while getting SecretKey", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containsKey(String alias) {
        try {
            KeyStore ks = java.security.KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            return ks.containsAlias(alias);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error while getting SecretKey", e);
            return false;
        }
    }

    public void lock() {
        if (secretKey != null && secretKey instanceof Destroyable) {
            Destroyable destroyable = (Destroyable) secretKey;
            try {
                destroyable.destroy();
            } catch (DestroyFailedException e) {
                if (DEBUG) Log.e(TAG, "Could not destroy key");
            }
        }
        secretKey = null;
    }

    public void removeKey(String alias) {
        try {
            KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            ks.deleteEntry(alias);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error while delete SecretKey", e);
        }
    }
}
