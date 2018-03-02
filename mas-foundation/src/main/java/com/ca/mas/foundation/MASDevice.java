/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.context.DeviceIdentifier;
import com.ca.mas.foundation.auth.MASProximityLoginBLEPeripheralListener;
import com.ca.mas.foundation.notify.Callback;

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
     * Starts the device acting as a bluetooth peripheral.
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
                        return (new DeviceIdentifier(MAS.getContext())).toString();
                    } catch (InvalidAlgorithmParameterException | java.io.IOException |
                            java.security.KeyStoreException | java.security.NoSuchAlgorithmException |
                            java.security.NoSuchProviderException | java.security.cert.CertificateException |
                            java.security.UnrecoverableKeyException e) {
                        return null;
                    }
                }

                @Override
                public void startAsBluetoothPeripheral(MASProximityLoginBLEPeripheralListener listener) {
                    MobileSsoFactory.getInstance().startBleSessionSharing(listener);
                }

                @Override
                public void stopAsBluetoothPeripheral() {
                    MobileSsoFactory.getInstance().stopBleSessionSharing();
                }
            };
        }
        return current;
    }

}
