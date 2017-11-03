/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.os.AsyncTask;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.context.DeviceIdentifier;
import com.ca.mas.foundation.auth.MASProximityLoginBLEPeripheralListener;
import com.ca.mas.foundation.notify.Callback;

import java.security.InvalidAlgorithmParameterException;

/**
 * <p>The <b>MASDevice</b> class is a local representation of device data.</p>
 */
public abstract class MASDevice implements Device {

    private static MASDevice current;

    private static class DeregisterTask extends AsyncTask<MASCallback<Void>, Void, Void> {

        @Override
        protected Void doInBackground(MASCallback<Void>... masCallbacks) {
            try {
                MobileSsoFactory.getInstance().removeDeviceRegistration();
                Callback.onSuccess(masCallbacks[0], null);
            } catch (Exception e) {
                Callback.onError(masCallbacks[0], e);
            }
            return null;
        }
    }

    private MASDevice() {
    }

    public static MASDevice getCurrentDevice() {
        if (current == null) {
            current = new DeviceImpl();
        }
        return current;
    }

    private static class DeviceImpl extends MASDevice {
        @Override
        public void deregister(final MASCallback<Void> callback) {

            final MobileSso mobileSso = MobileSsoFactory.getInstance();

            if (mobileSso != null && mobileSso.isDeviceRegistered()) {
                new DeregisterTask().execute(callback);
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
    }

}
