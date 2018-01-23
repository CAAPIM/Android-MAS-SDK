/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

import android.os.Build;

import java.security.Key;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * Abstract class to access and store keys and Certificate to the Android KeyStore.
 */
public abstract class KeyStoreRepository {

    /**
     * Retrieve Private from keystore
     *
     * @param alias Private Key Alias
     * @return The Private Key
     * @throws KeyStoreException
     */
    public abstract Key getPrivateKey(String alias) throws KeyStoreException;

    /**
     * Retrieve Public from keystore
     *
     * @param alias Public Key Alias
     * @return The Public Key
     * @throws KeyStoreException
     */
    public abstract Key getPublicKey(String alias) throws KeyStoreException;

    /**
     * Delete Key from keystore
     *
     * @param alias Key Alias
     */
    public abstract void deleteKey(String alias);

    /**
     * Create Private key and store to keystore
     *
     * @param alias      Alias to store the key pair
     * @param attributes Attribute to generate the private key
     * @return The KeyPair
     * @throws KeyStoreException
     */
    public abstract KeyPair createPrivateKey(String alias, GenerateKeyAttribute attributes) throws KeyStoreException;

    /**
     * Save certificate chain to keystore
     *
     * @param alias Alias to store the certificate chain
     * @param chain The certificate chain.
     * @throws KeyStoreException
     */
    public abstract void saveCertificateChain(String alias, X509Certificate[] chain) throws KeyStoreException;

    /**
     * Retrieve the stored Certificate Chain
     *
     * @param alias Alias for the Certificate Chain
     * @return The Certificate Chain stored in the keystore or null if empty
     * @throws KeyStoreException
     */
    public abstract X509Certificate[] getCertificateChain(String alias) throws KeyStoreException;

    /**
     * Delete certificate chain from keystore
     *
     * @param alias Alias for the Certificate Chain
     */
    public abstract void deleteCertificateChain(String alias);

    /**
     * Generate Certificate Signing Request
     *
     * @return The Certificate Signing Request
     * @throws CertificateException
     */
    public abstract byte[] generateCertificateSigningRequest(String commonName, String deviceId, String deviceName, String organization, PrivateKey privateKey, PublicKey publicKey) throws CertificateException;

    private static final KeyStoreRepository keyStoreRepository;
    private static final boolean pinRequired;

    static {
        switch (Build.VERSION.SDK_INT) {
            case Build.VERSION_CODES.JELLY_BEAN:
            case Build.VERSION_CODES.JELLY_BEAN_MR1:
                pinRequired = true;
                keyStoreRepository = new AndroidJellyBeanKeyRepository();
                break;
            case Build.VERSION_CODES.JELLY_BEAN_MR2:
                pinRequired = false;
                keyStoreRepository = new AndroidJellyBeanMR2KeyRepository();
                break;
            case Build.VERSION_CODES.KITKAT:
            case Build.VERSION_CODES.KITKAT_WATCH:
            case Build.VERSION_CODES.LOLLIPOP:
            case Build.VERSION_CODES.LOLLIPOP_MR1:
                pinRequired = false;
                keyStoreRepository = new AndroidKitKatKeyRepository();
                break;
            case Build.VERSION_CODES.M:
                pinRequired = false;
                keyStoreRepository = new AndroidMKeyRepository();
                break;
            case Build.VERSION_CODES.N:
                pinRequired = false;
                keyStoreRepository = new AndroidNKeyRepository();
                break;
            default:
                pinRequired = false;
                keyStoreRepository = new AndroidNKeyRepository();
        }
    }

    public static KeyStoreRepository getKeyStoreRepository() {
        return keyStoreRepository;
    }

    public static boolean isPinRequired() {
        return pinRequired;
    }


}
