/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.util;

import android.os.Build;
import androidx.annotation.RequiresApi;

import com.ca.mas.core.security.GenerateKeyAttribute;
import com.ca.mas.core.security.KeyStoreException;
import com.ca.mas.core.security.KeyStoreRepository;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASConfiguration;

import java.security.InvalidAlgorithmParameterException;
import java.security.Key;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.security.spec.MGF1ParameterSpec;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

/**
 * Utility methods for working with keys and key pairs.
 */

public class KeyUtilsAsymmetric {

    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";

    // RSA Encryption Ciphers
    private static final String CIPHER_ENCRYPTION_ANDROID_M_PLUS = "RSA/ECB/OAEPWithSHA-256AndMGF1Padding";

    private static KeyStoreRepository keyRepository = KeyStoreRepository.getKeyStoreRepository();

    /**
     * Generate a new RSA keypair with the specified number of key bits.
     * This will always create the key pair inside the AndroidKeyStore,
     * ensuring it is always protected.
     * <p>
     * For Pre-M, lollipopEncryptionRequired requires that the user sets
     * a screen lock and that the keys will be encrypted at rest.
     * <p>
     * For M+:
     * "The Android Keystore system lets you store cryptographic keys in a
     * container to make it more difficult to extract from the device.
     * Once keys are in the keystore, they can be used for cryptographic
     * operations with the key material remaining non-exportable".
     * This means that the key can't be extracted from the keystore, and
     * note that keys can only be shared between applications with
     * the same signing key and shared user id.
     * Therefore, it is not necessary to protect private keys stored
     * in the AndroidKeyStore with a screen lock in the same way
     * encryption was required for Pre-M.
     * <p>
     * The key protection parameters for M+ AndroidKeyStore RSA keys
     * marshmallowUserAuthenticationRequired,
     * marshmallowUserAuthenticationValidityDurationSeconds,
     * and nougatInvalidatedByBiometricEnrollment are linked,
     * below are the combinations:
     * <p>
     * 1) Android M+: marshmallowUserAuthenticationRequired(false) - the keys
     * are still protected from export but can be used whenever needed.
     * 2) Android M+: marshmallowUserAuthenticationRequired(true) and
     * marshmallowUserAuthenticationValidityDurationSeconds( > 0 ) -
     * the key can only be used if the pin has been entered within the given
     * number of seconds.  On some devices, when a fingerprint is added
     * the key is invalidated.  The # seconds can be set absurdly high,
     * for example 100,000 if it's ok for more than a day to pass without
     * an unlock.
     * 3) Android M+: marshmallowUserAuthenticationRequired(true) and
     * marshmallowUserAuthenticationValidityDurationSeconds(zero or neg) -
     * works only with fingerprint+pin/swipe/pattern, and requires that
     * the fingerprint is entered every time a key is used.  This requires
     * an added layer to prompt the user to swipe their fingerprint.
     * 4) Android N+: nougatInvalidatedByBiometricEnrollment(true/false)
     * changes the behavior in #2 - either the key will or won't be
     * invalided when the user adds an or another fingerprint.
     *
     * @param keysize                                              the key size in bits, eg 2048.
     * @param alias                                                the keystore alias to use
     * @param dn                                                   the dn for the initial self-signed certificate
     * @param lollipopEncryptionRequired                           true/false for pre-Android-M:
     *                                                             requires that the user sets a screen lock and
     *                                                             that the keys will be encrypted at rest
     * @param marshmallowUserAuthenticationRequired                true/false for Android-M+:
     *                                                             requires a lock screen in order to use the key.  If the validity duration
     *                                                             is equal to zero, a fingerprint validation is required for every use of the key.
     * @param marshmallowUserAuthenticationValidityDurationSeconds # secs for Android-M+:
     *                                                             if user authentication is required, this specifies the number of seconds after
     *                                                             unlocking the screen where key is still usable.  If this value is zero, a
     *                                                             fingerprint is required for every use.
     * @param nougatInvalidatedByBiometricEnrollment               true/false for Android-N+:
     *                                                             if setUserAuthenticationRequired true, some Android M devices may disable
     *                                                             a key if a fingerprint is added.  Setting this value to true ensures
     *                                                             the key is usabl even if a fingerprint is added.
     * @return a new RSA PrivateKey, created in and protected by the AndroidKeyStore.
     * The matching self-signed public certificate cannot be deleted.
     * @throws RuntimeException if an RSA key pair of the requested size cannot be generated
     */
    public static PrivateKey generateRsaPrivateKey(int keysize,
                                                   String alias, String dn, boolean lollipopEncryptionRequired,
                                                   boolean marshmallowUserAuthenticationRequired,
                                                   int marshmallowUserAuthenticationValidityDurationSeconds,
                                                   boolean nougatInvalidatedByBiometricEnrollment)
            throws KeyStoreException {

        int suggestedKeySize = keysize;
        // use a minimum of 2048
        if (keysize < 2048) suggestedKeySize = 2048;

        GenerateKeyAttribute attribute = new GenerateKeyAttribute();
        attribute.setKeySize(suggestedKeySize);
        attribute.setDn(dn);
        attribute.setEncryptionRequired(lollipopEncryptionRequired);
        attribute.setUserAuthenticationRequired(marshmallowUserAuthenticationRequired);
        attribute.setInvalidatedByBiometricEnrollment(nougatInvalidatedByBiometricEnrollment);
        attribute.setUserAuthenticationValidityDurationSeconds(marshmallowUserAuthenticationValidityDurationSeconds);

        return keyRepository.createPrivateKey(sanitizeAlias(alias), attribute).getPrivate();

    }

    public static PrivateKey generateRsaPrivateKey(String alias, String dn, boolean lollipopEncryptionRequired,
                                                   boolean marshmallowUserAuthenticationRequired,
                                                   int marshmallowUserAuthenticationValidityDurationSeconds,
                                                   boolean nougatInvalidatedByBiometricEnrollment) throws KeyStoreException {
        return generateRsaPrivateKey(2048,
                alias, dn, lollipopEncryptionRequired,
                marshmallowUserAuthenticationRequired, marshmallowUserAuthenticationValidityDurationSeconds,
                nougatInvalidatedByBiometricEnrollment);
    }


    /**
     * Gets the existing Android keystore key.
     *
     * @param alias the alias of the existing private key
     * @return the keystore Key
     */
    public static Key getKeystoreKey(String alias)
            throws java.io.IOException, java.security.KeyStoreException,
            java.security.NoSuchAlgorithmException, java.security.cert.CertificateException,
            java.security.UnrecoverableKeyException {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
        keyStore.load(null);
        return keyStore.getKey(alias, null);
    }

    public static String sanitizeAlias(String alias) {
        if (!MASConfiguration.getCurrentConfiguration().isSsoEnabled()) {
            return MAS.getContext().getPackageName() + "_" + alias;
        } else {
            return alias;
        }
    }


    /**
     * Get the existing private key.
     *
     * @param alias the alias of the existing private key
     * @return the Private Key
     */
    public static PrivateKey getRsaPrivateKey(String alias) throws KeyStoreException {
        return (PrivateKey) keyRepository.getPrivateKey(sanitizeAlias(alias));
    }

    /**
     * Get the existing self-signed public key.
     * Note: the private key will always be associated with the initial
     * self-signed public certificate.  The PublicKey can be
     * used in a Certificate Signing Request.
     *
     * @param alias the alias of the existing private key
     * @return the Private Key object
     */
    public static PublicKey getRsaPublicKey(String alias) throws KeyStoreException {
        return (PublicKey) keyRepository.getPublicKey(sanitizeAlias(alias));
    }


    /**
     * Remove the existing public and private keypair.  This will
     * not remove a certificate chain stored separately.
     *
     * @param alias the alias of the existing private key +
     *              self-signed public key
     */
    public static void deletePrivateKey(String alias) {
        keyRepository.deleteKey(sanitizeAlias(alias));
    }


    /**
     * This will install or replace the existing certificate chain in the AndroidKeyStore.
     * The chain will be stored as "alias#".
     * When private keys are generated, there is always a self-signed certificate
     * generated at the same time.  In other keystore types, this self-signed certificate
     * can be replaced by a CA-signed public cert.  For AndroidKeyStore, the self-signed
     * certificate cannot be replaced.  Therefore, any new certificate chain generated
     * for the original public key will always remain.  This will store the certificate
     * chain under a separate alias.
     *
     * @param aliasPrefix the alias prefix to use, will be appended with position number,
     *                    where position 0 in the array will be 1.
     * @param chain       array of certificates in chain.  Typically the public certificate
     *                    matching the private key will be in array position [0].
     */
    public static void setCertificateChain(String aliasPrefix, X509Certificate[] chain) throws KeyStoreException {
        keyRepository.saveCertificateChain(sanitizeAlias(aliasPrefix), chain);
    }

    /**
     * This will return the CA-signed certificate chain stored in the
     * AndroidKeyStore.  Note this is stored separately from the
     * self-signed public certificate from any RSA keypair.
     *
     * @param aliasPrefix Alias which which used to store the Certificate Chain
     * @return The Certificate Chain
     */
    public static X509Certificate[] getCertificateChain(String aliasPrefix) throws KeyStoreException {
        return keyRepository.getCertificateChain(sanitizeAlias(aliasPrefix));
    }

    /**
     * This will delete the existing certificate chain in the AndroidKeyStore.
     * It will not delete any RSA self-signed public certificate from a
     * generated KeyPair.
     * The chain will be stored as "alias#".
     *
     * @param aliasPrefix the alias prefix to use, will be appended with position number,
     *                    where position 0 in the array will be 1.
     */
    public static void clearCertificateChain(String aliasPrefix) {
        keyRepository.deleteCertificateChain(sanitizeAlias(aliasPrefix));
    }


    /**
     * Encrypt data using an RSA public key, as long as the
     * content is less than an eighth of the key size.
     * If the key is stored in the AndroidKeyStore, it will
     * not have to be unlocked prior to encryption.
     * The maximum data size for encryption is dependent
     * upon the size of the key.  For 2048-bit keys,
     * the limit is (256 - padding/11).
     *
     * @param publicKey        an RSA public key
     * @param contentToEncrypt the bytes to encrypt - if the size
     *                         exceeds 256 bytes then the data cannot be encrypted using
     *                         this method
     * @return encrypted bytes
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private static byte[] encryptSection(PublicKey publicKey, byte[] contentToEncrypt)
            throws javax.crypto.NoSuchPaddingException, java.security.InvalidKeyException,
            javax.crypto.BadPaddingException, java.security.NoSuchAlgorithmException,
            javax.crypto.IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(CIPHER_ENCRYPTION_ANDROID_M_PLUS);
        // ensure OAEP padding works with AndroidKeyStore
        cipher.init(
                Cipher.ENCRYPT_MODE,
                publicKey,
                new OAEPParameterSpec(
                        "SHA-256",
                        "MGF1",
                        MGF1ParameterSpec.SHA1,
                        PSource.PSpecified.DEFAULT));

        return cipher.doFinal(contentToEncrypt);
    }


    /**
     * Decrypt data using an RSA private key, as long as the byte
     * length is around 70% less than the key size.
     * If the key is stored in the AndroidKeyStore, the keystore must
     * be unlocked prior to use.
     *
     * @param privateKey       an RSA private key
     * @param contentToDecrypt the bytes to decrypt
     * @return the decrypted bytes
     */
    @RequiresApi(Build.VERSION_CODES.M)
    private static byte[] decryptSection(PrivateKey privateKey, byte[] contentToDecrypt)
            throws javax.crypto.NoSuchPaddingException, java.security.InvalidKeyException,
            javax.crypto.BadPaddingException, java.security.NoSuchAlgorithmException,
            javax.crypto.IllegalBlockSizeException, InvalidAlgorithmParameterException {
        Cipher cipher;
        cipher = Cipher.getInstance(CIPHER_ENCRYPTION_ANDROID_M_PLUS);
        // ensure OAEP padding works with AndroidKeyStore
        cipher.init(
                Cipher.DECRYPT_MODE,
                privateKey,
                new OAEPParameterSpec(
                        "SHA-256",
                        "MGF1",
                        MGF1ParameterSpec.SHA1,
                        PSource.PSpecified.DEFAULT));

        return cipher.doFinal(contentToDecrypt);
    }

    /**
     * Decrypt data using an RSA private key.
     * If the key is stored in the AndroidKeyStore, the keystore must
     * be unlocked prior to use.
     *
     * @param privateKey       an RSA private key
     * @param contentToDecrypt the bytes to decrypt
     * @return the decrypted bytes
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public static byte[] decrypt(PrivateKey privateKey, byte[] contentToDecrypt)
            throws javax.crypto.NoSuchPaddingException, java.security.InvalidKeyException,
            javax.crypto.BadPaddingException, java.security.NoSuchAlgorithmException,
            javax.crypto.IllegalBlockSizeException, InvalidAlgorithmParameterException {
        //keysize / 8
        int encryptedSize = 256;

        ArrayList<byte[]> encryptedParts = arraySplit(contentToDecrypt, encryptedSize);
        // now decrypt the lists
        ArrayList<byte[]> decrypted = new ArrayList<>();
        for (byte[] bytesToDecrypt : encryptedParts) {
            byte[] decryptedBytes = decryptSection(privateKey, bytesToDecrypt);
            decrypted.add(decryptedBytes);
        }
        return arrayConcat(decrypted);
    }

    /**
     * Encrypt data using an RSA public key,.
     * If the key is stored in the AndroidKeyStore, it will
     * not have to be unlocked prior to encryption.
     * The maximum data size for encryption is dependent
     * upon the size of the key.  For 2048-bit keys,
     * the limit is (256 - padding/11).
     *
     * @param publicKey        an RSA public key
     * @param contentToEncrypt the bytes to encrypt - if the size
     *                         exceeds 256 bytes then the data cannot be encrypted using
     *                         this method
     * @return encrypted bytes
     */
    @RequiresApi(Build.VERSION_CODES.M)
    public static byte[] encrypt(PublicKey publicKey, byte[] contentToEncrypt)
            throws javax.crypto.NoSuchPaddingException, java.security.InvalidKeyException,
            javax.crypto.BadPaddingException, java.security.NoSuchAlgorithmException,
            javax.crypto.IllegalBlockSizeException, InvalidAlgorithmParameterException {

        // Use (encryptedSize * 7) / 10 as original chunk
        int originalChunk = 128;

        ArrayList<byte[]> original = arraySplit(contentToEncrypt, originalChunk);
        ArrayList<byte[]> encrypted = new ArrayList<>();
        for (byte[] bytesToEncrypt : original) {
            byte[] encryptedBytes = encryptSection(publicKey, bytesToEncrypt);
            encrypted.add(encryptedBytes);
        }
        return arrayConcat(encrypted);
    }


    /**
     * Combine the arrays
     *
     * @param byteArrayList list of byte arrays
     * @return combined byte array
     */
    private static byte[] arrayConcat(ArrayList<byte[]> byteArrayList) {
        int lengthTotal = 0;
        for (byte[] byteArray : byteArrayList)
            lengthTotal += byteArray.length;
        byte[] result = new byte[lengthTotal];
        int lengthCurrent = 0;
        for (byte[] byteArray : byteArrayList) {
            System.arraycopy(byteArray, 0, result, lengthCurrent, byteArray.length);
            lengthCurrent += byteArray.length;
        }
        return result;
    }

    /**
     * Split the byte array into even chunks, last will be whatever is left over
     *
     * @param bytes      the byte array to split
     * @param eachLength the value to split by
     * @return list of byte arrays
     */
    private static ArrayList<byte[]> arraySplit(byte[] bytes, int eachLength) {
        ArrayList<byte[]> result = new ArrayList<>();
        int lengthTotal = 0;
        while (lengthTotal < bytes.length) {
            int lengthCurrent = bytes.length - lengthTotal;
            if (lengthCurrent > eachLength)
                lengthCurrent = eachLength;
            byte[] section = new byte[lengthCurrent];
            System.arraycopy(bytes, lengthTotal, section, 0, lengthCurrent);
            result.add(section);
            lengthTotal += lengthCurrent;
        }
        return result;
    }


    /**
     * Unused constructor
     */
    protected KeyUtilsAsymmetric() {
    }


}
