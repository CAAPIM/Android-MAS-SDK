/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

import com.ca.mas.core.cert.CertUtils;

import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.security.auth.x500.X500Principal;

public class AndroidJellyBeanKeyRepository extends KeyStoreRepository {

    private static final String PUTBLIC_KEY = "_public_key";
    private KeyStore keyStore = KeyStoreAdapter.getKeyStore();

    @Override
    public Key getPrivateKey(String alias) throws KeyStoreException {
        byte[] privateKey = keyStore.get(alias);
        if (privateKey != null) {
            try {
                return decodeRsaPrivateKey(privateKey);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public Key getPublicKey(String alias) throws KeyStoreException {
        byte[] publicKey = keyStore.get(alias + PUTBLIC_KEY);
        if (publicKey != null) {
            try {
                return decodeRsaPublicKey(publicKey);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }

    @Override
    public void deleteKey(String alias) {
        keyStore.delete(alias);
        keyStore.delete(alias + PUTBLIC_KEY);
    }

    @Override
    public KeyPair createPrivateKey(String alias, GenerateKeyAttribute attributes) throws KeyStoreException {
        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", new BouncyCastleProvider());
            kpg.initialize(attributes.getKeySize());
            KeyPair keyPair = kpg.generateKeyPair();
            byte[] privateKey = encodeRsaPrivateKey(keyPair.getPrivate());
            byte[] publicKey = encodeRsaPublicKey(keyPair.getPublic());
            keyStore.put(alias, privateKey);
            keyStore.put(alias + PUTBLIC_KEY, publicKey);
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            throw new KeyStoreException(e);
        }
    }

    @Override
    public void saveCertificateChain(String alias, X509Certificate[] chain) {
        byte[] certs = CertUtils.encodeCertificateChain(chain);
        keyStore.put(alias, certs);
    }

    @Override
    public X509Certificate[] getCertificateChain(String alias) throws KeyStoreException {
        byte[] certs = keyStore.get(alias);
        return CertUtils.decodeCertificateChain(certs);
    }

    @Override
    public void deleteCertificateChain(String alias) {
        keyStore.delete(alias);
    }

    @Override
    public byte[] generateCertificateSigningRequest(String commonName, String deviceId, String deviceName, String organization, PrivateKey privateKey, PublicKey publicKey) throws CertificateException {
        try {
            commonName = commonName.replace("\"", "\\\"");
            deviceId = deviceId.replace("\"", "\\\"");
            deviceName = deviceName.replace("\"", "\\\"");
            organization = organization.replace("\"", "\\\"");

            // remove special characters from device name
            deviceName = deviceName.replaceAll("[^a-zA-Z0-9]","");

            X500Principal subject = new X500Principal("cn=\"" + commonName +
                    "\", ou=\"" + deviceId +
                    "\", dc=\"" + deviceName +
                    "\", o=\"" + organization + "\"");
            ASN1Set attrs = new DERSet(new ASN1EncodableVector());
            PKCS10CertificationRequest csr = new PKCS10CertificationRequest("SHA1withRSA", subject, publicKey, attrs, privateKey, null);
            return csr.getEncoded();
        } catch (Exception e) {
            throw new CertificateException("Unable to generate certificate signing request: " + e.getMessage(), e);
        }
    }

    /**
     * Convert the specified private key into encoded key bytes in PKCS#8 format.
     *
     * @param privateKey the private key to encode.  Required.
     * @return the encoded form of this key.  Never null.
     * @throws IllegalArgumentException if the key is not RSA or cannot be encoded (perhaps it is a hardware key that cannot be exported)
     */
    private static byte[] encodeRsaPrivateKey(PrivateKey privateKey) {
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
    private PrivateKey decodeRsaPrivateKey(byte[] pkcs8EncodedKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(pkcs8EncodedKeyBytes));
    }

    /**
     * Convert the specified RSA public key into an X.509 SubjectPublicKeyInfo structure.
     *
     * @param publicKey the public key to encode.  Required.
     * @return the X.509 encoded bytes of the public key.  Never null.
     * @throws IllegalArgumentException if the key is not RSA or cannot be encoded.
     */
    private byte[] encodeRsaPublicKey(PublicKey publicKey) {
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
    private PublicKey decodeRsaPublicKey(byte[] x509EncodedKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(x509EncodedKeyBytes));
    }
}
