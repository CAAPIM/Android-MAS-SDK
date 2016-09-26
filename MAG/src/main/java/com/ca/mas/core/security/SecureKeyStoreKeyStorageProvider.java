package com.ca.mas.core.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.datasource.AccountManagerStoreDataSource;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceFactory;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

/**
 * Created by kalsa12 on 2016-09-23.
 */

public class SecureKeyStoreKeyStorageProvider extends KeyStoreKeyStorageProvider {

    private static final String TAG = SecureKeyStoreKeyStorageProvider.class.getCanonicalName();
    private DataSource<String, byte[]> storage;


    public SecureKeyStoreKeyStorageProvider(@NonNull Context ctx) {
        super(ctx);
        JSONObject params = new JSONObject();
        try {
            params.put("share", Boolean.TRUE);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        storage = DataSourceFactory.getStorage(ctx, AccountManagerStoreDataSource.class, params, null);
    }

    @Override
    protected boolean storeSecretKeyLocally(String alias, byte[] encryptedSecretkey) {
        storage.put(alias, encryptedSecretkey);
        return true;
    }

    @Override
    protected SecretKey getSecretKeyLocally(String alias) {
        byte[] encryptedSecretKey = storage.get(alias);

        java.security.KeyStore ks;
        try {
            ks = java.security.KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            Log.e(TAG, "Error while instantiating Android KeyStore");
            throw new RuntimeException("Error while instantiating Android KeyStore", e);
        }
        try {
            ks.load(null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            Log.e(TAG, "Error while instantiating Android KeyStore");
            throw new RuntimeException("Error while instantiating Android KeyStore", e);
        }
        java.security.KeyStore.Entry entry;
        PrivateKey privateKey;
        try {
            entry = ks.getEntry(ASYM_KEY_ALIAS, null);
            privateKey = ((java.security.KeyStore.PrivateKeyEntry) entry).getPrivateKey();
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | NullPointerException e) {
            Log.e(TAG, "Error while retrieving aysmmetric key from keystore", e);
            throw new RuntimeException("Error while retrieving aysmmetric key from keystore", e);
        }

        try {
            return decryptSecretKey(encryptedSecretKey, privateKey);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            Log.e(TAG, "Error while decrypting SecretKey", e);
            throw new RuntimeException("Error while  decrypting SecretKey", e);
        }
    }

    @Override
    protected boolean containsSecretkeyLocally(String alias) {
        byte[] encryptedSecretKey = storage.get(alias);
        return (encryptedSecretKey != null);
    }

    @Override
    protected boolean deleteSecretKeyLocally(String alias) {
        storage.remove(alias);
        return true;
    }
}
