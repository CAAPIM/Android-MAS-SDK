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

import com.ca.mas.core.cert.PublicKeyHash;
import com.ca.mas.core.util.KeyUtilsAsymmetric;

import java.security.InvalidAlgorithmParameterException;
import java.security.PublicKey;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class DeviceIdentifier {

    private String deviceId = "";
    private static final String DEVICE_IDENTIFIER = "msso.deviceIdentifier";
    private StringBuilder localDeviceID = new StringBuilder();

    /**
     * Generate device-id using ANDROID_ID, sharedUserId,container description if present and app signature.
     *
     * @return device-id
     */
    public DeviceIdentifier(Context context) {
        String uuid = "";
        try {
            PublicKey publicKey = KeyUtilsAsymmetric.getRsaPublicKey(DEVICE_IDENTIFIER);
            if (publicKey == null) {
                KeyUtilsAsymmetric.generateRsaPrivateKey(context, 4096, DEVICE_IDENTIFIER,
                        String.format("CN=%s, OU=%s", DEVICE_IDENTIFIER, "com.ca"),
                        false, false, Integer.MAX_VALUE, false);
                publicKey = KeyUtilsAsymmetric.getRsaPublicKey(DEVICE_IDENTIFIER);
            }

            PublicKeyHash hash = PublicKeyHash.fromPublicKey(publicKey);
            uuid = hash.getHashString()
                    .replaceAll("/", "").replaceAll("\\+", "");

            localDeviceID.append(uuid);
            //Trim the device-id to <=100 characters in case they the length is more. This is fallback mechanism if hashing fails.
            if (localDeviceID.length() > 100) {
                localDeviceID = new StringBuilder(localDeviceID.substring(0, 100));
            }

            deviceId = String.valueOf(localDeviceID);
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
