/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.foundation.auth.MASProximityLoginBLEPeripheralListener;

/**
 * <p>The <b>Device</b> interface is a local representation of device data.</p>
 */
public interface Device {
    /**
     * <p>Deregisters the application resources on this device. This is a two step operation.</p>
     * <p>It will attempt to remove the device's registered record in the cloud, and wipe the device
     * of all credential settings. If it fails, an error is returned.</p>
     * <p>WARNING #1:</p>
     * A call to deregister during the same session that the device was initially registered will
     * fail and may leave the device in a strange state.  This call should work after the
     * initial registration and a restart of the application.
     * <p>WARNING #2:</p>
     * A successful call to deregistration will leave the current application session
     * without required credentials and settings. You must restart the application
     * to re-register the application and retrieve new credentials.
     * <p>This should be considered an advanced feature. If you make this available to end users, it
     * should not be made too easily accessible and the UI control should be marked and possibly labeled
     * to accentuate that it will wipe settings both in the cloud and locally on the device. We
     * recommend that you also provide an additional user confirmation UI component to make it clear to
     * the user what will occur and allow them to cancel the operation or proceed.</p>
     * <p>An asynchronous block callback parameter is provided for detecting the response.</p>
     *
     * @param callback The MASCallback that receives the results.
     */
    void deregister(MASCallback<Void> callback);

    /**
     * Is the MASDevice registered?
     */
    boolean isRegistered();

    /**
     * <p>Resets the application’s locally stored data on the device only. This does NOT call the Gateway to remove the device record.
     * You must call {@link MASDevice#deregister(MASCallback)} to do so.</p>
     * WARNING: if you call this, all access credentials will be wiped.
     * You will have to re-register the application on the device to retrieve new ones. It may be necessary to talk to the administrator of your Gateway if you have issues.
     */
    void resetLocally();

    /**
     * The MASDevice identifier.
     */
    String getIdentifier();

    /**
     * Starts the device acting as a bluetooth peripheral.
     */
    void startAsBluetoothPeripheral(MASProximityLoginBLEPeripheralListener listener);

    /**
     * Stops the device acting as a bluetooth peripheral.
     */
    void stopAsBluetoothPeripheral();

}
