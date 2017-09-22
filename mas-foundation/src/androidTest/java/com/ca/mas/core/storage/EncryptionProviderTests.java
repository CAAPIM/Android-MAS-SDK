/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyProperties;
import android.support.test.runner.AndroidJUnit4;


import com.ca.mas.MASTestBase;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.util.Calendar;
import java.util.Date;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.x500.X500Principal;

import static junit.framework.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class EncryptionProviderTests extends MASTestBase {

    public static final String TESTDATA = "testdata";
    public static final String AES_CBC_PKCS5_PADDING = "AES/CBC/PKCS5Padding";
    public static final String RSA_ECB_PKCS1_PADDING = "RSA/ECB/PKCS1PADDING";
    public static final String AES = "AES";
    public static final String SECRET_KEY_ALIAS = "secret";
    public static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final int KEY_LENGTH = 256;

    @Test
    public void testEncryption() throws Exception {
        KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
        ks.load(null);
        SecretKey key = generateAesKey();
        /*
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ks.setEntry("secret", new KeyStore.SecretKeyEntry(key),
                    new KeyProtection.Builder(KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .build());
        } else {
        */
        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.YEAR, 1);

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            keyPairGenerator.initialize(new KeyPairGeneratorSpec.Builder(
                    getContext())
                    .setAlias(SECRET_KEY_ALIAS)
                    .setEncryptionRequired()
                    .setSubject(
                            new X500Principal(String.format("CN=%s, OU=%s", SECRET_KEY_ALIAS, "com.ca")))
                    .setSerialNumber(BigInteger.ONE)
                    .setStartDate(new Date())
                    .setEndDate(notAfter.getTime())
                    .build()
            );
        }
        keyPairGenerator.genKeyPair();

        //Encrypt data using secret key
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        byte[] iv = generateIv(cipher.getBlockSize());
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParams);
        byte[] encryptedData = cipher.doFinal(TESTDATA.getBytes());

        //Use the public key to encrypt the secret key, the encrypted secret key and iv will store in the file system
        byte[] encryptedSecretKey = encryptSecretKey(key);

        //Decrypt the data
        byte[] decryptedData = decrypt(encryptedData, encryptedSecretKey, iv);

        assertEquals(TESTDATA, new String(decryptedData));

        ks.deleteEntry(SECRET_KEY_ALIAS);
        //}

    }


    private byte[] encryptSecretKey(SecretKey secretKey) throws Exception {
        KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(SECRET_KEY_ALIAS, null);
        PublicKey publicKey = ((KeyStore.PrivateKeyEntry) entry).getCertificate().getPublicKey();
        Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(secretKey.getEncoded());
    }

    public byte[] decrypt(byte[] encryptedData, byte[] secretKey, byte[] iv) throws Exception {
        //Retrieve the private key from keystore
        KeyStore ks = KeyStore.getInstance(ANDROID_KEY_STORE);
        ks.load(null);
        KeyStore.Entry entry = ks.getEntry(SECRET_KEY_ALIAS, null);
        PrivateKey privateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
        //Decrypt the Secret key
        SecretKey sk = decryptKey(privateKey, secretKey);
        return decrypt(sk, encryptedData, iv);
    }

    private SecretKey decryptKey(PrivateKey privateKey, byte[] encryptedSecretKey) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance(RSA_ECB_PKCS1_PADDING);
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedData = cipher.doFinal(encryptedSecretKey);
        return new SecretKeySpec(decryptedData, AES);
    }

    private byte[] decrypt(SecretKey secretKey, byte[] encryptData, byte[] iv) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        Cipher cipher = Cipher.getInstance(AES_CBC_PKCS5_PADDING);
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
        return cipher.doFinal(encryptData);
    }

    private byte[] generateIv(int length) {
        byte[] b = new byte[length];
        SecureRandom random = new SecureRandom();
        random.nextBytes(b);
        return b;
    }

    public static SecretKey generateAesKey() {
        try {
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            kg.init(KEY_LENGTH);
            SecretKey key = kg.generateKey();

            return key;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

}


