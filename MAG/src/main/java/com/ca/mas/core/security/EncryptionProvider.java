package com.ca.mas.core.security;

public interface EncryptionProvider {

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
