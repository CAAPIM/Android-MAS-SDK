/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.util;

import org.spongycastle.jce.provider.BouncyCastleProvider;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * Utility methods for working with keys and key pairs.
 */
public class KeyUtils {

    /**
     * Generate a new RSA keypair with the specified number of key bits.
     *
     * @param keysize the key size in bits, eg 1024.
     * @return a new RSA keypair.  Never null.
     * @throws RuntimeException if an RSA key pair of the requested size cannot be generated
     */
    public static KeyPair generateRsaKeyPair(int keysize) {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", new BouncyCastleProvider());
            kpg.initialize(keysize);
            return kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert the specified private key into encoded key bytes in PKCS#8 format.
     *
     * @param privateKey the private key to encode.  Required.
     * @return the encoded form of this key.  Never null.
     * @throws IllegalArgumentException if the key is not RSA or cannot be encoded (perhaps it is a hardware key that cannot be exported)
     */
    public static byte[] encodeRsaPrivateKey(PrivateKey privateKey) {
        if (!"RSA".equals(privateKey.getAlgorithm()))
            throw new IllegalArgumentException("Private key is not an RSA private key: " + privateKey.getAlgorithm());

        if (!"PKCS#8".equals(privateKey.getFormat()))
            throw new IllegalArgumentException("Private key encoding format is not PKCS#8: " + privateKey.getFormat());

        byte[] bytes = privateKey.getEncoded();
        if (bytes == null || bytes.length < 1)
            throw new IllegalArgumentException("Private key encoded form is null or empty");

        return bytes;
    }

    /**
     * Decode the specified PKCS#8 encoded RSA private key bytes into an RSA PrivateKey instance.
     *
     * @param pkcs8EncodedKeyBytes the PKCS#8 encoded RSA private key bytes.  Required.
     * @return the decoded PrivateKey instance, created using the current default RSA KeyFactory.  Never null.
     * @throws IllegalArgumentException if the key cannot be decoded
     */
    public static PrivateKey decodeRsaPrivateKey(byte[] pkcs8EncodedKeyBytes) {
        try {
            return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKeyBytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Convert the specified RSA public key into an X.509 SubjectPublicKeyInfo structure.
     *
     * @param publicKey the public key to encode.  Required.
     * @return the X.509 encoded bytes of the public key.  Never null.
     * @throws IllegalArgumentException if the key is not RSA or cannot be encoded.
     */
    public static byte[] encodeRsaPublicKey(PublicKey publicKey) {
        if (!"RSA".equals(publicKey.getAlgorithm()))
            throw new IllegalArgumentException("Public key is not an RSA private key: " + publicKey.getAlgorithm());

        final String format = publicKey.getFormat();
        if (!"X.509".equals(format) && !"X509".equals(format))
            throw new IllegalArgumentException("Public key encoding format is not X.509: " + format);

        byte[] bytes = publicKey.getEncoded();
        if (bytes == null || bytes.length < 1)
            throw new IllegalArgumentException("Public key encoded form is null or empty");

        return bytes;
    }

    /**
     * Decode the specified X.509 encoded SubjectPublicKeyInfo bytes into an RSA PublicKey instance.
     *
     * @param x509EncodedKeyBytes the X.509 encoded bytes of the public key.  Required.
     * @return the decoded PublicKey instance, created using the default RSA KeyFactory.  Never null.
     * @throws IllegalArgumentException if the key cannot be decoded
     */
    public static PublicKey decodeRsaPublicKey(byte[] x509EncodedKeyBytes) {
        try {
            return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(x509EncodedKeyBytes));
        } catch (InvalidKeySpecException e) {
            throw new IllegalArgumentException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private KeyUtils() {
    }
}
