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
 * This interface is used to generate SecretKey for encryption purposes.
 */
interface KeyGenerator {

    /*
     * Return a secretKey to be used for encryption.
     */
    SecretKey generateKey() throws NoSuchAlgorithmException;
}
