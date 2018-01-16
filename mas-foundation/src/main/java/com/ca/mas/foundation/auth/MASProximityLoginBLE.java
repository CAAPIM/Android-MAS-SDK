/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.app.Activity;

import com.ca.mas.core.auth.ble.BluetoothLe;
import com.ca.mas.core.auth.ble.BluetoothLeCentralRenderer;
import com.ca.mas.core.service.Provider;
import com.ca.mas.foundation.MAS;

/**
 * Proximity login with Bluetooth Low Energy (BLE)
 */
public class MASProximityLoginBLE extends BluetoothLeCentralRenderer implements MASProximityLogin {

    public static final int BLE_ERROR_SESSION_SHARING_NOT_SUPPORTED = BluetoothLe.BLE_ERROR_SESSION_SHARING_NOT_SUPPORTED;
    public static final int BLE_ERROR_NOT_SUPPORTED = BluetoothLe.BLE_ERROR_NOT_SUPPORTED;
    public static final int BLE_ERROR_DISABLED = BluetoothLe.BLE_ERROR_DISABLED;
    public static final int BLE_ERROR_INVALID_UUID = BluetoothLe.BLE_ERROR_INVALID_UUID;

    public MASProximityLoginBLE(MASProximityLoginBLECentralListener callback) {
        super(callback);
    }

    @Override
    public boolean init(Activity activity, long requestId, MASAuthenticationProviders providers) {
        this.requestId = requestId;
        for (MASAuthenticationProvider p: providers.getProviders()) {
            if (p.isProximityLogin()) {
                return super.init(MAS.getContext(),new Provider(p.getIdentifier(), p.getAuthenticationUrl(), p.getPollUrl(), null));
            }
        }
        return false;
    }

    @Override
    public void start() {
        super.onRenderCompleted();
    }

    @Override
    public void stop() {
        super.close();
    }

}
