/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.content.Context;
import android.os.Build;
import androidx.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.util.KeyUtilsAsymmetric;
import com.ca.mas.core.util.KeyUtilsSymmetric;

import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public abstract class KeyStoreKeyStorageProvider implements KeyStorageProvider {

    private static final int VALIDITY_SECONDS = 7200;// 2 hours

    // For Android.M+, use the AndroidKeyStore
    // Otherwise, encrypt using RSA key with "RSA/ECB/PKCS1Padding"
    //    which actually doesn't implement ECB mode encryption.
    //    It should have been called "RSA/None/PKCS1Padding" as it can only be used to
    //    encrypt a single block of plaintext (The secret key)
    //    This may be naming mistake.

    private static final String ASYM_KEY_ALIAS = "ASYM_KEY";
    private static final String RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1PADDING";

    private Context context;

    /**
     * Default constructor.
     *
     * @param ctx context
     */
    public KeyStoreKeyStorageProvider(@NonNull Context ctx) {
        context = ctx.getApplicationContext();
    }

    /**
     * Retrieve the SecretKey from Storage
     *
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    @Override
    public SecretKey getKey(String alias, boolean userAuthenticationRequired) {
        // For Android.M+, if this key was created we'll find it here
        SecretKey secretKey = KeyUtilsSymmetric.retrieveKey(alias);

        if (secretKey == null) {

            // check if the key is present locally
            byte encryptedSecretKey[] = getEncryptedSecretKey(alias);
            if (encryptedSecretKey != null) {
                try {
                    secretKey = decryptSecretKey(encryptedSecretKey);
                } catch (Exception unableToDecrypt) {
                    if (DEBUG)
                        Log.e(TAG, "Error while decrypting SecretKey, deleting it", unableToDecrypt);

                    deleteSecretKeyLocally(alias);
                    encryptedSecretKey = null;
                }
            }

            // if still no key, generate one
            if (secretKey == null) {
                if (userAuthenticationRequired) {
                    secretKey = KeyUtilsSymmetric.generateKey(alias, "AES", 256, false, true, VALIDITY_SECONDS, false);
                } else {
                    secretKey = KeyUtilsSymmetric.generateKey(alias, "AES", 256, false, false, -1, false);
                }

                // if this is Pre- Android.M, we need to store it locally
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                    encryptedSecretKey = encryptSecretKey(secretKey);
                    storeSecretKeyLocally(alias, encryptedSecretKey);
                }
            } else {
                // if this is Android.M+, check if the operating system was upgraded
                //   and we can now store the SecretKey in the AndroidKeyStore
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    KeyUtilsSymmetric.storeKeyAndroidN(alias, secretKey,
                            false, -1, false);
                    deleteSecretKeyLocally(alias);
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    KeyUtilsSymmetric.storeKeyAndroidM(alias, secretKey,
                            false, -1);
                    deleteSecretKeyLocally(alias);
                }
            }
        }

        return secretKey;
    }


    /**
     * Remove the key
     *
     * @param alias the alias of the key to remove
     */
    @Override
    public boolean removeKey(String alias) {
        KeyUtilsSymmetric.deleteKey(alias);
        deleteSecretKeyLocally(alias);
        return true;
    }


    /**
     * @param alias              The alias to store the key against.
     * @param encryptedSecretKey The encrypted secret key to store.
     * @return                   Success/Fail
     */
    abstract boolean storeSecretKeyLocally(String alias, byte[] encryptedSecretKey);

    /**
     * @param alias The alias for the required secret key.
     * @return the encrypted bytes
     */
    abstract byte[] getEncryptedSecretKey(String alias);

    /**
     * Delete the secret key locally.
     *
     * @param alias Alias of the secret key
     * @return success / fail
     */
    abstract boolean deleteSecretKeyLocally(String alias);


    /**
     * This method encrypts a SecretKey using an RSA key.
     * This is intended for Pre-Android.M, where the
     * SecretKey cannot be stored in the AndroidKeyStore
     *
     * @param secretKey SecretKey to encrypt
     */
    private byte[] encryptSecretKey(SecretKey secretKey) {
        try {
            PublicKey publicKey = KeyUtilsAsymmetric.getRsaPublicKey(ASYM_KEY_ALIAS);
            if (publicKey == null) {

                KeyUtilsAsymmetric.generateRsaPrivateKey(
                        ASYM_KEY_ALIAS, String.format("CN=%s, OU=%s", ASYM_KEY_ALIAS, "com.ca"),
                        false, false, -1, false);
                publicKey = KeyUtilsAsymmetric.getRsaPublicKey(ASYM_KEY_ALIAS);
            }

            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(secretKey.getEncoded());

        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error while encrypting SecretKey", e);
            throw new RuntimeException("Error while encrypting SecretKey", e);
        }
    }

    /**
     * This method decrypts a SecretKey using an RSA key.
     * This is intended for Pre-Android.M, where the
     * SecretKey cannot be stored in the AndroidKeyStore
     *
     * @param encryptedSecretKey the encrypted bytes of the secret key
     */
    private SecretKey decryptSecretKey(byte encryptedSecretKey[]) {
        try {
            PrivateKey privateKey = KeyUtilsAsymmetric.getRsaPrivateKey(ASYM_KEY_ALIAS);

            Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedSecretkey = cipher.doFinal(encryptedSecretKey);
            return new SecretKeySpec(decryptedSecretkey, "AES");

        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error while decrypting SecretKey", e);
            throw new RuntimeException("Error while decrypting SecretKey", e);
        }


    }

    public Context getContext() {
        return context;
    }
}
