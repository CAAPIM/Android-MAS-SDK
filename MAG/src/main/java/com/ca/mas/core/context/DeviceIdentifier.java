/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.context;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;

import com.ca.mas.core.io.IoUtils;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class DeviceIdentifier {

    private static final String TAG = DeviceIdentifier.class.getCanonicalName();

    private String deviceId;

    public DeviceIdentifier(Context context) {
        /**
         * Generate device-id using ANDROID_ID, sharedUserId,container description if present and app signature.
         *
         * @return device-id
         */
        StringBuilder localDeviceID = new StringBuilder();
        String DEFAULT = "NONE";
        String sharedUserIdOrPackageNameHash = DEFAULT;
        String androidId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        androidId = androidId == null ? DEFAULT : androidId;


        try {
            String packageName = context.getPackageName();
            PackageManager pkgMgr = context.getPackageManager();
            PackageInfo pkgInfo = pkgMgr.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            if (pkgInfo.sharedUserId != null) {
                sharedUserIdOrPackageNameHash = String.valueOf(pkgInfo.sharedUserId.hashCode());
            } else {
                sharedUserIdOrPackageNameHash = String.valueOf(packageName.hashCode());
            }
        } catch (Exception x) {
            Log.w(TAG, "Unable to get sharedUserIdOrPackageNameHash: ", x);

        }
        StringBuilder signatures = new StringBuilder(DEFAULT);
        try {
            PackageManager pkgMgr = context.getPackageManager();
            String packageName = context.getPackageName();
            android.content.pm.Signature[] sigs = pkgMgr.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures;
            for (android.content.pm.Signature sig : sigs) {
                if (signatures.toString().equals(DEFAULT))
                    signatures = new StringBuilder(String.valueOf(sig.hashCode()));
                else {
                    signatures.append("_");
                    signatures.append(sig.hashCode());
                }
            }
        } catch (Exception noSignature) {
            Log.w(TAG, "Unable to get application signature(s): ", noSignature);
        }
        String containerDescription;
        try {
            containerDescription = getContainerDescription(context);
            if (containerDescription.equals("")) {
                containerDescription = DEFAULT;
            }
        } catch (Exception e) {
            containerDescription = DEFAULT;
        }


        localDeviceID.append(androidId);
        localDeviceID.append("__");
        localDeviceID.append(sharedUserIdOrPackageNameHash);
        localDeviceID.append("__");
        localDeviceID.append(containerDescription);
        localDeviceID.append("__");
        localDeviceID.append(signatures);

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] message = String.valueOf(localDeviceID).getBytes("UTF-8");
            md.update(message);
            byte[] mdBytes = md.digest();

            //Convert to hex String
            deviceId = IoUtils.hexDump(mdBytes);

        } catch (NoSuchAlgorithmException | UnsupportedEncodingException | IllegalArgumentException e) {
            Log.w(TAG, "Error while finding the algorithm to hash or while encoding to utf-8", e);
        }
        //Trim the device-id to <=100 characters in case they the length is more. This is fallback mechanism if hashing fails.
        if (localDeviceID.length() > 100) {
            localDeviceID = new StringBuilder(localDeviceID.substring(0, 100));
        }

        deviceId = String.valueOf(localDeviceID);
    }

    /**
     * Return a description including Container info
     *
     * @return string description, "" if not running in container
     */
    public String getContainerDescription(Context context) {
        String containerDescription = null;
        // parse the files directory to determine container id
        File filesDir = null;
        try {
            filesDir = context.getFilesDir();
            String dirs[] = filesDir.toString().split("\\/");
            if (dirs[1].equals("data")) {
                if (dirs[2].equals("data")) {
                    Log.d(TAG, "APP Not in knox container: " + dirs[3]);
                    containerDescription = "";
                } else if (dirs[2].equals("data1")) {
                    Log.d(TAG, "App In knox container #1: " + dirs[3]);
                    containerDescription = "-knox-1";
                } else if (dirs[2].equals("user")) {
                    Log.d(TAG, "APP In knox container #" + dirs[3] + ": " + dirs[4]);
                    containerDescription = "-knox-" + dirs[3];
                } else
                    Log.d(TAG, "APP Knox container status /data/" + dirs[2] + " unknown: " + filesDir);
            } else
                System.out.println("APP Knox container status /" + dirs[1] + " unknown: " + filesDir);
        } catch (Exception noDesc) {
            Log.w(TAG, "Unable to get container description from " + filesDir + ": " + noDesc);
        }

        if (containerDescription == null)
            return "";
        else
            return containerDescription;
    }

    @Override
    public String toString() {
        return deviceId;
    }
}
