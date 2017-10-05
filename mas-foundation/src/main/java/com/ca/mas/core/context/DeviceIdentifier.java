/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.context;

import android.content.Context;
import android.util.Log;

import com.ca.mas.core.io.IoUtils;
import com.ca.mas.core.util.KeyUtilsAsymmetric;

import java.security.InvalidAlgorithmParameterException;
import java.security.MessageDigest;
import java.security.PublicKey;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class DeviceIdentifier {

    private String deviceId = "";
    private static final String DEVICE_IDENTIFIER = "com.ca.mas.foundation.msso.DEVICE_IDENTIFIER";

    /**
     * Generates a set of asymmetric keys in the Android keystore and builds the device identifier off of the public key.
     * Apps built with the same sharedUserId value in AndroidManifest.xml will reuse the same identifier.
     * @param context
     */
    public DeviceIdentifier(Context context) {
        try {
            PublicKey publicKey = KeyUtilsAsymmetric.getRsaPublicKey(DEVICE_IDENTIFIER);
            if (publicKey == null) {
                KeyUtilsAsymmetric.generateRsaPrivateKey(context, 2048, DEVICE_IDENTIFIER,
                        String.format("CN=%s, OU=%s", DEVICE_IDENTIFIER, "com.ca"),
                        false, false, Integer.MAX_VALUE, false);
                publicKey = KeyUtilsAsymmetric.getRsaPublicKey(DEVICE_IDENTIFIER);
            }

            //Convert the public key to a hash string
            byte[] encoded = publicKey.getEncoded();

            //Encode to SHA-256 and then convert to a hex string
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(encoded);
            byte[] mdBytes = md.digest();
            deviceId = IoUtils.hexDump(mdBytes);
        } catch (InvalidAlgorithmParameterException | java.io.IOException |
                java.security.KeyStoreException | java.security.NoSuchAlgorithmException |
                java.security.NoSuchProviderException | java.security.cert.CertificateException |
                java.security.UnrecoverableKeyException e) {
            if (DEBUG) Log.e(TAG, "Failed to generate device identifier.");
        }
    }

    @Override
    public String toString() {
        return deviceId;
    }
}
