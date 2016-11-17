/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.security.*;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

public abstract class KeyStoreKeyStorageProvider implements KeyStorageProvider {

    //"RSA/ECB/PKCS1Padding" actually doesn't implement ECB mode encryption.
    // It should have been called "RSA/None/PKCS1Padding" as it can only be used to
    // encrypt a single block of plaintext (The secret key)
    // This may be naming mistake.

    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    protected static final String ASYM_KEY_ALIAS = "ASYM_KEY";
    public static final String RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1PADDING";
    private static final String AES = "AES";

    private Context context;

    public KeyStoreKeyStorageProvider(@NonNull Context ctx) {
        context = ctx.getApplicationContext();
    }

    abstract boolean storeSecretKeyLocally(String alias, byte[] encryptedSecretKey);

    abstract boolean containsSecretKeyLocally(String alias);

    abstract boolean deleteSecretKeyLocally(String alias);

    abstract byte[] getEncryptedSecretKey(String alias);

    /**
     * This method stores the SecretKey in safe location.
     * If it is Android M or higher , then in the Android KeyStore
     * else if Android 4.4 or higher, it encrypts the SecretKey with an asymmetric keypair and stores
     * the keypair in the Android KeyStore and the encrypted SecretKey in the local storage
     *
     * @param alias: the alias to store and retrieve the key
     * @param key:   SecretKey to store
     */
    @Override
    public void storeKey(String alias, SecretKey key) {
        /**
         * Since this is Android M or higher, we can directly store a symmetric Key into the Android KeyStore
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            storeKeyToKeystore(alias, key);
        } else {

            java.security.KeyStore ks;
            try {
                ks = java.security.KeyStore.getInstance(ANDROID_KEY_STORE);

            } catch (KeyStoreException e) {
                if (DEBUG) Log.e(TAG, "Error while instantiating Android KeyStore instance", e);
                throw new RuntimeException("Error while instantiating Android KeyStore", e);
            }
            try {
                ks.load(null);
            } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                if (DEBUG) Log.e(TAG, "Error while loading Android KeyStore instance", e);
                throw new RuntimeException("Error while instantiating Android KeyStore", e);
            }
            java.security.KeyStore.Entry entry;
            try {
                entry = ks.getEntry(ASYM_KEY_ALIAS, null);
            } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
                if (DEBUG) Log.e(TAG, "Error while getting entry from Android KeyStore", e);
                throw new RuntimeException("Error while getting entry from Android KeyStore", e);
            }

            Calendar notAfter = Calendar.getInstance();
            notAfter.add(Calendar.YEAR, 1);

            if (entry == null) {
                KeyPairGenerator keyPairGenerator;
                try {
                    keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
                } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
                    if (DEBUG) Log.e(TAG, "Error while instantiating KeyPairGenerator", e);
                    throw new RuntimeException("Error while instantiating KeyPairGenerator", e);
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {

                    //Generating an asymmetric key
                    try {
                        keyPairGenerator.initialize(new KeyPairGeneratorSpec.Builder(context)
                                .setAlias(ASYM_KEY_ALIAS)
                                .setEncryptionRequired()
                                .setSubject(
                                        new X500Principal(String.format("CN=%s, OU=%s", ASYM_KEY_ALIAS, "com.ca")))
                                .setSerialNumber(BigInteger.ONE)
                                .setStartDate(new Date())
                                .setEndDate(notAfter.getTime())
                                .build());
                    } catch (InvalidAlgorithmParameterException | NullPointerException e) {
                        if (DEBUG) Log.e(TAG, "Error while instantiating KeyPairGenerator", e);
                        throw new RuntimeException("Error while instantiating KeyPairGenerator", e);
                    }
                }
                keyPairGenerator.generateKeyPair(); //This is important, don't remove

                try {
                    entry = ks.getEntry(ASYM_KEY_ALIAS, null);
                } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException e) {
                    if (DEBUG) Log.e(TAG, "Error while getting entry from Android KeyStore", e);
                    throw new RuntimeException("Error while getting entry from Android KeyStore", e);
                }
            }
            PublicKey publicKey = ((java.security.KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();

            try {
                byte[] encryptedSecretkey = encryptSecretKey(key, publicKey);
                storeSecretKeyLocally(alias, encryptedSecretkey);
            } catch (InvalidKeyException | BadPaddingException | IllegalBlockSizeException | NoSuchPaddingException | NoSuchAlgorithmException e) {
                if (DEBUG) Log.e(TAG, "Error while encrypting SecretKey", e);
                throw new RuntimeException("Error while encrypting SecretKey", e);
            }
        }
    }

    /**
     * This method retrieves the SecretKey from safe storage.
     *
     * @param alias : the alias against which the key is stored
     * @return SecretKey: key or null if there is some error.
     */
    @Override
    public SecretKey getKey(String alias) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !containsSecretKeyLocally(alias)) {
            java.security.KeyStore ks;
            try {
                ks = java.security.KeyStore.getInstance("AndroidKeyStore");
            } catch (KeyStoreException e) {
                if (DEBUG) Log.e(TAG, "Error while instantiating Android KeyStore instance", e);
                throw new RuntimeException("Error while instantiating Android KeyStore instance", e);
            }
            try {
                ks.load(null);
            } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                if (DEBUG) Log.e(TAG, "Error while loading Android KeyStore instance", e);
                throw new RuntimeException("Error while loading Android KeyStore instance", e);
            }
            SecretKey sk;
            try {
                java.security.KeyStore.SecretKeyEntry entry = (java.security.KeyStore.SecretKeyEntry) ks.getEntry(alias, null);
                sk = entry.getSecretKey();
            } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException | NullPointerException e) {
                if (DEBUG) Log.e(TAG, "Error while getting entry from Android KeyStore", e);
                throw new RuntimeException("Error while getting entry from Android KeyStore", e);
            }
            return sk;

        } else {
            /**
             * This is a special upgrade case where the user upgrades from NON-Android M to Android M or higher
             * In this case if the SecretKey is stored locally and the asymmetric Key is stored in the KeyStore,
             * then the SecretKey is retrieved from the local store and stored in the KeyStore.
             * Asymmetric Key is deleted from keystore.
             * SecretKey is deleted from local store.
             * Now the secretKey is only present in the Keystore.
             */
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && containsSecretKeyLocally(alias)) {

                //Get SecretKey from local store
                SecretKey key = getSecretKeyLocally(alias);
                //Delete Asymmetric Key from the KeyStore
                deleteKeyFromKeystore(ASYM_KEY_ALIAS);
                //Store the SecretKey in the KeyStore
                storeKey(alias, key);
                //Delete SecretKey from Local store
                deleteSecretKeyLocally(alias);
                //return the secretKey from the its new place, the KeyStore
                return getKey(alias);
            } else {
                return getSecretKeyLocally(alias);
            }
        }
    }

    @Override
    public boolean containsKey(String alias) {
        boolean hasSecureKey = containsSecretKeyLocally(alias);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (!hasSecureKey) {
                //This is not an upgrade from Android M- to M or M+. it is using Android M on first install.
                java.security.KeyStore ks;

                try {
                    ks = java.security.KeyStore.getInstance(ANDROID_KEY_STORE);
                } catch (KeyStoreException e) {
                    if (DEBUG) Log.e(TAG, "Error while instantiating Android KeyStore");
                    return false;
                }
                try {
                    ks.load(null);
                } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
                    if (DEBUG) Log.e(TAG, "Error while instantiating Android KeyStore");
                    return false;
                }
                try {
                    return ks.containsAlias(alias);
                } catch (KeyStoreException e) {
                    if (DEBUG) Log.e(TAG, "Error in  containsAlias function");
                    return false;
                }
            } else {
                //This is an upgrade from Android M- to M or M+
                return true;
            }
        } else {
            return hasSecureKey;
        }
    }

    /**
     * This method returns the Secretkey which is stored locally, in this case from the SharedPrefernces
     *
     * @param alias : the alias against which to find the key
     * @return: the Secretkey
     */
    protected SecretKey getSecretKeyLocally(String alias) {
        byte[] encryptedSecretKey = getEncryptedSecretKey(alias);
        KeyStore ks;

        try {
            ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error while instantiating Android KeyStore");
            throw new RuntimeException("Error while instantiating Android KeyStore", e);
        }

        try {
            ks.load(null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            if (DEBUG) Log.e(TAG, "Error while instantiating Android KeyStore");
            throw new RuntimeException("Error while instantiating Android KeyStore", e);
        }

        KeyStore.Entry entry;
        PrivateKey privateKey;

        try {
            entry = ks.getEntry(ASYM_KEY_ALIAS, null);
            privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
        } catch (NoSuchAlgorithmException | UnrecoverableEntryException | KeyStoreException | NullPointerException e) {
            if (DEBUG) Log.e(TAG, "Error while retrieving aysmmetric key from keystore", e);
            throw new RuntimeException("Error while retrieving aysmmetric key from keystore", e);
        }

        try {
            return decryptSecretKey(encryptedSecretKey, privateKey);
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
            if (DEBUG) Log.e(TAG, "Error while decrypting SecretKey", e);
            throw new RuntimeException("Error while  decrypting SecretKey", e);
        }
    }

    /**
     * This method stores a given SecretKey into the KeyStore
     *
     * @param alias: the alias against which to store the key against
     * @param key:   the SecretKey
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean storeKeyToKeystore(String alias, SecretKey key) {
        java.security.KeyStore ks;
        try {
            ks = java.security.KeyStore.getInstance("AndroidKeyStore");
            ks.load(null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            if (DEBUG) Log.e(TAG, "Error instantiating Android keyStore");
            throw new RuntimeException("Error instantiating Android keyStore", e);
        }

        KeyProtection kp = new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build();


        try {
            ks.setEntry(alias, new KeyStore.SecretKeyEntry(key), kp);
            return true;
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error setting entry into Android keyStore");
            throw new RuntimeException("Error setting entry into Android keyStore", e);
        }

    }

    /**
     * This method deletes the given key from Android KeyStore
     *
     * @param alias
     */
    private void deleteKeyFromKeystore(String alias) {
        KeyStore ks;
        try {
            ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error instantiating Android keyStore");
            throw new RuntimeException("Error instantiating Android keyStore", e);
        }
        try {
            ks.load(null);
        } catch (IOException | NoSuchAlgorithmException | CertificateException e) {
            if (DEBUG) Log.e(TAG, "Error loading Android keyStore");
            throw new RuntimeException("Error loading Android keyStore", e);
        }
        try {
            ks.deleteEntry(alias);
        } catch (KeyStoreException e) {
            if (DEBUG) Log.e(TAG, "Error deleting a key Android keyStore");
            throw new RuntimeException("Error deleting a key Android keyStore", e);
        }
    }

    /**
     * This method encrypts the SecretKey with asymmetric Key
     *
     * @param key:       the SecretKey to encrypt
     * @param publicKey: The public key part of the asymmetric Key
     * @return byte[]: the EncryptedKey
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    private byte[] encryptSecretKey(SecretKey key, PublicKey publicKey) throws InvalidKeyException, BadPaddingException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException {
        Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(key.getEncoded());
    }

    /**
     * This method decrypts the encrypted SecretKey
     *
     * @param encryptedSecretKey
     * @param privateKey:        the private Key part of the asymmetric Key
     * @return Secretkey
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     * @throws InvalidKeyException
     * @throws BadPaddingException
     * @throws IllegalBlockSizeException
     */
    protected SecretKey decryptSecretKey(byte[] encryptedSecretKey, PrivateKey privateKey) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedSecretkey = cipher.doFinal(encryptedSecretKey);
        return new SecretKeySpec(decryptedSecretkey, AES);
    }

    public Context getContext() {
        return context;
    }
}
