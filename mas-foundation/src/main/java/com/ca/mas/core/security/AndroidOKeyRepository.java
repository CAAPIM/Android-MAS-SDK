/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

import android.util.Log;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

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

class AndroidOKeyStoreRepository extends AndroidKeyStoreRepository {

    // for certificate chains
    private static final int MAX_CHAIN = 9;

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    @Override
    AlgorithmParameterSpec getAlgorithmParameterSpec(String alias, GenerateKeyAttribute attribute) throws KeyStoreException {
        return null;
    }

    @Override
    public KeyPair createPrivateKey(String alias, GenerateKeyAttribute attributes) throws KeyStoreException {
        KeyPairGenerator keyPairGenerator = null;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance("RSA", new BouncyCastleProvider());
            keyPairGenerator.initialize(attributes.getKeySize());
        } catch (Exception e) {
            throw new KeyStoreException(e);
        }

        return keyPairGenerator.generateKeyPair();
    }

    private KeyStore getKeyStore() throws java.security.KeyStoreException, CertificateException, NoSuchAlgorithmException, IOException {
        KeyStore keyStore = KeyStore.getInstance("BKS");
        keyStore.load(null);
        return keyStore;
    }

}
