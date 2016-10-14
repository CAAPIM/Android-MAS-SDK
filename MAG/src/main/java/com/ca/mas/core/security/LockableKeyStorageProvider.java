package com.ca.mas.core.security;

import android.os.Build;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.security.*;
import java.security.KeyStore;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

@RequiresApi(api = Build.VERSION_CODES.M)
public class LockableKeyStorageProvider implements KeyStorageProvider {
    private static final String TAG = LockableKeyStorageProvider.class.getCanonicalName();
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private SecretKey secretKey;
    private String suffix;

    public LockableKeyStorageProvider(@NonNull String suffix) {
        this.suffix = suffix;
    }

    @Override
    public void storeKey(String alias, SecretKey sk) {

        java.security.KeyStore ks;
        try {
            ks = java.security.KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Error instantiating Android keyStore");
            throw new RuntimeException("Error instantiating Android keyStore", e);
        }

        KeyProtection kp = new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setUserAuthenticationRequired(true)
                .setUserAuthenticationValidityDurationSeconds(-1)
                .build();

        try {
            this.secretKey = sk;
            ks.setEntry(getKeyName(alias), new KeyStore.SecretKeyEntry(sk), kp);
        } catch (KeyStoreException e) {
            Log.e(TAG, "Error setting entry into Android keyStore");
            throw new RuntimeException("Error setting entry into Android keyStore", e);
        }
    }

    @Override
    public SecretKey getKey(String alias) {

        if (secretKey != null) {
            return secretKey;
        }

        java.security.KeyStore ks;
        try {
            ks = java.security.KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            java.security.KeyStore.SecretKeyEntry entry = (java.security.KeyStore.SecretKeyEntry) ks.getEntry(getKeyName(alias), null);
            if (entry == null) {
                return null;
            } else {
                return entry.getSecretKey();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while getting SecretKey", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean containsKey(String alias) {
        KeyStore ks;

        try {
            ks = java.security.KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            return ks.containsAlias(getKeyName(alias));
        } catch (Exception e) {
            Log.e(TAG, "Error while getting SecretKey", e);
            return false;
        }
    }

    private String getKeyName(String alias) {
        return alias + suffix;
    }

    public void lock() {
        if (secretKey != null) {
            if (secretKey instanceof Destroyable) {
                Destroyable destroyable = (Destroyable) secretKey;
                try {
                    destroyable.destroy();
                } catch (DestroyFailedException e) {
                    Log.e(TAG, "Could not destroy key");
                }
            }
            secretKey = null;
        }
    }

    public void removeKey(String alias) {
        try {
            KeyStore ks = java.security.KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
            ks.deleteEntry(getKeyName(alias));
        } catch (Exception e) {
            Log.e(TAG, "Error while delete SecretKey", e);
        }
    }
}
