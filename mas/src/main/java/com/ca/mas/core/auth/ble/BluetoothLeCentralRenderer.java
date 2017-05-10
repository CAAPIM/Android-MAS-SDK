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
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.auth.PollingRenderer;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.auth.ble.BluetoothLe;
import com.ca.mas.core.auth.ble.BluetoothLeCentralCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * BluetoothLe for cross device session sharing
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BluetoothLeCentralRenderer extends PollingRenderer {

    /**
     * Maximum bytes that can send with bluetooth characteristic
     */
    public static final int MAX = 20;

    private static final UUID NOTIFY_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothGatt bluetoothGatt;
    private BluetoothLeScanner scanner;
    private ScanCallback scanCallback;

    private UUID serviceUUID;
    private UUID characteristicUUID;

    private BluetoothLeCentralCallback callback;

    private int rssi = -80;

    public BluetoothLeCentralRenderer(BluetoothLeCentralCallback callback) {
        this.callback = getCallback(callback);
    }

    @Override
    public View render() {
        return null;
    }

    @Override
    public void onRenderCompleted() {
        super.onRenderCompleted();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            onError(BluetoothLe.BLE_ERROR_SESSION_SHARING_NOT_SUPPORTED, "Bluetooth LE session sharing not supported", null);
            return;
        }

        BluetoothManager manager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager == null) {
            onError(BluetoothLe.BLE_ERROR_NOT_SUPPORTED, "Bluetooth LE not supported", null);
            return;
        }
        BluetoothAdapter btAdapter = manager.getAdapter();

        if (btAdapter == null) {
            onError(BluetoothLe.BLE_ERROR_NOT_SUPPORTED, "Bluetooth LE not supported", null);
            return;
        }
        if (!btAdapter.isEnabled()) {
            onError(BluetoothLe.BLE_ERROR_DISABLED, "Bluetooth LE Disabled", null);
            return;
        }

        ConfigurationProvider configurationProvider = MobileSsoFactory.getInstance().getConfigurationProvider();
        String uuid = configurationProvider.getProperty(MobileSsoConfig.PROP_BLE_SERVICE_UUID);

        if (uuid == null || uuid.trim().length() == 0) {
            onError(BluetoothLe.BLE_ERROR_INVALID_UUID, "Service UUID is not provided", null);
            return;
        }

        try {
            serviceUUID = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            onError(BluetoothLe.BLE_ERROR_INVALID_UUID, "Invalid Service UUID", e);
            return;
        }

        uuid = configurationProvider.getProperty(MobileSsoConfig.PROP_BLE_CHARACTERISTIC_UUID);

        if (uuid == null || uuid.trim().length() == 0) {
            onError(BluetoothLe.BLE_ERROR_INVALID_UUID, "Characteristic UUID is not provided", null);
            return;
        }

        try {
            characteristicUUID = UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            onError(BluetoothLe.BLE_ERROR_INVALID_UUID, "Invalid Characteristic UUID", e);
            return;
        }

        Integer r = configurationProvider.getProperty(MobileSsoConfig.PROP_BLE_RSSI);
        if (r != null) {
            rssi = r;
        }

        scanner = manager.getAdapter().getBluetoothLeScanner();
        startScan();
        callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_SCAN_STARTED);
    }

    /**
     * Notify the host application when any error occur.
     *
     * @param code    The status code corresponding to an BLE_ERROR* value defined under
     *                {@link com.ca.mas.core.auth.ble.BluetoothLeCentralCallback}
     * @param message Error message The error message
     * @param e       Exception The exception for the error.
     */
    @Override
    public void onError(int code, String message, Exception e) {
        close();
    }

    @Override
    public void close() {
        super.close();
        stopScan();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
    }

    private void startScan() {

        if (DEBUG) Log.d(TAG, "Start BLE Scanning...");

        scanCallback = new ScanCallback() {
            @Override
            public synchronized void onScanResult(int callbackType, ScanResult result) {
                callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_DEVICE_DETECTED);
                if (DEBUG) Log.d(TAG,
                        "BLE advertisement has been found with Rssi: " + result.getRssi());
                if (result.getRssi() > rssi) {
                    if (scanner != null) {
                        stopScan();
                        if (DEBUG) Log.d(TAG, "Start process BLE session sharing.");
                        startProcess(result.getDevice());
                    }
                }
            }
        };

        ScanFilter.Builder scanFilterBuilder = new ScanFilter.Builder();
        scanFilterBuilder.setServiceUuid(new ParcelUuid(serviceUUID));
        List<ScanFilter> filters = new ArrayList<ScanFilter>();
        filters.add(scanFilterBuilder.build());

        ScanSettings.Builder scanSettingBuilder = new ScanSettings.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            scanSettingBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED).setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES);
        } else {
            scanSettingBuilder.setScanMode(ScanSettings.SCAN_MODE_BALANCED);
        }

        scanner.startScan(filters, scanSettingBuilder.build(), scanCallback);
    }

    private void stopScan() {
        if (scanner != null) {
            scanner.stopScan(scanCallback);
            scanner = null;
            callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_SCAN_STOPPED);
        }
    }

    private void startProcess(final BluetoothDevice bluetoothDevice) {
        bluetoothGatt = bluetoothDevice.connectGatt(context, false, new BluetoothGattCallback() {

                    private List<byte[]> session;
                    private int i = 0;

                    @Override
                    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                        super.onServicesDiscovered(gatt, status);
                        BluetoothGattService service = gatt.getService(serviceUUID);
                        if (service == null) {
                            callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_DISCONNECTED);
                            return;
                        }

                        callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_SERVICE_DISCOVERED);

                        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUUID);

                        if (characteristic != null) {
                            callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_CHARACTERISTIC_FOUND);
                            bluetoothGatt.setCharacteristicNotification(characteristic, true);
                            session = splitArray(getSessionRequest(), MAX);
                            byte[] first = session.get(i);
                            characteristic.setValue(first);
                            gatt.writeCharacteristic(characteristic);
                            BluetoothGattDescriptor d = characteristic.getDescriptor(NOTIFY_UUID);
                            if (d != null) {
                                d.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
                                bluetoothGatt.writeDescriptor(d);
                            }
                        }
                    }

                    @Override
                    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                        // if connected successfully
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_CONNECTED);
                            //discover services
                            gatt.discoverServices();
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                            callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_DISCONNECTED);
                        }
                    }

                    @Override
                    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                        if (characteristic != null) {
                            byte[] result = characteristic.getValue();
                            String s = new String(result);
                            if ("0".equals(s)) {
                                proceed();
                            } else {
                                callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_AUTH_FAILED);
                                close();
                            }
                        }
                    }

                    @Override
                    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        i++;
                        if (i == session.size()) {
                            //last
                            characteristic.setValue(BluetoothLe.EOM);
                            gatt.writeCharacteristic(characteristic);
                            return;
                        } else if (i > session.size()) {
                            callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_CHARACTERISTIC_WRITTEN);
                            poll();
                            return;
                        }
                        characteristic.setValue(session.get(i));
                        gatt.writeCharacteristic(characteristic);
                    }
                }
        );
    }

    @Override
    protected void onAuthCodeReceived(String code) {
        super.onAuthCodeReceived(code);
        callback.onStatusUpdate(BluetoothLeCentralCallback.BLE_STATE_AUTH_SUCCEEDED);
    }

    private List<byte[]> splitArray(byte[] items, int max) {
        List<byte[]> result = new ArrayList<byte[]>();
        if (items == null || items.length == 0) {
            return result;
        }
        int from = 0;
        int to = 0;
        int slicedLength = 0;
        while (slicedLength < items.length) {
            to = from + Math.min(max, items.length - to);
            byte[] slice = Arrays.copyOfRange(items, from, to);
            result.add(slice);
            slicedLength += slice.length;
            from = to;
        }
        return result;
    }

    private BluetoothLeCentralCallback getCallback(BluetoothLeCentralCallback callback) {
        if (callback == null) {
            return new BluetoothLeCentralCallback() {

                @Override
                public void onStatusUpdate(int state) {

                }
            };
        }
        return callback;
    }

    private byte[] getSessionRequest() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(BluetoothLe.PROVIDER_URL, provider.getUrl());
            jsonObject.putOpt(BluetoothLe.DEVICE_NAME, Build.MODEL);
            return jsonObject.toString().getBytes();
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, e.getMessage(), e);
        }
        return null;
    }

    @Override
    protected boolean startPollingOnStartup() {
        return false;
    }


}
