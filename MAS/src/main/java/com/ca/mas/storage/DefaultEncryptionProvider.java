/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

public class DefaultEncryptionProvider implements MASEncryptionProvider {

    private KeyStorageProvider ksp;


    private static final String KEY_ALIAS = "secret";
    private static final String ALGORITHM = "AES";
    private static final int KEY_SIZE = 256;
    public static final String TAG = DefaultEncryptionProvider.class.getCanonicalName();
    public static final String AES_GCM_NO_PADDING="AES/GCM/NoPadding";
    public static final String HMAC_SHA256="HmacSHA256";
    public static final int IV_LENGTH=12;


    public DefaultEncryptionProvider(@NonNull Context ctx) {
        this(ctx, new KeyStoreKeyStorageProvider(ctx));
    }

    public DefaultEncryptionProvider(Context ctx, KeyStorageProvider keyStorageProvider) {
        ksp = keyStorageProvider;
        boolean temp = ksp.containsKey(KEY_ALIAS);

        if (!temp) {

            KeyGenerator keyGenerator = new DefaultKeyGenerator(ALGORITHM, KEY_SIZE);
            SecretKey sk;
            try {
                sk = keyGenerator.generateKey();
            } catch (NoSuchAlgorithmException e) {
                Log.e(TAG, "Error while generating key");
                throw new RuntimeException(e.getMessage(), e);
            }
            ksp.storeKey(KEY_ALIAS, sk);
        }
    }

    /**
     * Encrypts the given data.
     *
     * @param data : the data to encrypt
     * @return encrypted data as byte[]
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public byte[] encrypt(byte[] data) {
        byte[] encryptedData;
        try {
            SecretKey secretKey = ksp.getKey(KEY_ALIAS);
            Cipher cipher = Cipher.getInstance(AES_GCM_NO_PADDING);

            byte[] iv;
            AlgorithmParameterSpec ivParams;
            if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                iv = cipher.getIV();
            }
            else{
                iv = new byte[IV_LENGTH];
                SecureRandom secureRandom = new SecureRandom();
                secureRandom.nextBytes(iv);
                if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
                     ivParams=new GCMParameterSpec(128,iv);
                }
                else{
                    /**
                     * GCMParameterSpec does not work in Android 19
                     */
                    ivParams= new IvParameterSpec(iv);
                }

                cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivParams);

            }

            encryptedData = cipher.doFinal(data);
            byte[] mac= computeMac(KEY_ALIAS, encryptedData);
            encryptedData=concatArrays(mac, iv, encryptedData);
        } catch (Exception e) {
            Log.e(TAG, "inside exception of encrypt function: ", e);
            throw new RuntimeException(e.getMessage(), e);
        }
        return encryptedData;
    }

    /**
     * @param encryptedData : data to be decrypted
     * @return byte[] of decrypted data
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public byte[] decrypt(byte[] encryptedData) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance(AES_GCM_NO_PADDING);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            Log.e(TAG, "Error while getting an cipher instance", e);
            throw new RuntimeException("Error while getting an cipher instance", e);
        }

        int ivlength=IV_LENGTH;
        int macLength;
        try {
            macLength=Mac.getInstance(HMAC_SHA256).getMacLength();
        } catch (NoSuchAlgorithmException e) {
           Log.e(TAG,"Error while instantiating MAC",e);
            throw new RuntimeException("Error while instantiating MAC",e);
        }
        int encryptedDataLength=encryptedData.length-ivlength-macLength;
        byte[] macFromMessage=getArraySubset(encryptedData,0,macLength);

        byte[]iv=getArraySubset(encryptedData,macLength,ivlength);
        encryptedData=getArraySubset(encryptedData,macLength+ivlength,encryptedDataLength);
        byte[] mac= computeMac(KEY_ALIAS, encryptedData);

        if(!Arrays.equals(mac, macFromMessage)){
            Log.e(TAG,"MAC signature could not be verified");
            throw new RuntimeException("MAC signature could not be verified");
        }

        AlgorithmParameterSpec ivParams;
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){

            ivParams = new GCMParameterSpec(128,iv);
        }
        else
        {
            ivParams=new IvParameterSpec(iv);
        }


        try {
            SecretKey secretKey = ksp.getKey(KEY_ALIAS);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivParams);
            return cipher.doFinal(encryptedData);

        } catch (Exception e) {
            Log.e(TAG, "Error while decrypting an cipher instance", e);
            throw new RuntimeException(e.getMessage(), e);
        }
    }



    /**
     * Computes the mac signature for the encrypted array
     * @param key : Key to use to generate a secret key for MAC operation
     * @param cipherText: the data for which the signature has to be calculated
     * @return
     */
    private byte[] computeMac(String key, byte[] cipherText){
        Mac hm;
        SecretKey secretKey = null;
        try {
            hm = Mac.getInstance(HMAC_SHA256);
            secretKey=new SecretKeySpec(key.getBytes("UTF-8"),HMAC_SHA256);
            hm.init(secretKey);
            return hm.doFinal(cipherText);
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            Log.e(TAG,"Error while calculating signature",e);
            throw new RuntimeException("Error while calculating signature",e);
        }finally {
            destroyKey(secretKey);
        }
    }

    /**
     * Combine the mac,iv and encrypted data arrays into one array
     * @param mac
     * @param iv
     * @param cipherText
     * @return
     */
    private byte[] concatArrays(byte[]mac,byte[]iv,byte[]cipherText){
        int macLength=mac.length;
        int ivLength=iv.length;
        int cipherTextLength=cipherText.length;
        int totalLength=macLength+ivLength+cipherTextLength;
        byte[] result=new byte[totalLength];
        System.arraycopy(mac, 0, result, 0, macLength);
        System.arraycopy(iv, 0, result, macLength,ivLength);
        System.arraycopy(cipherText,0,result,macLength+ivLength,cipherTextLength);
        return result;
    }

    private byte[] getArraySubset(byte[] array,int start,int length){
        byte[] result=new byte[length];
        System.arraycopy(array,start,result,0,length);
        return result;
    }

    /**
     * Destroys the ephemeral key, in this case the Secret key generated for MAC, if the key implements Destroyable
     * @param key
     */
    private void destroyKey(SecretKey key){
        if ( key instanceof Destroyable) {
            Destroyable destroyable = (Destroyable) key;
            try {
                destroyable.destroy();
            } catch ( DestroyFailedException e ) {
                Log.e(TAG,"Could not destroy key");
            }
        }
    }
}
