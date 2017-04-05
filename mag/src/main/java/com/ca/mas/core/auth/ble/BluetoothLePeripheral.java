/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.ble;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelUuid;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.http.MAGResponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.UUID;

import static android.content.Context.BLUETOOTH_SERVICE;

public class BluetoothLePeripheral {

    private static final String SUCCESS = "0";
    private static final String CANCEL = "1";

    private static BluetoothLePeripheral instance = new BluetoothLePeripheral();

    private Context appContext;
    private ConfigurationProvider configurationProvider;
    private BluetoothLePeripheralCallback callback;

    private BluetoothLeAdvertiser advertiser;
    private BluetoothGattServer server;
    private BluetoothDevice connectedDevice;

    private boolean isAuthenticating = false;

    private BluetoothLePeripheral() {
    }

    public static BluetoothLePeripheral getInstance() {
        return instance;
    }

    public void init(ConfigurationProvider configurationProvider, Context context) {
        this.appContext = context.getApplicationContext();
        this.configurationProvider = configurationProvider;
        stop();
    }

    public boolean isAuthenticating() {
        return isAuthenticating;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public synchronized void start(final BluetoothLePeripheralCallback callback) {
        this.callback = getCallback(callback);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            notifyWithError(BluetoothLe.BLE_ERROR_SESSION_SHARING_NOT_SUPPORTED);
            return;
        }

        final Context context = appContext;
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            notifyWithError(BluetoothLe.BLE_ERROR_NOT_SUPPORTED);
            return;
        }

        BluetoothManager btManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        if (btManager == null) {
            notifyWithError(BluetoothLe.BLE_ERROR_NOT_SUPPORTED);
            return;
        }
        BluetoothAdapter btAdapter = btManager.getAdapter();

        if (btAdapter == null) {
            notifyWithError(BluetoothLe.BLE_ERROR_NOT_SUPPORTED);
            return;
        }

        if (!btAdapter.isEnabled()) {
            notifyWithError(BluetoothLe.BLE_ERROR_DISABLED);
            return;
        }

        String uuid = configurationProvider.getProperty(MobileSsoConfig.PROP_BLE_SERVICE_UUID);

        if (uuid == null || uuid.trim().length() == 0) {
            notifyWithError(BluetoothLe.BLE_ERROR_INVALID_UUID);
            return;
        }

        UUID serviceUUID = null;

        try {
            serviceUUID = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            notifyWithError(BluetoothLe.BLE_ERROR_INVALID_UUID);
            return;
        }

        uuid = configurationProvider.getProperty(MobileSsoConfig.PROP_BLE_CHARACTERISTIC_UUID);

        if (uuid == null || uuid.trim().length() == 0) {
            notifyWithError(BluetoothLe.BLE_ERROR_INVALID_UUID);
            return;
        }

        UUID characteristicUUID = null;

        try {
            characteristicUUID = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            notifyWithError(BluetoothLe.BLE_ERROR_INVALID_UUID);
            return;
        }

        // this will always return NULL
        advertiser = btAdapter.getBluetoothLeAdvertiser();
        if (advertiser == null) {
            notifyWithError(BluetoothLePeripheralCallback.BLE_ERROR_PERIPHERAL_MODE_NOT_SUPPORTED);
            return;
        }

        if (server != null) {
            return;
        }

        AdvertiseSettings.Builder settingsBuilder = new AdvertiseSettings.Builder();
        settingsBuilder.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED);
        settingsBuilder.setConnectable(true);
        settingsBuilder.setTimeout(0);
        settingsBuilder.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM);

        AdvertiseData.Builder dataBuilder = new AdvertiseData.Builder();
        dataBuilder.setIncludeTxPowerLevel(true);
        dataBuilder.addServiceUuid(new ParcelUuid(serviceUUID));

        advertiser.startAdvertising(settingsBuilder.build(), dataBuilder.build(), new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
            }

            @Override
            public void onStartFailure(int errorCode) {
                super.onStartFailure(errorCode);
                notifyWithError(BluetoothLePeripheralCallback.BLE_ERROR_ADVERTISE_FAILED);
            }
        });

        final StringBuilder buffer = new StringBuilder();

        server = btManager.openGattServer(context, new BluetoothGattServerCallback() {

            @Override
            public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
                super.onConnectionStateChange(device, status, newState);
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    connectedDevice = device;
                    callback.onStatusUpdate(BluetoothLePeripheralCallback.BLE_STATE_CONNECTED);
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    callback.onStatusUpdate(BluetoothLePeripheralCallback.BLE_STATE_DISCONNECTED);
                    connectedDevice = null;
                }
            }

            @Override
            public void onCharacteristicWriteRequest(final BluetoothDevice device, int requestId, final BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value) {
                super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                String received = new String(value);
                if (received.equals(BluetoothLe.EOM)) {
                    String session = null;
                    String deviceName = null;
                    try {
                        JSONObject jsonObject = new JSONObject(buffer.toString());
                        session = jsonObject.getString(BluetoothLe.PROVIDER_URL);
                        deviceName = jsonObject.optString(BluetoothLe.DEVICE_NAME);
                    } catch (JSONException e) {
                        notifyWithError(BluetoothLePeripheralCallback.BLE_ERROR_AUTH_FAILED);
                        return;
                    }
                    buffer.setLength(0);

                    final String finalSession = session;
                    callback.onConsentRequested(deviceName, new BluetoothLeConsentHandler() {

                        @Override
                        public void proceed() {
                            isAuthenticating = true;
                            MobileSsoFactory.getInstance(context).authorize(finalSession, new MAGResultReceiver() {

                                @Override
                                public void onSuccess(MAGResponse response) {
                                    isAuthenticating = false;

                                    if (response.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                        characteristic.setValue(SUCCESS);
                                        callback.onStatusUpdate(BluetoothLePeripheralCallback.BLE_STATE_SESSION_AUTHORIZED);
                                        try {
                                            server.notifyCharacteristicChanged(device, characteristic, true);
                                            callback.onStatusUpdate(BluetoothLePeripheralCallback.BLE_STATE_SESSION_NOTIFIED);
                                        } catch (Exception e) {
                                            callback.onError(BluetoothLePeripheralCallback.BLE_ERROR_CENTRAL_UNSUBSCRIBED);
                                        }
                                    } else {
                                        onError(new MAGError(new MAGException(MAGErrorCode.UNKNOWN, "Server response with error.")) );
                                    }
                                }

                                @Override
                                public void onError(MAGError error) {
                                    isAuthenticating = false;
                                    characteristic.setValue(error.getMessage());
                                    notifyWithError(BluetoothLePeripheralCallback.BLE_ERROR_AUTH_FAILED);
                                    try {
                                        server.notifyCharacteristicChanged(device, characteristic, true);
                                        callback.onStatusUpdate(BluetoothLePeripheralCallback.BLE_STATE_SESSION_NOTIFIED);
                                    } catch (Exception e) {
                                        callback.onError(BluetoothLePeripheralCallback.BLE_ERROR_CENTRAL_UNSUBSCRIBED);
                                    }
                                }

                                @Override
                                public void onRequestCancelled(Bundle bundle) {

                                }
                            });
                        }

                        @Override
                        public void cancel() {
                            characteristic.setValue(CANCEL);
                            server.notifyCharacteristicChanged(device, characteristic, true);
                        }
                    });

                } else {
                    buffer.append(received);
                }
            }

        });
        BluetoothGattService service = new BluetoothGattService(serviceUUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);
        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(characteristicUUID,
                (BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE | BluetoothGattCharacteristic.PROPERTY_NOTIFY),
                BluetoothGattCharacteristic.PERMISSION_WRITE | BluetoothGattCharacteristic.PERMISSION_READ);

        service.addCharacteristic(characteristic);
        if (server != null) {
            server.addService(service);
            callback.onStatusUpdate(BluetoothLePeripheralCallback.BLE_STATE_STARTED);
        } else {
            callback.onError(BluetoothLePeripheralCallback.BLE_ERROR_ADVERTISE_FAILED);
        }

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public synchronized void stop() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            notifyWithError(BluetoothLePeripheralCallback.BLE_ERROR_NOT_SUPPORTED);
            return;
        }

        if (advertiser != null) {
            try {
                advertiser.stopAdvertising(new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        super.onStartSuccess(settingsInEffect);
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        super.onStartFailure(errorCode);
                    }
                });
            } catch (Exception e) {
            }
        }

        if (server != null) {
            try {
                if (connectedDevice != null) {
                    server.cancelConnection(connectedDevice);
                }
                server.clearServices();
                server.close();
            } catch (Exception e) {
            }
            getCallback(callback).onStatusUpdate(BluetoothLePeripheralCallback.BLE_STATE_STOPPED);
        }

        server = null;
        connectedDevice = null;

    }

    private void notifyWithError(int errorCode) {
        getCallback(callback).onError(errorCode);
    }

    private BluetoothLePeripheralCallback getCallback(BluetoothLePeripheralCallback callback) {
        if (callback == null) {
            return new BluetoothLePeripheralCallback() {

                @Override
                public void onStatusUpdate(int state) {
                }

                @Override
                public void onError(int errorCode) {
                }

                @Override
                public void onConsentRequested(String deviceName, BluetoothLeConsentHandler handler) {
                }
            };
        }
        return callback;
    }

}
