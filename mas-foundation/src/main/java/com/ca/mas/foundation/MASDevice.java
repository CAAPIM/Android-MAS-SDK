/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.support.annotation.NonNull;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.auth.ble.BluetoothLePeripheral;
import com.ca.mas.core.context.DeviceIdentifier;
import com.ca.mas.foundation.auth.MASProximityLoginBLEPeripheralListener;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * <p>The <b>MASDevice</b> class is a local representation of device data.</p>
 */
public abstract class MASDevice {

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
    public abstract void deregister(MASCallback<Void> callback);

    /**
     * Is the MASDevice registered?
     */
    public abstract boolean isRegistered();

    /**
     * <p>Resets the application’s locally stored data on the device only. This does NOT call the Gateway to remove the device record.
     * You must call {@link MASDevice#deregister(MASCallback)} to do so.</p>
     * WARNING: if you call this, all access credentials will be wiped.
     * You will have to re-register the application on the device to retrieve new ones. It may be necessary to talk to the administrator of your Gateway if you have issues.
     */
    public abstract void resetLocally();

    /**
     * The MASDevice identifier.
     */
    public abstract String getIdentifier();

    /**
     * This method is used by a device to start a BLE session sharing in a peripheral role.
     * Register your callback to receive events and errors during the session sharing.
     *
     * @param listener Register your listener to receive event and error during the session sharing.
     */

    public abstract void startAsBluetoothPeripheral(MASProximityLoginBLEPeripheralListener listener);

    /**
     * Stops the device acting as a bluetooth peripheral.
     */
    public abstract void stopAsBluetoothPeripheral();


    private static MASDevice current;

    private MASDevice() {
    }

    public static MASDevice getCurrentDevice() {
        if (current == null) {
            current = new MASDevice() {
                @Override
                public void deregister(final MASCallback<Void> callback) {
                    final MobileSso mobileSso = MobileSsoFactory.getInstance();
                    if (mobileSso != null && mobileSso.isDeviceRegistered()) {
                        Thread t = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    mobileSso.removeDeviceRegistration();
                                    Callback.onSuccess(callback, null);
                                } catch (Exception e) {
                                    Callback.onError(callback, e);
                                }
                            }
                        });
                        t.start();
                    } else {
                        Callback.onError(callback, new IllegalStateException("Device is not registered"));
                    }
                }

                @Override
                public boolean isRegistered() {
                    return MobileSsoFactory.getInstance().isDeviceRegistered();
                }

                @Override
                public void resetLocally() {
                    MobileSsoFactory.getInstance().destroyAllPersistentTokens();
                }

                @Override
                public String getIdentifier() {
                    try {
                        return (new DeviceIdentifier().toString());
                    } catch (Exception e) {
                        throw new IllegalArgumentException("Keystore is not available", e);
                    }
                }

                @Override
                public void startAsBluetoothPeripheral(MASProximityLoginBLEPeripheralListener listener) {
                    final BluetoothLePeripheral bleServer = BluetoothLePeripheral.getInstance();
                    bleServer.init(MAS.getContext());
                    bleServer.start(listener);
                }

                @Override
                public void stopAsBluetoothPeripheral() {
                    BluetoothLePeripheral.getInstance().stop();
                }
            };
        }
        return current;
    }

    /**
     * Update or create attribute to the device, throw MASDeviceAttributeOverflowException when exceed ${mag-device-max-tag}
     * @param attr String
     * @param value String
     * @param callback MASCallback<Void>
     */
    public void addAttribute(@NonNull String attr, String value, MASCallback<Void> callback) {
        DeviceMetadata.putAttribute(attr, value, callback);
    }

    /**
     *  Remove all attributes
     *  @param callback MASCallback<Void>
     */
    public void removeAllAttributes(MASCallback<Void> callback){
        DeviceMetadata.deleteAttributes(callback);
    }

    /**
     * Remove attribute by name, succeed even device attribute does not exists
     * @param attr String
     * @param callback MASCallback<Void>
     */
    public void removeAttribute(@NonNull String attr, MASCallback<Void> callback){
        DeviceMetadata.deleteAttribute(attr, callback);
    }

    /**
     * Get attribute by name, return empty String if no attribute is found.
     * @param attr String
     * @param callback MASCallback
     */
    public void getAttribute(@NonNull String attr, MASCallback<JSONObject> callback){
        DeviceMetadata.getAttribute(attr,callback);
    }

    /**
     * Get all attributes, return empty Map if no attributes found.
     * @param callback MASCallback<Map<String, String>>
     */
    public void getAttributes(MASCallback<JSONArray> callback) {
        DeviceMetadata.getAttributes(callback);
    }

}
