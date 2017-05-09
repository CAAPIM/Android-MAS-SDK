/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.ble;

public interface BluetoothLeCentralCallback extends BluetoothLe {

    /**
     * Bluetooth LE Scan has started
     */
    int BLE_STATE_SCAN_STARTED = 0;
    /**
     * Bluetooth LE scan has stopped
     */
    int BLE_STATE_SCAN_STOPPED = 1;
    /**
     * Remote peripheral device has detected
     */
    int BLE_STATE_DEVICE_DETECTED = 2;
    /**
     * Connected to remote peripheral device
     */
    int BLE_STATE_CONNECTED = 3;
    /**
     * Disconnected from remote peripheral device
     */
    int BLE_STATE_DISCONNECTED = 4;
    /**
     * Bluetooth LE GATT Service has been discovered
     */
    int BLE_STATE_SERVICE_DISCOVERED = 5;
    /**
     * Bluetooth LE GATT Characteristic found
     */
    int BLE_STATE_CHARACTERISTIC_FOUND = 6;
    /**
     * Data has been written to Bluetooth GATT Characteristic
     */
    int BLE_STATE_CHARACTERISTIC_WRITTEN = 7;
    /**
     * The remote peripheral device has authorize the session sharing request
     */
    int BLE_STATE_AUTH_SUCCEEDED = 8;
    /**
     * The remote peripheral device has rejected or failed to authorize the session sharing request.
     */
    int BLE_STATE_AUTH_FAILED = 9;

}
