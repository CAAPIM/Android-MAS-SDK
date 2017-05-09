/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.auth.NFCRenderer;
import com.ca.mas.core.auth.NfcResultReceiver;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.service.Provider;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONException;

import java.io.IOException;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * Proximity login with Near Field Communication (NFC).
 */
public class MASProximityLoginNFC extends NFCRenderer implements MASProximityLogin {

    /**
     * Error when NFC is not available
     */
    public static final int NFC_ERR = NFCRenderer.NFC_ERR;
    /**
     * Error when unable to start Bluetooth Service
     */
    public static final int BLUETOOTH_ERR = NFCRenderer.BLUETOOTH_ERR;
    /**
     * Error when failed to start Bluetooth server
     */
    public static final int BLUETOOTH_CONN_ERR = NFCRenderer.BLUETOOTH_CONN_ERR;


    @SuppressWarnings("MissingPermission")
    public static void authorize(String authenticateUrl, final MASCallback<Void> callback) {

        MobileSsoFactory.getInstance().authorize(authenticateUrl, new NfcResultReceiver() {

            @Override
            public void onSuccess(MAGResponse response) {
                String uuid;
                String address;
                try {
                    if (getData() == null) {
                        if (DEBUG) Log.w(TAG, "A Json message is expected for NFCResultReceiver.");
                        Callback.onError(callback, new IllegalArgumentException("A Json message is expected for NFCResultReceiver."));
                        return;
                    }
                    uuid = getData().getString(NFCRenderer.UUID);
                    address = getData().getString(NFCRenderer.ADDRESS);
                } catch (JSONException e) {
                    if (DEBUG) Log.w(TAG, "Invalid Json message from NFC Session Sharing", e);
                    Callback.onError(callback, e);
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
                        socket = device.createInsecureRfcommSocketToServiceRecord(java.util.UUID.fromString(uuid));
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
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(MAGError error) {
                Callback.onError(callback, error);
            }

            @Override
            public void onRequestCancelled(Bundle data) {

            }
        });
    }

    @Override
    public boolean init(Activity activity, long requestId, MASAuthenticationProviders providers) {
        this.requestId = requestId;
        for (MASAuthenticationProvider p : providers.getProviders()) {
            if (p.isProximityLogin()) {
                return super.init(activity, new Provider(p.getIdentifier(), p.getAuthenticationUrl(), p.getPollUrl(), null));
            }
        }
        return false;
    }

    @Override
    public View render() {
        return super.render();
    }

    @Override
    public void start() {
        super.onRenderCompleted();
    }

    @Override
    public void stop() {
        super.close();
    }

    @Override
    public void onError(int errorCode, String m, Exception e) {
    }


}
