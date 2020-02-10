/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.storage.sharedstorage;

import androidx.annotation.NonNull;

import java.util.List;

public interface StorageActions {

    /**
     * Saves a string value with the given key into the storage AccountManager || SharedPreferences.
     *
     * @param key string of the key to store the string value
     * @param value the string value to be stored
     */
    void save(@NonNull String key, String value);

    /**
     * Saves a byte array with the given key into the storage AccountManager || SharedPreferences
     *
     * @param key string of the key to store the byte[] value
     * @param value the byte[] value to be stored
     */
    void save(@NonNull String key, byte[] value);

    /**
     * Deletes any data with the given key in the storage.
     * Functionally the same as calling save(key, null).
     *
     * @param key string of the key to be deleted
     */
    void delete(@NonNull String key);

    /**
     * Retrieves a string value in the storage given by the key.
     *
     * @param key string of the key to retrieve the string value
     * @return value associated with the key
     */
    String getString(@NonNull String key);

    /**
     * Retrieves a byte array in the storage given by the key.
     *
     * @param key string of the key to retrieve the byte[] value
     * @return value associated with the key
     */
    byte[] getBytes(@NonNull String key);

    /**
     * Retrieves a List of all the keys currently saved in the storage
     */
    List<String> getKeys();

    /**
     * Delete all data related to the storage
     */
    void removeAll();
}
