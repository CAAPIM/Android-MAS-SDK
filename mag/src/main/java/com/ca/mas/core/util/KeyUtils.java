/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;

import javax.security.auth.x500.X500Principal;

import static android.security.keystore.KeyProperties.BLOCK_MODE_CBC;
import static android.security.keystore.KeyProperties.BLOCK_MODE_CTR;
import static android.security.keystore.KeyProperties.BLOCK_MODE_ECB;
import static android.security.keystore.KeyProperties.BLOCK_MODE_GCM;
import static android.security.keystore.KeyProperties.DIGEST_MD5;
import static android.security.keystore.KeyProperties.DIGEST_NONE;
import static android.security.keystore.KeyProperties.DIGEST_SHA1;
import static android.security.keystore.KeyProperties.DIGEST_SHA256;
import static android.security.keystore.KeyProperties.DIGEST_SHA384;
import static android.security.keystore.KeyProperties.DIGEST_SHA512;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_PKCS7;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_RSA_OAEP;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1;
import static android.security.keystore.KeyProperties.SIGNATURE_PADDING_RSA_PKCS1;
import static android.security.keystore.KeyProperties.SIGNATURE_PADDING_RSA_PSS;


/**
 * Utility methods for working with keys and key pairs.
 */
public class KeyUtils {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final int MAX_CHAIN = 9;

    /**
     * Generate a new RSA keypair with the specified number of key bits.
     *     This will always create the key pair inside the AndroidKeyStore,
     *        ensuring it is always protected.
     *
     * @param context needed for generating key pre-M
     * @param keysize the key size in bits, eg 2048.
     * @param alias the keystore alias to use
     * @param deviceLockRequired whether or not device lock is required to protect private key
     * @return a new RSA PrivateKey.
     * @throws RuntimeException if an RSA key pair of the requested size cannot be generated
     */
    public static PrivateKey generateRsaPrivateKey(Context context, int keysize, String alias, boolean deviceLockRequired)
            throws java.security.InvalidAlgorithmParameterException, java.io.IOException,
            java.security.KeyStoreException, java.security.NoSuchAlgorithmException,
            java.security.NoSuchProviderException, java.security.cert.CertificateException,
            java.security.UnrecoverableKeyException
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // use KeyGenParameterSpec.Builder, new in Marshmallow
            return generateRsaPrivateKeyAndroidM(keysize, alias, deviceLockRequired);
        }

        // For Android Pre-M
        // use the KeyPairGeneratorSpec.Builder, deprecated as of Marshmallow
        //    generates the key inside the AndroidKeyStore, always protected
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keysize, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);

        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();

        if (deviceLockRequired)
            kpg.initialize(new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setAlgorithmParameterSpec(spec)
                    .setStartDate(now).setEndDate(end)
                    .setSerialNumber(BigInteger.valueOf(1))
                    .setSubject(new X500Principal("CN=msso"))
                    .setEncryptionRequired()
                    .build());
        else
            kpg.initialize(new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setAlgorithmParameterSpec(spec)
                    .setStartDate(now).setEndDate(end)
                    .setSerialNumber(BigInteger.valueOf(1))
                    .setSubject(new X500Principal("CN=msso"))
                    .build());
        return kpg.generateKeyPair().getPrivate();
    }

    /**
     * This method generates an RSA asymmetric key pair directly in
     *    the AndroidKeyStore.
     *
     * @param keysize the key size in bits, eg 2048.
     * @param alias the alias against which to store the key against
     * @param deviceLockRequired whether or not device lock is required to protect private key
     * @return a new RSA keypair, created in and protected by the AndroidKeyStore, with an
     *        unusable self-signed certificate
     */
    @TargetApi(Build.VERSION_CODES.M)
    protected static PrivateKey generateRsaPrivateKeyAndroidM(int keysize, String alias, boolean deviceLockRequired)
            throws java.security.InvalidAlgorithmParameterException, java.io.IOException,
            java.security.KeyStoreException, java.security.NoSuchAlgorithmException,
            java.security.NoSuchProviderException, java.security.cert.CertificateException,
            java.security.UnrecoverableKeyException
    {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();
        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT + KeyProperties.PURPOSE_DECRYPT + KeyProperties.PURPOSE_SIGN + KeyProperties.PURPOSE_VERIFY)
                        .setKeySize(keysize)
                        .setCertificateNotBefore(now).setCertificateNotAfter(end)
                        .setCertificateSubject(new X500Principal("CN=msso"))
                        .setCertificateSerialNumber(BigInteger.valueOf(1))
                        //.setUserAuthenticationRequired(deviceLockRequired)
                        .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_ECB, BLOCK_MODE_GCM)
                        .setDigests(DIGEST_NONE, DIGEST_MD5, DIGEST_SHA1, DIGEST_SHA256, DIGEST_SHA384, DIGEST_SHA512)
                        .setEncryptionPaddings(ENCRYPTION_PADDING_NONE, ENCRYPTION_PADDING_PKCS7, ENCRYPTION_PADDING_RSA_OAEP, ENCRYPTION_PADDING_RSA_PKCS1)
                        .setRandomizedEncryptionRequired(false)
                        .setSignaturePaddings(SIGNATURE_PADDING_RSA_PSS, SIGNATURE_PADDING_RSA_PKCS1)
                        .build());
        return keyPairGenerator.generateKeyPair().getPrivate();
    }

    /**
     * Get the existing private key.
     *     Note: the initial self-signed public cert is not usable.
     *
     * @param alias the alias of the existing private key
     * @return the Private Key object
     */
    public static PrivateKey getRsaPrivateKey(String alias)
            throws java.io.IOException, java.security.KeyStoreException,
            java.security.NoSuchAlgorithmException, java.security.cert.CertificateException,
            java.security.UnrecoverableKeyException
    {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return (PrivateKey) keyStore.getKey(alias, null);
    }


    /**
     * Get the existing public key.
     *     Note: the private key will be associated with the initial
     *     self-signed public certificate.  The PublicKey can be
     *     used in a Certificate Signing Request.
     *
     * @param alias the alias of the existing private key
     * @return the Private Key object
     */
    public static PublicKey getRsaPublicKey(String alias)
            throws java.io.IOException, java.security.KeyStoreException,
            java.security.NoSuchAlgorithmException, java.security.cert.CertificateException
    {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        Certificate cert = keyStore.getCertificate(alias);
        if (cert != null)
            return cert.getPublicKey();
        else
            return null;
    }


    /**
     * Remove the existing public key.
     *     Note: the initial self-signed public cert is not usable.
     *
     * @param alias the alias of the existing private key +
     *              self-signed public key
     * @return the Private Key object
     */
    public static void deletePrivateKey(String alias)
            throws java.security.cert.CertificateException, java.io.IOException,
            java.security.KeyStoreException, java.security.NoSuchAlgorithmException
    {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        keyStore.deleteEntry(alias);
    }


    /**
     * This will install or replace the existing certificate chain in the AndroidKeyStore.
     *       The chain will be stored as "alias#".
     * @param aliasPrefix the alias prefix to use, will be appended with position number,
     *                    where position 0 in the array will be 1.
     * @param chain array of certificates in chain.  Typically the public certificate
     *              matching the private key will be in array position [0].
     */
    public static void setCertificateChain(String aliasPrefix, X509Certificate chain[])
            throws java.io.IOException, java.security.KeyStoreException,
            java.security.NoSuchAlgorithmException, java.security.cert.CertificateException
    {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        // delete any existing ones
        for (int i=1; i<=MAX_CHAIN; i++) {
            keyStore.deleteEntry(aliasPrefix + i);
        }
        // now add the new ones
        for (int i=0; i<chain.length; i++) {
            keyStore.setCertificateEntry(aliasPrefix + (i+1), chain[i]);
        }
    }

    /**
     * Clear any existing certificates in the public certificate chain.
     * @param aliasPrefix
     * @return
     */
    public static X509Certificate[] getCertificateChain(String aliasPrefix)
            throws java.io.IOException, java.security.KeyStoreException,
            java.security.NoSuchAlgorithmException, java.security.cert.CertificateException
    {
        X509Certificate returnChain[] = null;

        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        // discover how many certificates are in the chain
        int numInChain = 0;
        for (Enumeration e = keyStore.aliases(); e.hasMoreElements(); ) {
            String aliasFound = (String) e.nextElement();
            if (aliasFound.startsWith(aliasPrefix)) {
                int numAlias = Integer.parseInt(aliasFound.replace(aliasPrefix, ""));
                if (numAlias > numInChain)
                    numInChain = numAlias;
            }
        }
        if (numInChain > 0) {
            returnChain = new X509Certificate[numInChain];
            for (int i=0; i<numInChain; i++)
                returnChain[i] = (X509Certificate) keyStore.getCertificate(aliasPrefix + (i+1));
        }

        return returnChain;
    }

    /**
     * This will install or replace the existing certificate chain in the AndroidKeyStore.
     *       The chain will be stored as "alias#".
     * @param aliasPrefix the alias prefix to use, will be appended with position number,
     *                    where position 0 in the array will be 1.
     */
    public static void clearCertificateChain(String aliasPrefix)
            throws java.io.IOException, java.security.KeyStoreException,
            java.security.NoSuchAlgorithmException, java.security.cert.CertificateException

    {
        KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
        keyStore.load(null);
        // delete any existing ones
        for (int i=1; i<=MAX_CHAIN; i++) {
            keyStore.deleteEntry(aliasPrefix + i);
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
