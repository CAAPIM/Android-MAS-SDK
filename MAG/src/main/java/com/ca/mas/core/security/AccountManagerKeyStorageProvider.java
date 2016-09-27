package com.ca.mas.core.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.datasource.AccountManagerStoreDataSource;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceFactory;


import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by kalsa12 on 2016-09-23.
 */

public class AccountManagerKeyStorageProvider extends KeyStoreKeyStorageProvider {

    private static final String TAG = AccountManagerKeyStorageProvider.class.getCanonicalName();

    private DataSource<String, byte[]> storage;


    public AccountManagerKeyStorageProvider(@NonNull Context ctx) {
        super(ctx);
        JSONObject params = new JSONObject();
        try {
            params.put("share", Boolean.TRUE);
        } catch (JSONException e) {
            Log.w(TAG, "Failed to set sharing property " + e);
        }

        storage = DataSourceFactory.getStorage(ctx, AccountManagerStoreDataSource.class, params, null);
    }

    @Override
    protected boolean storeSecretKeyLocally(String alias, byte[] encryptedSecretkey) {
        storage.put(alias, encryptedSecretkey);
        return true;
    }

    @Override
    protected byte[] getEncryptedSecretKey(String alias) {
        byte[] encryptedSecretKey = storage.get(alias);
        return encryptedSecretKey;
    }

    @Override
    protected boolean containsSecretKeyLocally(String alias) {
        byte[] encryptedSecretKey = storage.get(alias);
        return (encryptedSecretKey != null);
    }

    @Override
    protected boolean deleteSecretKeyLocally(String alias) {
        storage.remove(alias);
        return true;
    }
}
