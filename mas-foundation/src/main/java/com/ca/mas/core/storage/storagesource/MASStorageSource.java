/*
 * Copyright (c) 2017 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.storage.storagesource;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ca.mas.core.security.EncryptionProvider;
import com.ca.mas.core.storage.StorageActions;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASFoundationStrings;

/**
 * MASSharedStorage is designed for developers to write, read, and delete String or byte[] data into
 * the AccountManager so that multiple applications signed with the same key and using the same
 * account name can share data.
 *
 * Note: the framework should be initialized prior to using any of MASSharedStorage's CRUD operations.
 * <p>
 * Requires the android.permission.AUTHENTICATE_ACCOUNTS and
 * android:name="android.permission.MANAGE_ACCOUNTS" permissions
 * in your application's AndroidManifest.xml.
 * <p>
 * By default, the massharedauthenticator.xml file will be used to create the accounts,
 * but can be overridden in AndroidManifest.xml with your own xml file for another account type.
 */
@SuppressWarnings({"MissingPermission"})
public class MASStorageSource {

    private StorageActions storageProvider;
    private boolean isSharedStorage;
    private Context context;
    private String mAccountName;

    /**
     * Creates or retrieves a MASSharedStorage with the specified name and account type.
     * Ensure that this does not conflict with any existing accountType on the device.
     *
     * @param accountName the name of the account to be created in the AccountManager
     */
    public MASStorageSource(@NonNull String accountName, boolean shared) {
        if (accountName == null || accountName.isEmpty()) {
            throw new IllegalArgumentException(MASFoundationStrings.SHARED_STORAGE_NULL_ACCOUNT_NAME);
        }

        // The SDK must be initialized before creating the storage
        context = MAS.getContext();

        if (context == null) {
            throw new IllegalStateException(MASFoundationStrings.SDK_UNINITIALIZED);
        }


        mAccountName = accountName;
        isSharedStorage = shared;
        storageProvider = getStorageProvider();
    }

    public MASStorageSource(String id) {
        new MASStorageSource(id, false);
    }

    protected void preconditionCheck(String key) {
        //If the SDK hasn't been initialized, throw an IllegalStateException
        if (MAS.getContext() == null) {
            throw new IllegalArgumentException("The SDK should be initialized.");
        }

        //If the data key is null, throw an exception
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException("Data key should be a String that cannot be null or empty.");
        }
    }

    /**
     * Saves a string value with the given key into the shared storage.
     *
     * @param key string of the key to store the string value
     * @param value the string value to be stored
     */
    public void save(@NonNull String key, String value) {
        preconditionCheck(key);
        storageProvider.save(key,value);
    }

    /**
     * Saves a byte array with the given key into the shared storage.
     *
     * @param key string of the key to store the byte[] value
     * @param value the byte[] value to be stored
     */
    public void save(@NonNull String key, byte[] value) {
        preconditionCheck(key);
        storageProvider.save(key, value);
    }

    /**
     * Deletes any data with the given key in the shared storage.
     * Functionally the same as calling save(key, null).
     *
     * @param key string of the key to be deleted
     */
    public void delete(@NonNull String key) {
        preconditionCheck(key);
        storageProvider.delete(key);
    }


    /**
     * Retrieves a string value in the shared storage given by the key.
     *
     * @param key string of the key to retrieve the string value
     * @return value associated with the key
     */
    public String getString(String key) {
        preconditionCheck(key);
        String value = storageProvider.getString(key);
        return  value;
    }

    /**
     * Retrieves a byte array in the shared storage given by the key.
     *
     * @param key string of the key to retrieve the byte[] value
     * @return value associated with the key
     */
    public byte[] getBytes(String key) {
        preconditionCheck(key);
        return storageProvider.getBytes(key);
    }

    protected EncryptionProvider getEncryptionProvider () {
        return new EncryptionProvider() {
            public byte[] encrypt(byte[] data) {
                return data;
            }

            public byte[] decrypt(byte[] data) {
                return data;
            }
        };
    }

    private StorageActions getStorageProvider() {
        StorageActions storage;
        if (isSharedStorage) {
            storage = new AccountManagerUtil(context, mAccountName);
        } else {
            storage = new SharedPreferencesUtil(context, mAccountName);
        }

        return storage;
    }
}
