/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

import android.util.Log;

import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Enumeration;

import sun.security.pkcs.PKCS10;
import sun.security.x509.X500Signer;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

abstract class AndroidKeyStoreRepository extends KeyStoreRepository {

    // for certificate chains
    private static final int MAX_CHAIN = 9;

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    @Override
    public Key getPrivateKey(String alias) throws KeyStoreException {
        try {
            KeyStore keyStore = getKeyStore();
            return keyStore.getKey(alias, null);
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public Key getPublicKey(String alias) throws KeyStoreException {
        try {
            KeyStore keyStore = getKeyStore();
            java.security.cert.Certificate cert = keyStore.getCertificate(alias);
            if (cert != null) {
                return cert.getPublicKey();
            } else {
                return null;
            }
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public void deleteKey(String alias) {
        try {
            KeyStore keyStore = getKeyStore();
            keyStore.deleteEntry(alias);
        } catch (Exception ignore) {
            //ignore
        }
    }

    abstract AlgorithmParameterSpec getAlgorithmParameterSpec(String alias, GenerateKeyAttribute attribute) throws KeyStoreException;

    @Override
    public KeyPair createPrivateKey(String alias, GenerateKeyAttribute attributes) throws KeyStoreException {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);
            keyPairGenerator.initialize(getAlgorithmParameterSpec(alias, attributes));
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }

        return keyPairGenerator.generateKeyPair();
    }

    @Override
    public void saveCertificateChain(String alias, X509Certificate[] chain) throws KeyStoreException {
        try {
            KeyStore keyStore = getKeyStore();
            // delete any existing ones
            for (int i = 1; i <= MAX_CHAIN; i++) {
                keyStore.deleteEntry(alias + i);
            }
            // now add the new ones
            for (int i = 0; i < chain.length; i++) {
                keyStore.setCertificateEntry(alias + (i + 1), chain[i]);
            }
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }
    }

    private KeyStore getKeyStore() throws java.security.KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return keyStore;
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) throws KeyStoreException {
        try {
            X509Certificate[] returnChain = null;

            KeyStore keyStore = getKeyStore();
            // discover how many certificates are in the chain
            int numInChain = 0;
            for (Enumeration e = keyStore.aliases(); e.hasMoreElements(); ) {
                String aliasFound = (String) e.nextElement();
                if (aliasFound.startsWith(alias)) {
                    int numAlias = Integer.parseInt(aliasFound.replace(alias, ""));
                    if (numAlias > numInChain)
                        numInChain = numAlias;
                }
            }
            if (numInChain > 0) {
                returnChain = new X509Certificate[numInChain];
                for (int i = 0; i < numInChain; i++)
                    returnChain[i] = (X509Certificate) keyStore.getCertificate(alias + (i + 1));
            }

            return returnChain;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void deleteCertificateChain(String alias) {
        try {
            KeyStore keyStore = getKeyStore();
            for (int i = 1; i <= MAX_CHAIN; i++) {
                keyStore.deleteEntry(alias + i);
            }
        } catch (Exception ignore) {
            //ignore
        }

    }

    @Override
    public byte[] generateCertificateSigningRequest(String commonName, String deviceId, String deviceName, String organization, PrivateKey privateKey, PublicKey publicKey) throws CertificateException {
        try {
            PKCS10 pkcs10 = new PKCS10(publicKey);
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);

            commonName = commonName.replace("\"", "\\\"");
            deviceId = deviceId.replace("\"", "\\\"");
            deviceName = deviceName.replace("\"", "\\\"");
            organization = organization.replace("\"", "\\\"");

            // remove special characters from device name
            deviceName = deviceName.replaceAll("[^a-zA-Z0-9]","");

            sun.security.x509.X500Name x500Name = new sun.security.x509.X500Name(commonName,deviceId,deviceName,organization,true);

            pkcs10.encodeAndSign(new X500Signer(signature, x500Name));
            return pkcs10.getEncoded();
        } catch (Exception t) {
            if (DEBUG) Log.e(TAG, "Unable to generate certificate signing request: " + t, t);
            throw new CertificateException("Unable to generate certificate signing request: " + t);
        }
    }

}
