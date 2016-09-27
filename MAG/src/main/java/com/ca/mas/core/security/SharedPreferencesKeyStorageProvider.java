/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Base64;

class SharedPreferencesKeyStorageProvider extends KeyStoreKeyStorageProvider {

    private static final String TAG = SharedPreferencesKeyStorageProvider.class.getCanonicalName();

    public static final String PREFS_NAME = "SECRET_PREFS";
    private SharedPreferences sharedpreferences;

    /**
     * Constructor to KeyStorageProvider
     *
     * @param ctx : requires context of the calling application
     */
    public SharedPreferencesKeyStorageProvider(@NonNull Context ctx) {
        super(ctx);
    }

    /**
     * This method stores the encrypted SecretKey in the locally, using SharedPreferences
     *
     * @param alias              :             the alias to store the Key against
     * @param encryptedSecretkey
     * @return : true or false
     */
    protected boolean storeSecretKeyLocally(String alias, byte[] encryptedSecretkey) {
        sharedpreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        String stringToSave = Base64.encodeToString(encryptedSecretkey, Base64.DEFAULT);
        editor.putString(alias, stringToSave);
        editor.apply();
        return true;
    }

    /**
     * This method checks whether the encrypted secretKey is stored locally or not
     *
     * @param alias : the alias against which to store the key against
     * @return: true or false
     */
    protected boolean containsSecretKeyLocally(String alias) {
        sharedpreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedpreferences.contains(alias);

    }

    /**
     * This method deletes the key from the local storage
     *
     * @param alias : the alias to find the key
     * @return: true or false
     */
    protected boolean deleteSecretKeyLocally(String alias) {
        sharedpreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.remove(alias);
        editor.apply();
        return true;
    }

    protected byte[] getEncryptedSecretKey(String alias){
        sharedpreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        byte[] encryptedSecretKey;
        String sretrieve = sharedpreferences.getString(alias, "default");
        encryptedSecretKey = Base64.decode(sretrieve, Base64.DEFAULT);
        return encryptedSecretKey;
    }
}
