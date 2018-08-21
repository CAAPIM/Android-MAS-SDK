/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.util;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProtection;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import static android.security.keystore.KeyProperties.BLOCK_MODE_CBC;
import static android.security.keystore.KeyProperties.BLOCK_MODE_CTR;
import static android.security.keystore.KeyProperties.BLOCK_MODE_GCM;
import static android.security.keystore.KeyProperties.ENCRYPTION_PADDING_NONE;
import static android.security.keystore.KeyProperties.PURPOSE_DECRYPT;
import static android.security.keystore.KeyProperties.PURPOSE_ENCRYPT;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;


/**
 * This interface is used to generate a SecretKey for encryption purposes.
 * For Android.M+, the key will be created in the AndroidKeyStore
 * and will remain there.
 * For Pre-Android.M, the key will be created and returned, and
 * a KeyStorageProvider must be used to protect the key.  If
 * the user upgrades to Android.M+, the key will be moved
 * to the AndroidKeyStore.
 */
public class KeyUtilsSymmetric {

    // the following parameters apply to all key generation operations
    private static final String DEFAULT_ALGORITHM = "AES";
    private static final int DEFAULT_KEY_LENGTH = 256;

    // the following apply to keys created or stored in Android.M+
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";


    /**
     * Generate a symmetric key, allows for full selection of key properties
     * <p>
     * For M+:
     * “The Android Keystore system lets you store cryptographic keys in a
     * container to make it more difficult to extract from the device.
     * Once keys are in the keystore, they can be used for cryptographic
     * operations with the key material remaining non-exportable”.
     * This means that the key can't be extracted from the keystore, and
     * note that keys can only be shared between applications with
     * the same signing key and shared user id.
     * <p>
     * The key protection parameters for M+ AndroidKeyStore AES keys
     * userAuthenticationRequired,
     * userAuthenticationValidityDurationSeconds,
     * and nougatInvalidatedByBiometricEnrollment are linked,
     * below are the combinations:
     * <p>
     * 1) Android M+: userAuthenticationRequired(false) – the keys
     * are still protected from export but can be used whenever needed.
     * 2) Android M+: userAuthenticationRequired(true) and
     * userAuthenticationValidityDurationSeconds( > 0 ) –
     * the key can only be used if the pin has been entered within the given
     * number of seconds.  On some devices, when a fingerprint is added
     * the key is invalidated.  The # seconds can be set absurdly high,
     * for example 100,000 if it's ok for more than a day to pass without
     * an unlock.
     * 3) Android M+: userAuthenticationRequired(true) and
     * userAuthenticationValidityDurationSeconds(zero or neg) –
     * works only with fingerprint+pin/swipe/pattern, and requires that
     * the fingerprint is entered every time a key is used.  This requires
     * an added layer to prompt the user to swipe their fingerprint.
     * 4) Android N+: nougatInvalidatedByBiometricEnrollment(true/false)
     * changes the behavior in #2 – either the key will or won’t be
     * invalided when the user adds an or another fingerprint.
     *
     * @param alias                             the alias to use if generated in AndroidKeyStore
     * @param algorithm                         AES or other Symmetric Key algorithm
     * @param keyLength                         default is 256
     * @param inMemory                          for Android.M+ the key will be created outside the AndroidKeyStore and then stored
     *                                          inside.  The in-memory copy can be used without user authentication until the
     *                                          app is closed, dereferenced, or variable is destroyed.  This is useful
     *                                          for MASSessionLock and Unlock, where screen unlock is only needed for unlock.
     * @param userAuthenticationRequired        for Android.M+ require screen lock.  Note, if inMemory is true,
     *                                          this applies only to versions extracted from the AndroidKeyStore.
     * @param userAuthenticationValiditySeconds sets the duration for which this key is authorized
     *                                          to be used after the user is successfully authenticated.
     * @param invalidatedByBiometricEnrollment  true/false for Android-N+:
     *                                          if setUserAuthenticationRequired true, some Android M devices may disable
     *                                          a key if a fingerprint is added.  Setting this value to true ensures
     *                                          the key is usable even if a fingerprint is added.
     * @return secret key
     */
    public static SecretKey generateKey(String alias, String algorithm, int keyLength,
                                        boolean inMemory, boolean userAuthenticationRequired,
                                        int userAuthenticationValiditySeconds,
                                        boolean invalidatedByBiometricEnrollment) {
        SecretKey returnKey = null;

        if ((algorithm == null) || (algorithm.trim().length() == 0)) {
            if (DEBUG)
                Log.d(TAG, "Algorithm (" + algorithm + ") is either null or zero length, assigning default: " + DEFAULT_ALGORITHM);
            algorithm = DEFAULT_ALGORITHM;
        }

        if (keyLength < DEFAULT_KEY_LENGTH) {
            if (DEBUG)
                Log.d(TAG, "key length (" + keyLength + ") is less than zero, assigning default: " + DEFAULT_KEY_LENGTH);
            keyLength = DEFAULT_KEY_LENGTH;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (inMemory) {
                // generate the key outside AndroidKeyStore then store
                returnKey = generateKeyInMemory(algorithm, keyLength);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    storeKeyAndroidN(alias, returnKey,
                            userAuthenticationRequired, userAuthenticationValiditySeconds,
                            invalidatedByBiometricEnrollment);
                } else {
                    storeKeyAndroidM(alias, returnKey,
                            userAuthenticationRequired, userAuthenticationValiditySeconds);
                }
            } else {
                // generate the key inside the keystore
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    returnKey = generateKeyInAndroidKeyStoreAndroidN(alias, algorithm, keyLength,
                            userAuthenticationRequired, userAuthenticationValiditySeconds,
                            invalidatedByBiometricEnrollment);
                } else {
                    returnKey = generateKeyInAndroidKeyStoreAndroidM(alias, algorithm, keyLength,
                            userAuthenticationRequired, userAuthenticationValiditySeconds);
                }
            }
        } else {
            returnKey = generateKeyInMemory(algorithm, keyLength);
        }
        return returnKey;
    }


    /**
     * Return a secretKey to be used for encryption.
     * This key will be generated in memory and may
     * be vulnerable to discovery through memory scans.
     * This is useful for Pre-M devices, where AndroidKeyStore
     * is not supported and the key must be stored elsewhere.
     * This is also useful for session lock: the key can
     * be used right away without requiring authentication,
     * but stored protected in the AndroidKeyStore.
     *
     * @param algorithm AES or other Symmetric Key algorithm
     * @param keyLength default is 256
     * @return secret key
     */
    private static SecretKey generateKeyInMemory(String algorithm, int keyLength) {
        try {
            KeyGenerator kg = KeyGenerator.getInstance(algorithm);
            kg.init(keyLength);
            return kg.generateKey();
        } catch (Exception x) {
            if (DEBUG) Log.e(TAG, "Error generateKeyInMemory", x);
            throw new RuntimeException("Error generateKeyInMemory", x);
        }
    }


    /**
     * For Android.M+ only, Return a secretKey to be used for encryption.
     *
     * @param alias                             the alias to use if generated in AndroidKeyStore
     * @param algorithm                         AES or other Symmetric Key algorithm
     * @param keyLength                         default is 256
     * @param userAuthenticationRequired        for Android.M+ require screen lock.  Note, if inMemory is true,
     *                                          this applies only to versions extracted from the AndroidKeyStore.
     * @param userAuthenticationValiditySeconds sets the duration for which this key is authorized
     *                                          to be used after the user is successfully authenticated.
     * @return secret key
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static SecretKey generateKeyInAndroidKeyStoreAndroidM(
            String alias, String algorithm, int keyLength,
            boolean userAuthenticationRequired,
            int userAuthenticationValiditySeconds) {

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    algorithm, ANDROID_KEY_STORE);
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(alias, PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
                    .setKeySize(keyLength < DEFAULT_KEY_LENGTH ? DEFAULT_KEY_LENGTH : keyLength)
                    .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_GCM)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(userAuthenticationRequired);

            if (userAuthenticationRequired && userAuthenticationValiditySeconds > 0)
                builder.setUserAuthenticationValidityDurationSeconds(userAuthenticationValiditySeconds);

            KeyGenParameterSpec keyGenSpec = builder.build();
            keyGenerator.init(keyGenSpec);

            return keyGenerator.generateKey();

        } catch (Exception x) {
            if (DEBUG) Log.e(TAG, "Error generateKeyInAndroidKeyStore", x);
            throw new RuntimeException("Error generateKeyInAndroidKeyStore", x);
        }
    }


    /**
     * For Android.N+ only, Return a secretKey to be used for encryption.
     *
     * @param alias                             the alias to use if generated in AndroidKeyStore
     * @param algorithm                         AES or other Symmetric Key algorithm
     * @param keyLength                         default is 256
     * @param userAuthenticationRequired        for Android.M+ require screen lock.  Note, if inMemory is true,
     *                                          this applies only to versions extracted from the AndroidKeyStore.
     * @param userAuthenticationValiditySeconds sets the duration for which this key is authorized
     *                                          to be used after the user is successfully authenticated.
     * @param invalidatedByBiometricEnrollment  true/false for Android-N+:
     *                                          if setUserAuthenticationRequired true, some Android M devices may disable
     *                                          a key if a fingerprint is added.  Setting this value to true ensures
     *                                          the key is usable even if a fingerprint is added.
     * @return secret key
     */
    @TargetApi(Build.VERSION_CODES.N)
    private static SecretKey generateKeyInAndroidKeyStoreAndroidN(String alias, String algorithm, int keyLength,
                                                                  boolean userAuthenticationRequired,
                                                                  int userAuthenticationValiditySeconds,
                                                                  boolean invalidatedByBiometricEnrollment) {

        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(
                    algorithm, ANDROID_KEY_STORE);
            KeyGenParameterSpec.Builder builder = new KeyGenParameterSpec.Builder(alias, PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
                    .setKeySize(keyLength < DEFAULT_KEY_LENGTH ? DEFAULT_KEY_LENGTH : keyLength)
                    .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_GCM)
                    .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                    .setRandomizedEncryptionRequired(true)
                    .setUserAuthenticationRequired(userAuthenticationRequired)
                    .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);

            if (userAuthenticationRequired && userAuthenticationValiditySeconds > 0)
                builder.setUserAuthenticationValidityDurationSeconds(userAuthenticationValiditySeconds);

            KeyGenParameterSpec keyGenSpec = builder.build();
            keyGenerator.init(keyGenSpec);

            SecretKey key = keyGenerator.generateKey();
            return key;

        } catch (Exception x) {
            if (DEBUG) Log.e(TAG, "Error generateKeyInAndroidKeyStore", x);
            throw new RuntimeException("Error generateKeyInAndroidKeyStore", x);
        }
    }


    /**
     * Retrieve the key from the AndroidKeyStore
     *
     * @param alias the alias to store the key as
     * @return for Android Pre-M, this will return null
     * otherwise it will try to find the key in the AndroidKeyStore
     */
    public static SecretKey retrieveKey(String alias) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // get the key from the AndroidKeyStore
            return retrieveKeyAndroidM(alias);
        }

        // if Android Pre-M, the key will not be in the AndroidKeyStore
        return null;
    }


    /**
     * Retrieve the key from the AndroidKeyStore
     *
     * @param alias the alias to use when storing the key
     * @return the SecretKey, or null if not present
     */
    @TargetApi(Build.VERSION_CODES.M)
    private static SecretKey retrieveKeyAndroidM(String alias) {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error while instantiating Android KeyStore instance", e);
            throw new RuntimeException("Error while instantiating Android KeyStore instance", e);
        }
        try {
            ks.load(null);
        } catch (IOException | NoSuchAlgorithmException | java.security.cert.CertificateException e) {
            if (DEBUG) Log.e(TAG, "Error while loading Android KeyStore instance", e);
            throw new RuntimeException("Error while loading Android KeyStore instance", e);
        }
        SecretKey sk;
        try {
            KeyStore.SecretKeyEntry entry = (KeyStore.SecretKeyEntry) ks.getEntry(alias, null);
            if (entry == null)
                return null;
            sk = entry.getSecretKey();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | NullPointerException e) {
            if (DEBUG) Log.e(TAG, "Error while getting entry from Android KeyStore", e);
            throw new RuntimeException("Error while getting entry from Android KeyStore", e);
        }
        return sk;
    }

    /**
     * Stores a SecretKey in the AndroidKeyStore.
     * This is only useful when the phone is upgraded
     * to Android.M, where an existing key was stored
     * outside of the AndroidKeyStore.
     *
     * @param alias                             the alias to use if generated in AndroidKeyStore
     * @param secretKey                         the key to store
     * @param userAuthenticationRequired        for Android.M+ require screen lock.  Note, if inMemory is true,
     *                                          this applies only to versions extracted from the AndroidKeyStore.
     * @param userAuthenticationValiditySeconds sets the duration for which this key is authorized
     *                                          to be used after the user is successfully authenticated.
     */
    @TargetApi(Build.VERSION_CODES.M)
    public static boolean storeKeyAndroidM(String alias, SecretKey secretKey,
                                           boolean userAuthenticationRequired,
                                           int userAuthenticationValiditySeconds) {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
        } catch (KeyStoreException | java.security.cert.CertificateException | NoSuchAlgorithmException | IOException e) {
            if (DEBUG) Log.e(TAG, "Error instantiating Android keyStore");
            throw new RuntimeException("Error instantiating Android keyStore", e);
        }

        KeyProtection.Builder builder = new KeyProtection.Builder(PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
                .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_GCM)
                .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setUserAuthenticationRequired(userAuthenticationRequired);

        if (userAuthenticationRequired && userAuthenticationValiditySeconds > 0)
            builder.setUserAuthenticationValidityDurationSeconds(userAuthenticationValiditySeconds);

        KeyProtection kp = builder.build();

        try {
            ks.setEntry(alias, new KeyStore.SecretKeyEntry(secretKey), kp);
            return true;
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error setting entry into Android keyStore");
            throw new RuntimeException("Error setting entry into Android keyStore", e);
        }
    }


    /**
     * Stores a SecretKey in the AndroidKeyStore.
     * This is only useful when the phone is upgraded
     * to Android.M, where an existing key was stored
     * outside of the AndroidKeyStore.
     *
     * @param alias                             the alias to use if generated in AndroidKeyStore
     * @param secretKey                         the symmetric key to store
     * @param userAuthenticationRequired        for Android.M+ require screen lock.  Note, if inMemory is true,
     *                                          this applies only to versions extracted from the AndroidKeyStore.
     * @param userAuthenticationValiditySeconds sets the duration for which this key is authorized
     *                                          to be used after the user is successfully authenticated.
     * @param invalidatedByBiometricEnrollment  true/false for Android-N+:
     *                                          if setUserAuthenticationRequired true, some Android M devices may disable
     *                                          a key if a fingerprint is added.  Setting this value to true ensures
     *                                          the key is usable even if a fingerprint is added.
     */
    @RequiresApi(Build.VERSION_CODES.N)
    public static boolean storeKeyAndroidN(
            String alias, SecretKey secretKey,
            boolean userAuthenticationRequired,
            int userAuthenticationValiditySeconds,
            boolean invalidatedByBiometricEnrollment) {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(ANDROID_KEY_STORE);
            ks.load(null);
        } catch (KeyStoreException | java.security.cert.CertificateException | NoSuchAlgorithmException | IOException e) {
            if (DEBUG) Log.e(TAG, "Error instantiating Android keyStore");
            throw new RuntimeException("Error instantiating Android keyStore", e);
        }

        KeyProtection.Builder builder = new KeyProtection.Builder(PURPOSE_ENCRYPT | PURPOSE_DECRYPT)
                .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_GCM)
                .setEncryptionPaddings(ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .setUserAuthenticationRequired(userAuthenticationRequired)
                .setInvalidatedByBiometricEnrollment(invalidatedByBiometricEnrollment);

        if (userAuthenticationRequired && userAuthenticationValiditySeconds > 0)
            builder.setUserAuthenticationValidityDurationSeconds(userAuthenticationValiditySeconds);

        KeyProtection kp = builder.build();

        try {
            ks.setEntry(alias, new KeyStore.SecretKeyEntry(secretKey), kp);
            return true;
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error setting entry into Android keyStore");
            throw new RuntimeException("Error setting entry into Android keyStore", e);
        }
    }


    /**
     * This method deletes the given key from AndroidKeyStore
     *
     * @param alias the alias of the key to delete
     */
    public static void deleteKey(String alias) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // remove it from AndroidKeyStore
            deleteKeyAndroidM(alias);
        }
    }


    /**
     * This method deletes the given key from AndroidKeyStore
     *
     * @param alias the alias of the key to delete
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private static void deleteKeyAndroidM(String alias) {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error instantiating Android keyStore");
            throw new RuntimeException("Error instantiating Android keyStore", e);
        }
        try {
            ks.load(null);
        } catch (IOException | NoSuchAlgorithmException | java.security.cert.CertificateException e) {
            if (DEBUG) Log.e(TAG, "Error loading Android keyStore");
            throw new RuntimeException("Error loading Android keyStore", e);
        }
        try {
            ks.deleteEntry(alias);
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error deleting Android keyStore");
            throw new RuntimeException("Error deleting Android keyStore", e);
        }
    }

    // Encryption / decryption defaults
    private static final String AES_GCM_NO_PADDING = "AES/GCM/NoPadding";
    private static final String HMAC_SHA256 = "HmacSHA256";
    private static final int IV_LENGTH = 12;

    /**
     * Encrypts the given data.
     *
     * @param data      the data to encrypt
     * @param secretKey the symmetric key for encryption
     * @param key       Key to use to generate a secret key for MAC operation, can be key alias
     * @return encrypted data as byte[]
     */
    public static byte[] encrypt(byte[] data, SecretKey secretKey, String key) {
        if (data == null) {
            return null;
        }

        byte[] encryptedData;
        try {
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

            byte[] iv;
            AlgorithmParameterSpec ivParams;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                iv = cipher.getIV();
            } else {
                iv = new byte[IV_LENGTH];
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(iv);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ivParams = new GCMParameterSpec(128, iv);
                } else {
                    /*
                     * GCMParameterSpec does not work in Android 19
                     */
                    ivParams = new IvParameterSpec(iv);
                }

                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);
            }

            encryptedData = cipher.doFinal(data);
            byte[] mac = computeMac(key, encryptedData);
            encryptedData = concatArrays(mac, iv, encryptedData);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "inside exception of encrypt function: ", e);
            // if auth error, we should delete the invalid key
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                checkDeleteKeys(key, e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return encryptedData;
    }

    /**
     * Decrypt the given data.
     *
     * @param encryptedData data to be decrypted
     * @param secretKey     the symmetric key for decryption
     * @param key           Key to use to generate a secret key for MAC operation, can be key alias
     * @return byte[] of decrypted data
     */
    public static byte[] decrypt(byte[] encryptedData, SecretKey secretKey, String key) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            if (DEBUG) Log.e(TAG, "Error while getting an cipher instance", e);
            throw new RuntimeException("Error while getting an cipher instance", e);
        }

        int ivlength = IV_LENGTH;
        int macLength;
        try {
            macLength = Mac.getInstance(HMAC_SHA256).getMacLength();
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) Log.e(TAG, "Error while instantiating MAC", e);
            throw new RuntimeException("Error while instantiating MAC", e);
        }
        int encryptedDataLength = encryptedData.length - ivlength - macLength;
        byte[] macFromMessage = getArraySubset(encryptedData, 0, macLength);

        byte[] iv = getArraySubset(encryptedData, macLength, ivlength);
        encryptedData = getArraySubset(encryptedData, macLength + ivlength, encryptedDataLength);
        byte[] mac = computeMac(key, encryptedData);

        if (!Arrays.equals(mac, macFromMessage)) {
            if (DEBUG) Log.e(TAG, "MAC signature could not be verified");
            throw new RuntimeException("MAC signature could not be verified");
        }

        AlgorithmParameterSpec ivParams;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ivParams = new GCMParameterSpec(128, iv);
        } else {
            ivParams = new IvParameterSpec(iv);
        }

        try {
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                checkDeleteKeys(key, e);
            if (DEBUG) Log.i(TAG, "Error while decrypting an cipher instance", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    /**
     * Computes the mac signature for the encrypted array
     *
     * @param key        Key to use to generate a secret key for MAC operation
     * @param cipherText the data for which the signature has to be calculated
     */
    private static byte[] computeMac(String key, byte[] cipherText) {
        Mac hm;
        SecretKey secretKey = null;
        try {
            hm = Mac.getInstance(HMAC_SHA256);
            secretKey = new SecretKeySpec(key.getBytes("UTF-8"), HMAC_SHA256);
            hm.init(secretKey);
            return hm.doFinal(cipherText);
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            if (DEBUG) Log.e(TAG, "Error while calculating signature", e);
            throw new RuntimeException("Error while calculating signature", e);
        } finally {
            destroyKey(secretKey);
        }
    }

    /**
     * Combine the mac,iv and encrypted data arrays into one array
     *
     * @param mac
     * @param iv
     * @param cipherText
     * @return Combined mac, iv and encrypted data arrays.
     */
    private static byte[] concatArrays(byte[] mac, byte[] iv, byte[] cipherText) {
        int macLength = mac.length;
        int ivLength = iv.length;
        int cipherTextLength = cipherText.length;
        int totalLength = macLength + ivLength + cipherTextLength;
        byte[] result = new byte[totalLength];
        System.arraycopy(mac, 0, result, 0, macLength);
        System.arraycopy(iv, 0, result, macLength, ivLength);
        System.arraycopy(cipherText, 0, result, macLength + ivLength, cipherTextLength);
        return result;
    }

    private static byte[] getArraySubset(byte[] array, int start, int length) {
        byte[] result = new byte[length];
        System.arraycopy(array, start, result, 0, length);
        return result;
    }

    /**
     * Destroys the ephemeral key, in this case the Secret key generated for MAC, if the key implements Destroyable
     *
     * @param key
     */
    private static void destroyKey(SecretKey key) {
        if (key instanceof Destroyable) {
            try {
                (key).destroy();
            } catch (DestroyFailedException e) {
                if (DEBUG) Log.w(TAG, "Could not destroy key");
            }
        }
    }

    /**
     * If there is an exception with user not authenticated, delete the key
     *
     * @param alias the name of the key
     * @param e     the exception
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private static void checkDeleteKeys(String alias, Exception e) {
        if (!(e instanceof android.security.keystore.UserNotAuthenticatedException)) {
            deleteKey(alias);
            if (DEBUG) Log.e(TAG, "deleted key " + alias + " since User not authenticated");
        }
    }

    /**
     * Constructor, not necessary
     */
    protected KeyUtilsSymmetric() {
    }


}
