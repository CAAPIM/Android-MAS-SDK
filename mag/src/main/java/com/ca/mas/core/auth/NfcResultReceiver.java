/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.ca.mas.core.service.MssoIntents;

import org.json.JSONException;

import java.io.IOException;
import java.util.UUID;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * The default NFC result receiver. To avoid polling the MAG, this receiver will notify
 * the session request device after it successfully authenticates the user session.
 */
public class NfcResultReceiver extends AuthResultReceiver {

    /**
     * Create a new ResultReceive to receive results.
     * Your {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public NfcResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     * Sends an Acknowledgement to the Bluetooth server after successfully authenticating the user session.
     *
     * @param resultCode the result code
     */
    @Override
    protected void onReceiveResult(int resultCode, Bundle resultData) {
        String uuid;
        String address;

        if (resultCode == MssoIntents.RESULT_CODE_SUCCESS) {
            try {
                if (json == null) {
                    if (DEBUG) Log.w(TAG, "A Json message is expected for NFCResultReceiver.");
                    return;
                }
                uuid = json.getString(NFCRenderer.UUID);
                address = json.getString(NFCRenderer.ADDRESS);
            } catch (JSONException e) {
                if (DEBUG) Log.w(TAG, "Invalid Json message from NFC Session Sharing", e);
                return;
            }
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                //Cancel Discovery to improve performance.
                adapter.cancelDiscovery();
                BluetoothDevice device = adapter.getRemoteDevice(address);
                BluetoothSocket socket = null;
                try {
                    //Client
                    socket = device.createInsecureRfcommSocketToServiceRecord(UUID.fromString(uuid));
                    socket.connect();
                } catch (Exception e) {
                    if (DEBUG) Log.d(TAG,
                            "Failed to send acknowledgement to NFC Bluetooth Service. " + e.getMessage());
                } finally {
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            if (DEBUG) Log.d(TAG,
                                    "Failed to close NFC Bluetooth Service Socket. " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}
