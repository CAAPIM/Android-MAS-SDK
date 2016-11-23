/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.content.Context;

import com.ca.mas.core.auth.ble.BluetoothLeConsentHandler;
import com.ca.mas.core.auth.ble.BluetoothLePeripheralCallback;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.auth.MASProximityLoginBLEUserConsentHandler;

public abstract class MASProximityLoginBLEPeripheralListener implements BluetoothLePeripheralCallback {

    @Override
    public final void onConsentRequested(String deviceName, final BluetoothLeConsentHandler handler) {

        onConsentRequested(MAS.getCurrentActivity(), deviceName, new MASProximityLoginBLEUserConsentHandler() {
            @Override
            public void proceed() {
                handler.proceed();
            }

            @Override
            public void cancel() {
                handler.cancel();
            }
        });
    }

    /**
     * Notify the host application that an consent request occurred while session
     * sharing. The host application must call either handler.cancel() or
     * handler.proceed().
     *
     * @param context The current active Activity
     * @param deviceName  The remote Bluetooth Device Name.
     * @param handler An handler object that will handle the user's response.
     */
    public abstract void onConsentRequested(Context context, String deviceName, MASProximityLoginBLEUserConsentHandler handler);
}

