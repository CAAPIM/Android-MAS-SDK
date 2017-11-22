/*
 * Copyright (c) 2017 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.storage.sharedstorage;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.util.Log;

import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.foundation.MAS;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * MASSharedStorage provides the ability to share data between apps signed with the same key.
 * <p>
 * Requires the android.permission.AUTHENTICATE_ACCOUNTS and
 * android:name="android.permission.MANAGE_ACCOUNTS" permissions
 * in your application's AndroidManifest.xml.
 * <p>
 * The underlying implementation of this class uses the AccountManager APIs.
 * By default, the massharedauthenticator.xml file will be used to create the accounts,
 * but can be overridden in AndroidManifest.xml with your own xml file for another account type.
 */
@SuppressWarnings({"MissingPermission"})
public class MASSharedStorage {

    private static final String KEYINDEX_COLUMN_NAME = "mas_shared_storage_lookup_index";
    private AccountManager mAccountManager;
    private Account mAccount;
    private AccountIndexFormatter mAccountFormatter;

    /**
     * Creates or retrieves a MASSharedStorage with the specified name and account type.
     * Ensure that this does not conflict with any existing accountType on the device.
     *
     * @param accountName
     */
    public MASSharedStorage(String accountName, String accountType) {
        if (accountName == null || accountType == null) {
            throw new IllegalArgumentException("Account name or type cannot be null.");
        }

        mAccountManager = AccountManager.get(MAS.getContext());

        //Attempt to retrieve the account
        Account[] accounts = mAccountManager.getAccountsByType(accountType);
        for (Account account : accounts) {
            if (accountName.equals(account.name)) {
                mAccount = account;
            }
        }

        //Create the account if it wasn't retrieved
        if (mAccount == null) {
            mAccount = new Account(accountName, accountType);
            mAccountManager.addAccountExplicitly(mAccount, null, null);
        }

        mAccountFormatter = new AccountIndexFormatter();
    }

    private void preconditionCheck(String key) {
        //If the SDK hasn't been initialized, throw an IllegalStateException
        MobileSsoFactory.getInstance();

        //If the data key is null, throw an exception
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("Data key cannot be null or empty.");
        }
    }

    /**
     * Saves a string value to the shared storage with the given key.
     *
     * @param key
     * @param value
     */
    public void save(String key, String value) {
        preconditionCheck(key);
        mAccountManager.setUserData(mAccount, key, value == null ? "" : value);
        updateIndex(key);
    }

    /**
     * Saves a byte array to the shared storage with the given key.
     *
     * @param key
     * @param value
     */
    public void save(String key, byte[] value) {
        preconditionCheck(key);
        mAccountManager.setUserData(mAccount, key, value == null ? "" : new String(value));
        updateIndex(key);
    }

    private void updateIndex(String key) {
        String keyString = mAccountManager.getUserData(mAccount, KEYINDEX_COLUMN_NAME);
        Set<String> keys = mAccountFormatter.unmarshal(keyString);
        keys.add(key);
        String newKeyString = mAccountFormatter.marshal(keys);
        mAccountManager.setUserData(mAccount, KEYINDEX_COLUMN_NAME, newKeyString);
    }

    /**
     * Deletes a value in the shared storage assigned to the given key.
     * Functionally the same as calling save(key, null).
     *
     * @param key
     */
    public void delete(String key) {
        preconditionCheck(key);
        mAccountManager.setUserData(mAccount, key, null);
        removeIndex(key);
    }

    private void removeIndex(String key) {
        String keyString = mAccountManager.getUserData(mAccount, KEYINDEX_COLUMN_NAME);
        Set<String> keys = mAccountFormatter.unmarshal(keyString);
        keys.remove(key);
        String newKeyString = mAccountFormatter.marshal(keys);
        mAccountManager.setUserData(mAccount, KEYINDEX_COLUMN_NAME, newKeyString);
    }

    /**
     * Retrieves a string value in the shared storage given by the key.
     *
     * @param key
     * @return value associated with the key
     */
    public String getString(String key) {
        preconditionCheck(key);
        return mAccountManager.getUserData(mAccount, key);
    }

    /**
     * Retrieves a byte array in the shared storage given by the key.
     *
     * @param key
     * @return value associated with the key
     */
    public byte[] getBytes(String key) {
        preconditionCheck(key);
        String byteString = mAccountManager.getUserData(mAccount, key);
        if (byteString != null) {
            return byteString.getBytes();
        }
        return null;
    }

    private Set<String> keySet() {
        String keyBlob = mAccountManager.getUserData(mAccount, KEYINDEX_COLUMN_NAME);
        return mAccountFormatter.unmarshal(keyBlob);
    }

    /**
     * Handles key index marshalling and unmarshalling.
     */
    private class AccountIndexFormatter {

        private static final String ACCOUNT_KEY_SEPARATOR = ":";
        protected static final String UTF8 = "UTF-8";

        /**
         * @param keys
         * @return String representation of the keys
         */
        private String marshal(Set<String> keys) {
            if (keys == null || keys.isEmpty()) {
                return "";
            }
            try {
                StringBuilder buff = new StringBuilder();
                for (String keyElement : keys) {
                    buff.append(keyElement);
                    buff.append(ACCOUNT_KEY_SEPARATOR);
                }
                String keyBlob = buff.toString();
                keyBlob = keyBlob.endsWith(ACCOUNT_KEY_SEPARATOR)
                        ? keyBlob.substring(0, keyBlob.length() - 1)
                        : keyBlob;
                return keyBlob;
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "Error in marshalling: " + e);
                return "";
            }
        }

        /**
         * @param keyString
         * @return Set representation of the keys
         */
        private Set<String> unmarshal(String keyString) {
            Set<String> keys = new HashSet<>();
            if (keyString == null || keyString.length() == 0) {
                return keys;
            }

            keys.addAll(Arrays.asList(keyString.split(ACCOUNT_KEY_SEPARATOR)));
            return keys;
        }
    }
}
