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
import android.util.Log;

import java.math.BigInteger;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.spec.RSAKeyGenParameterSpec;
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
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_PKCS7;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_RSA_OAEP;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1;
import static android.security.keystore.KeyProperties.SIGNATURE_PADDING_RSA_PKCS1;
import static android.security.keystore.KeyProperties.SIGNATURE_PADDING_RSA_PSS;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

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
     *  For Pre-M, lollipop_setEncryptionRequired requires that the user sets
     *      a screen lock and that the keys will be encrypted at rest.
     *
     *  For M+:
     *      “The Android Keystore system lets you store cryptographic keys in a
     *           container to make it more difficult to extract from the device.
     *           Once keys are in the keystore, they can be used for cryptographic
     *           operations with the key material remaining non-exportable”.
     *           This means that the key can't be extracted from the keystore, and 
     *           note that keys can only be shared between applications with
     *           the same signing key and shared user id.
     *      Therefore, it is not necessary to protect private keys stored
     *           in the AndroidKeyStore with a screen lock in the same way
     *           encryption was required for Pre-M.
     *
     *      The key protection parameters for M+ AndroidKeyStore RSA keys
     *           marshmallow_setUserAuthenticationValidityDurationSeconds,
     *           and nougat_setInvalidatedByBiometricEnrollment are linked,
     *           below are the combinations:
     *           
     *       1) Android M+: marshmallow_setUserAuthenticationRequired(false) – the keys
     *           are still protected from export but can be used whenever needed.
     *       2) Android M+: marshmallow_setUserAuthenticationRequired(true) and
     *           marshmallow_setUserAuthenticationValidityDurationSeconds( > 0 ) –
     *           the key can only be used if the pin has been entered within the given
     *           number of seconds.  On some devices, when a fingerprint is added
     *           the key is invalidated.
     *       3) Android M+: marshmallow_setUserAuthenticationRequired(true) and
     *           marshmallow_setUserAuthenticationValidityDurationSeconds(zero) –
     *           works only with fingerprint+pin/swipe/pattern, and requires that
     *           the fingerprint is entered every time a key is used.  This requires
     *           an added layer to prompt the user to swipe their fingerprint.
     *       4) Android N+: nougat_setInvalidatedByBiometricEnrollment(true/false)
     *           changes the behavior in #2 – either the key will or won’t be
     *           invalided when the user adds an or another fingerprint.
     *
     * @param context needed for generating key pre-M
     * @param keysize the key size in bits, eg 2048.
     * @param alias the keystore alias to use
     * @oaram lollipop_setEncryptionRequired true/false for pre-Android-M:
     *           requires that the user sets a screen lock and that the keys will be encrypted at rest
     * @param marshmallow_userAuthenticationRequired true/false for Android-M+:
     *           requires a lock screen in order to use the key.  If the validity duration
     *           is equal to zero, a fingerprint validation is required for every use of the key.
     * @param marshmallow_userAuthenticationValidityDurationSeconds # secs for Android-M+:
     *           if user authentication is required, this specifies the number of seconds after
     *           unlocking the screen where key is still usable.  If this value is zero, a
     *           fingerprint is required for every use.
     * @param nougat_invalidatedByBiometricEnrollment true/false for Android-N+:
     *           if setUserAuthenticationRequired true, some Android M devices may disable
     *           a key if a fingerprint is added.  Setting this value to true ensures
     *           the key is usabl even if a fingerprint is added.
     * @return a new RSA PrivateKey.
     * @throws RuntimeException if an RSA key pair of the requested size cannot be generated
     */
    public static PrivateKey generateRsaPrivateKey(Context context, int keysize,
                     String alias, String dn, boolean lollipop_encryptionRequired,
                     boolean marshmallow_userAuthenticationRequired,
                     int marshmallow_userAuthenticationValidityDurationSeconds,
                     boolean nougat_invalidatedByBiometricEnrollment)
            throws java.security.InvalidAlgorithmParameterException, java.io.IOException,
            java.security.KeyStoreException, java.security.NoSuchAlgorithmException,
            java.security.NoSuchProviderException, java.security.cert.CertificateException,
            java.security.UnrecoverableKeyException
    {
        // use a minimum of 2048
        if (keysize < 2048)
            keysize = 2048;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // use KeyGenParameterSpec.Builder, new in Marshmallow
            //   and include nougat_invalidatedByBiometricEnrollment
            return generateRsaPrivateKey_AndroidN(keysize, alias, dn,
                      marshmallow_userAuthenticationRequired,
                      marshmallow_userAuthenticationValidityDurationSeconds,
                      nougat_invalidatedByBiometricEnrollment);
        }

        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // use KeyGenParameterSpec.Builder, new in Marshmallow
            return generateRsaPrivateKey_AndroidM(keysize, alias, dn,
                      marshmallow_userAuthenticationRequired,
                      marshmallow_userAuthenticationValidityDurationSeconds);
        }

        else {
            return generateRsaPrivateKey_AndroidL(context, keysize,
                      alias, dn, lollipop_encryptionRequired);
        }
    }


    /**
     * Android Lollipop and prior:
     * Generate a new RSA keypair with the specified number of key bits.
     *     This will always create the key pair inside the AndroidKeyStore,
     *        ensuring it is always protected.
     *
     * @param context needed for generating key Android.M+
     * @param keysize the key size in bits, eg 2048.
     * @param alias the keystore alias to use
     * @oaram lollipop_setEncryptionRequired true/false for pre-Android-M:
     *           requires that the user sets a screen lock and that the keys will be encrypted at rest
     * @param marshmallow_setUserAuthenticationRequired true/false for Android-M+:
     *           requires a lock screen in order to use the key.  If the validity duration
     *           is equal to zero, a fingerprint validation is required for every use of the key.
     * @param marshmallow_setUserAuthenticationValidityDurationSeconds # secs for Android-M+:
     *           if user authentication is required, this specifies the number of seconds after
     *           unlocking the screen where key is still usable.  If this value is zero, a
     *           fingerprint is required for every use.
     * @param nougat_setInvalidatedByBiometricEnrollment true/false for Android-N+:
     *           if setUserAuthenticationRequired true, some Android M devices may disable
     *           a key if a fingerprint is added.  Setting this value to true ensures
     *           the key is usabl even if a fingerprint is added.
     * @return a new RSA PrivateKey.
     * @throws RuntimeException if an RSA key pair of the requested size cannot be generated
     */
    protected static PrivateKey generateRsaPrivateKey_AndroidL(Context context, int keysize,
                     String alias, String dn, boolean setEncryptionRequired)
            throws java.security.InvalidAlgorithmParameterException, java.io.IOException,
            java.security.KeyStoreException, java.security.NoSuchAlgorithmException,
            java.security.NoSuchProviderException, java.security.cert.CertificateException,
            java.security.UnrecoverableKeyException
    {
        // For Android Pre-M (Lollipop and prior)
        // use the KeyPairGeneratorSpec.Builder, deprecated as of Marshmallow
        //    generates the key inside the AndroidKeyStore
        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keysize, RSAKeyGenParameterSpec.F4);
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA", ANDROID_KEY_STORE);

        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();

        if (setEncryptionRequired) {
            kpg.initialize(new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setAlgorithmParameterSpec(spec)
                    .setEncryptionRequired()
                    .setStartDate(now).setEndDate(end)
                    .setSerialNumber(BigInteger.valueOf(1))
                    .setSubject(new X500Principal(dn))
                    .build());
        } else {
            kpg.initialize(new KeyPairGeneratorSpec.Builder(context)
                    .setAlias(alias)
                    .setAlgorithmParameterSpec(spec)
                    .setStartDate(now).setEndDate(end)
                    .setSerialNumber(BigInteger.valueOf(1))
                    .setSubject(new X500Principal(dn))
                    .build());
        }
        return kpg.generateKeyPair().getPrivate();
    }

    /**
     * Android Marshmallow:
     * Generate a new RSA keypair with the specified number of key bits.
     *     This will always create the key pair inside the AndroidKeyStore
     *
     * @param keysize the key size in bits, eg 2048.
     * @param alias the alias against which to store the key against
     * @param userAuthenticationRequired true/false for Android-M+:
     *           requires a lock screen in order to use the key.  If the validity duration
     *           is equal to zero, a fingerprint validation is required for every use of the key.
     * @param userAuthenticationValidityDurationSeconds # secs for Android-M+:
     *           if user authentication is required, this specifies the number of seconds after
     *           unlocking the screen where key is still usable.  If this value is zero, a
     *           fingerprint is required for every use.
     * @return a new RSA keypair, created in and protected by the AndroidKeyStore, with an
     *           unusable self-signed certificate
     */
    @TargetApi(Build.VERSION_CODES.M)
    protected static PrivateKey generateRsaPrivateKey_AndroidM(int keysize,
                        String alias, String dn, boolean userAuthenticationRequired,
                        int userAuthenticationValidityDurationSeconds)
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
                new KeyGenParameterSpec.Builder(alias,
                       KeyProperties.PURPOSE_ENCRYPT + KeyProperties.PURPOSE_DECRYPT
                                  + KeyProperties.PURPOSE_SIGN + KeyProperties.PURPOSE_VERIFY)
                        .setKeySize(keysize)
                        .setCertificateNotBefore(now).setCertificateNotAfter(end)
                        .setCertificateSubject(new X500Principal("CN=msso"))
                        .setCertificateSerialNumber(BigInteger.valueOf(1))
                        .setUserAuthenticationRequired(userAuthenticationRequired)
                        .setUserAuthenticationValidityDurationSeconds(userAuthenticationValidityDurationSeconds)
                        // In HttpUrlConnection, com.android.org.conscrypt.CryptoUpcalls.rawSignDigestWithPrivateKey
                        //   requires "NONEwithRSA", so we need to include DIGEST_NONE
                        //   therefore we can only setRandomizedEncruptionRequired to false
                        //   and must include DIGEST_NONE in allowed digests
                        .setRandomizedEncryptionRequired(false)
                        .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_ECB, BLOCK_MODE_GCM)
                        .setDigests(DIGEST_NONE, DIGEST_MD5, DIGEST_SHA1, DIGEST_SHA256, DIGEST_SHA384, DIGEST_SHA512)
                        .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7, ENCRYPTION_PADDING_RSA_OAEP, ENCRYPTION_PADDING_RSA_PKCS1)
                        .setSignaturePaddings(SIGNATURE_PADDING_RSA_PSS, SIGNATURE_PADDING_RSA_PKCS1)
                        .build());
        return keyPairGenerator.generateKeyPair().getPrivate();
    }


    /**
     * Android Nougat and above::
     * Generate a new RSA keypair with the specified number of key bits.
     *     This will always create the key pair inside the AndroidKeyStore
     *
     * @param keysize the key size in bits, eg 2048.
     * @param alias the alias against which to store the key against
     * @param userAuthenticationRequired true/false for Android-M+:
     *           requires a lock screen in order to use the key.  If the validity duration
     *           is equal to zero, a fingerprint validation is required for every use of the key.
     * @param userAuthenticationValidityDurationSeconds # secs for Android-M+:
     *           if user authentication is required, this specifies the number of seconds after
     *           unlocking the screen where key is still usable.  If this value is zero, a
     *           fingerprint is required for every use.
     * @param invalidatedByBiometricEnrollment true/false for Android-N+:
     *           if setUserAuthenticationRequired true, some Android M devices may disable
     *           a key if a fingerprint is added.  Setting this value to true ensures
     *           the key is usabl even if a fingerprint is added.
     * @return a new RSA keypair, created in and protected by the AndroidKeyStore, with an
     *           unusable self-signed certificate
     */
    @TargetApi(Build.VERSION_CODES.N)
    protected static PrivateKey generateRsaPrivateKey_AndroidN(int keysize,
                        String alias, String dn, boolean userAuthenticationRequired,
                        int userAuthenticationValidityDurationSeconds,
                        boolean invalidatedByBiometricEnrollment)
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
                new KeyGenParameterSpec.Builder(alias,
                       KeyProperties.PURPOSE_ENCRYPT + KeyProperties.PURPOSE_DECRYPT
                                  + KeyProperties.PURPOSE_SIGN + KeyProperties.PURPOSE_VERIFY)
                        .setKeySize(keysize)
                        .setCertificateNotBefore(now).setCertificateNotAfter(end)
                        .setCertificateSubject(new X500Principal("CN=msso"))
                        .setCertificateSerialNumber(BigInteger.valueOf(1))
                        .setUserAuthenticationRequired(userAuthenticationRequired)
                        .setUserAuthenticationValidityDurationSeconds(userAuthenticationValidityDurationSeconds)
                        .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment)
                        // In HttpUrlConnection, com.android.org.conscrypt.CryptoUpcalls.rawSignDigestWithPrivateKey
                        //   requires "NONEwithRSA", so we need to include DIGEST_NONE
                        //   therefore we can only setRandomizedEncruptionRequired to false
                        //   and must include DIGEST_NONE in allowed digests
                        .setRandomizedEncryptionRequired(false)
                        .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_ECB, BLOCK_MODE_GCM)
                        .setDigests(DIGEST_NONE, DIGEST_MD5, DIGEST_SHA1, DIGEST_SHA256, DIGEST_SHA384, DIGEST_SHA512)
                        .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7, ENCRYPTION_PADDING_RSA_OAEP, ENCRYPTION_PADDING_RSA_PKCS1)
                        .setSignaturePaddings(SIGNATURE_PADDING_RSA_PSS, SIGNATURE_PADDING_RSA_PKCS1)
                        .build());
        return keyPairGenerator.generateKeyPair().getPrivate();
    }


    /**
     * Get the existing private key.
     *     Note: the initial self-signed public cert is typically not useful.
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
    {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
            keyStore.deleteEntry(alias);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable delete privage key: " + e.getMessage(), e);
        }
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
    {
        try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            // delete any existing ones
            for (int i = 1; i <= MAX_CHAIN; i++) {
                keyStore.deleteEntry(aliasPrefix + i);
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable to clear certificate chain: " + e.getMessage(), e);
        }
    }

    private KeyUtils() {
    }
}
