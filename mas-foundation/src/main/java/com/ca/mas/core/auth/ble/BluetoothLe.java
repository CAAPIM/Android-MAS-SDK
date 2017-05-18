/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.ble;

public interface BluetoothLe {

    // Internal use only, define end of message between BluetoothLe devices.
    String EOM = "EOM";

    // Internal use only, define the provider url attribute
    String PROVIDER_URL = "provider_url";

    // Internal use only, define device name attribute.
    String DEVICE_NAME = "device_name";

    /**
     * The Bluetooth on the device is disabled
     */
    int BLE_ERROR_DISABLED = 100;
    /**
     * The Bluetooth is not supported on the device
     */
    int BLE_ERROR_NOT_SUPPORTED = 101;
    /**
     * The provided UUID is empty or invalid
     */
    int BLE_ERROR_INVALID_UUID = 102;
    /**
     * Bluetooth LE session sharing is not supported
     */
    int BLE_ERROR_SESSION_SHARING_NOT_SUPPORTED = 103;
    /**
     * Notify the host application for status update.
     *
     * @param state The status code corresponding to an BLE_STATE* value defined under
     *              {@link BluetoothLeCentralCallback} or
     *              {@link BluetoothLePeripheralCallback}
     */
    void onStatusUpdate(int state);


}
