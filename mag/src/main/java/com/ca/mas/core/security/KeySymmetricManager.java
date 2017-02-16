/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;

/**
 * This interface is used to generate a SecretKey for encryption purposes.
 *     For Android.M+, the key will be created in the AndroidKeyStore
 *         and will remain there.
 *     For Pre-Android.M, the key will be created and returned, and
 *         a KeyStorageProvider must be used to protect the key.  If
 *         the user upgrades to Android.M+, the key will be moved
 *         to the AndroidKeyStore.
 */
interface KeySymmetricManager {

    /**
     * Return a secretKey to be used for encryption.
     *   For Android.M+, the key will be protected by 
     *   the AndroidKeyStore.  If pre-M, the SecretKey
     *   must be stored using a KeyStorageProvider.
     */
    SecretKey generateKey(String alias);

    /**
     * Retrieve the key from the AndroidKeyStore
     * @return the SecretKey, or null if not present
     */
    SecretKey retrieveKey(String alias);

    /**
     * Stores a SecretKey in the AndroidKeyStore.
     *   This is only useful when the phone is upgraded 
     *   to Android.M, where an existing key was stored
     *   outside of the AndroidKeyStore.
     */
    void storeKey(String alias, SecretKey secretKey);

    /**
     * This method deletes the given key from AndroidKeyStore
     *
     * @param alias
     */
    void deleteKey(String alias);

}
