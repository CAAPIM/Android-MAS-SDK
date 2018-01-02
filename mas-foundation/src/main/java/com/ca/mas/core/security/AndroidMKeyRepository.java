/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.support.annotation.RequiresApi;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Calendar;
import java.util.Date;

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

@RequiresApi(api = Build.VERSION_CODES.M)
public class AndroidMKeyRepository extends AndroidKeyStoreRepository {

    KeyGenParameterSpec.Builder getKeyGenParameterSpecBuilder(String alias, GenerateKeyAttribute attributes) {
        Calendar cal = Calendar.getInstance();
        Date now = cal.getTime();
        cal.add(Calendar.YEAR, 1);
        Date end = cal.getTime();
        return new KeyGenParameterSpec.Builder(alias,
                KeyProperties.PURPOSE_ENCRYPT + KeyProperties.PURPOSE_DECRYPT
                        + KeyProperties.PURPOSE_SIGN + KeyProperties.PURPOSE_VERIFY)
                .setKeySize(attributes.getKeySize())
                .setCertificateNotBefore(now).setCertificateNotAfter(end)
                .setCertificateSubject(new X500Principal("CN=msso"))
                .setCertificateSerialNumber(BigInteger.valueOf(1))
                .setUserAuthenticationRequired(attributes.isUserAuthenticationRequired())
                .setUserAuthenticationValidityDurationSeconds(attributes.getUserAuthenticationValidityDurationSeconds())
                // In HttpUrlConnection, com.android.org.conscrypt.CryptoUpcalls.rawSignDigestWithPrivateKey
                //   requires "NONEwithRSA", so we need to include DIGEST_NONE
                //   therefore we can only setRandomizedEncruptionRequired to false
                //   and must include DIGEST_NONE in allowed digests
                .setRandomizedEncryptionRequired(false)
                .setBlockModes(BLOCK_MODE_CBC, BLOCK_MODE_CTR, BLOCK_MODE_ECB, BLOCK_MODE_GCM)
                .setDigests(DIGEST_NONE, DIGEST_MD5, DIGEST_SHA1, DIGEST_SHA256, DIGEST_SHA384, DIGEST_SHA512)
                .setEncryptionPaddings(ENCRYPTION_PADDING_PKCS7, ENCRYPTION_PADDING_RSA_OAEP, ENCRYPTION_PADDING_RSA_PKCS1)
                .setSignaturePaddings(SIGNATURE_PADDING_RSA_PSS, SIGNATURE_PADDING_RSA_PKCS1);
    }

    @Override
    AlgorithmParameterSpec getAlgorithmParameterSpec(String alias, GenerateKeyAttribute attributes) {
        return getKeyGenParameterSpecBuilder(alias, attributes).build();
    }

}
