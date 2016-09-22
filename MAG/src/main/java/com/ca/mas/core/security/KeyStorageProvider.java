/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import javax.crypto.SecretKey;

/**
 * This interface provides a storage mechanism for cryptographic keys
 */
public interface KeyStorageProvider {

    /**
     * Stores the Secret key in secure Storage
     * @param alias: The alias to store the key against
     * @param sk : the SecretKey
     */
    void storeKey(String alias, SecretKey sk);

    /**
     * Retrieve the SecretKey from Storage
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    SecretKey getKey(String alias);

    /**
     * Checks whether the key exists or not in the storage
     * @param alias: the alias to find the key
     * @return true or false
     */
    boolean containsKey(String alias);
}
