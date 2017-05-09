/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.ble;

public interface BluetoothLePeripheralCallback extends BluetoothLe {

    /**
     * Failed to advertise Bluetooth LE
     */
    int BLE_ERROR_ADVERTISE_FAILED = 120;
    /**
     * Failed to authenticate the session
     */
    int BLE_ERROR_AUTH_FAILED = 121;
    /**
     * Peripheral mode is not supported on the device
     */
    int BLE_ERROR_PERIPHERAL_MODE_NOT_SUPPORTED = 122;

    /**
     * Central device is not subscribed to receive the characteristic update.
     */
    int BLE_ERROR_CENTRAL_UNSUBSCRIBED = 123;


    /**
     * Bluetooth LE Central device has been connected.
     */
    int BLE_STATE_CONNECTED = 10;
    /**
     * Bluetooth Central device has been disconnected.
     */
    int BLE_STATE_DISCONNECTED = 11;
    /**
     * The Bluetooth Peripheral Device is started and ready to accept session sharing request.
     */
    int BLE_STATE_STARTED = 12;
    /**
     * The Bluetooth Peripheral Device is stopped
     */
    int BLE_STATE_STOPPED = 13;
    /**
     * Session sharing authorization success
     */
    int BLE_STATE_SESSION_AUTHORIZED = 14;
    /**
     * Session sharing has been notified to the central device
     */
    int BLE_STATE_SESSION_NOTIFIED = 15;



    /**
     * Notify the host application that any error occur.
     * The error code corresponding to #BLE_ERROR* value.
     */
    void onError(int errorCode);

    /**
     * Notify the host application that an consent request occurred while session
     * sharing. The host application must call either handler.cancel() or
     * handler.proceed().
     *
     * @param deviceName  The remote Bluetooth Device Name.
     * @param handler An BluetoothLeConsentHandler object that will handle the user's
     *                response.
     */
    void onConsentRequested(String deviceName, BluetoothLeConsentHandler handler);

}
