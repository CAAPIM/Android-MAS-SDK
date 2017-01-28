/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.datasource.AccountManagerStoreDataSource;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.security.DefaultKeySymmetricManager;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

public class AccountManagerKeyStorageProvider extends KeyStoreKeyStorageProvider {

    private DataSource<String, byte[]> storage;

    /**
     * Constructor
     *
     * @param ctx
     */
    public AccountManagerKeyStorageProvider(@NonNull Context ctx) {
        super(ctx);

        JSONObject params = new JSONObject();
        try {
            params.put("share", Boolean.TRUE);
        } catch (JSONException e) {
            if (DEBUG) Log.w(TAG, "Failed to set sharing property " + e);
        }

        storage = DataSourceFactory.getStorage(ctx, AccountManagerStoreDataSource.class, params, null);
    }


    /**
     * @param alias              The alias to store the key against.
     * @param encryptedSecretkey The encrypted secret key to store.
     * @return
     */
    @Override
    protected boolean storeSecretKeyLocally(String alias, byte[] encryptedSecretkey) {
        storage.put(alias, encryptedSecretkey);
        return true;
    }

    /**
     * @param alias The alias for the required secret key.
     * @return
     */
    @Override
    protected byte[] getEncryptedSecretKey(String alias) {
        byte[] encryptedSecretKey = storage.get(alias);
        return encryptedSecretKey;
    }


    /**
     * Delete the secret key locally.
     *
     * @param alias
     * @return
     */
    @Override
    protected boolean deleteSecretKeyLocally(String alias) {
        storage.remove(alias);
        return true;
    }
}
