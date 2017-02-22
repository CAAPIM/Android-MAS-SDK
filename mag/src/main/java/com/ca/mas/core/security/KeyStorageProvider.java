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
 * This interface manages cryptographic keys
 */
public interface KeyStorageProvider {

    /**
     * Retrieve the SecretKey from Storage
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    SecretKey getKey(String alias);

    /**
     * Remove the key
     * @param alias the alias of the key to remove
     */
    boolean removeKey(String alias);
}
