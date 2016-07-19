/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

public interface MASEncryptionProvider {

    /**
     * @param plaintext bytes to encrypt
     * @return encrypted data
     */
    byte[] encrypt(byte[] plaintext);

    /**
     * @param encryptedData bytes to decrypt
     * @return decrypted data
     */
    byte[] decrypt(byte[] encryptedData);
}
