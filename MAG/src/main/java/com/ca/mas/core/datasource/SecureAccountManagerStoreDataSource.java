package com.ca.mas.core.datasource;

import android.content.Context;

import com.ca.mas.core.security.DefaultEncryptionProvider;
import com.ca.mas.core.security.SecureKeyStoreKeyStorageProvider;
import com.ca.mas.core.storage.StorageException;
import com.ca.mas.core.storage.StorageResult;
import com.ca.mas.core.storage.StorageResultReceiver;

import org.json.JSONObject;

/**
 * Created by kalsa12 on 2016-09-21.
 */

public class SecureAccountManagerStoreDataSource<K, V> extends AccountManagerStoreDataSource<K, V> {

    private DefaultEncryptionProvider encryptionProvider;

    public SecureAccountManagerStoreDataSource(Context context, JSONObject param, DataConverter converter) {
        super(context, param, converter);
        encryptionProvider = new DefaultEncryptionProvider(context, new SecureKeyStoreKeyStorageProvider(context));
    }

    @Override
    public void put(K key, V value) {
        byte[] valueBytes = getValueBytes(value);

        if (valueBytes == null) {
            // No need to encrypt
            super.put(key, value);
        } else {
            // Encrypt
            byte[] encryptedValue = encryptionProvider.encrypt(valueBytes);
            super.put(key, (V) encryptedValue);
        }
    }

    @Override
    public void put(K key, V value, DataSourceCallback callback) {
        byte[] valueBytes = getValueBytes(value);

        if (valueBytes == null) {
            // No need to encrypt
            super.put(key, value, callback);
        } else {
            // Encrypt
            byte[] encryptedValue = encryptionProvider.encrypt(valueBytes);
            super.put(key, (V) encryptedValue, callback);
        }
    }

    @Override
    public V get(K key) {
        try {
            StorageResult result = null;
            result = getStorage().readData((String) key);
            if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                StorageException exception = (StorageException) result.getData();
                if (exception.getCode() == StorageException.READ_DATA_NOT_FOUND) {
                    return null;
                } else {
                    throw exception;
                }
            } else {
                V encryptedValue = (V) result.getData();
                byte[] decryptedValue = encryptionProvider.decrypt( (byte[]) encryptedValue );
                if( getConverter() != null ){
                    V converted = (V) getConverter().convert(key, decryptedValue);
                    return converted;
                } else {
                    return (V) decryptedValue;
                }
            }
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void get(final K key, final DataSourceCallback callback) {
        try {
            getStorage().readData((String) key, new StorageResultReceiver(callback.getHandler()) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                        callback.onError(new DataSourceError(((StorageException) result.getData())));
                    } else {
                        byte[] encryptedData = (byte[]) result.getData();
                        byte[] decryptedData = encryptionProvider.decrypt(encryptedData);
                        if (getConverter() != null) {
                            callback.onSuccess(getConverter().convert(key, decryptedData));
                        } else {
                            callback.onSuccess(decryptedData);
                        }
                    }
                }
            });
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    /**
     * @param value
     * @return byte array of given object if object is instance of byte[] or String, null otherwise.
     */
    private byte[] getValueBytes(V value) {
        byte[] bytes;

        if (value instanceof byte[]) {
            bytes = (byte[]) value;
        } else if (value instanceof String) {
            bytes = ((String) value).getBytes();
        } else {
            bytes = null;
        }

        return bytes;
    }
}
