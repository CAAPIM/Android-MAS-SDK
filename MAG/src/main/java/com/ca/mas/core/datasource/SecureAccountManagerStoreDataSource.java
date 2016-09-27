package com.ca.mas.core.datasource;

import android.content.Context;

import com.ca.mas.core.security.DefaultEncryptionProvider;
import com.ca.mas.core.security.AccountManagerKeyStorageProvider;
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
        encryptionProvider = new DefaultEncryptionProvider(context, new AccountManagerKeyStorageProvider(context));
    }

    @Override
    public void put(K key, V value) {
        put(key, value, null);
    }

    @Override
    public void put(K key, V value, DataSourceCallback callback) {
        byte[] valueBytes = getValueBytes(value);

        if (valueBytes != null) {
            // Encrypt
            valueBytes = encryptionProvider.encrypt(valueBytes);
        }

        if( callback == null ){
            super.put(key, (V) valueBytes);
        } else {
            super.put(key, (V) valueBytes, callback);
        }
    }

    @Override
    protected V getData(K key, StorageResult result) {
        V encryptedValue = (V) result.getData();
        byte[] decryptedValue = encryptionProvider.decrypt( (byte[]) encryptedValue );
        if( getConverter() != null ){
            V converted = (V) getConverter().convert(key, decryptedValue);
            return converted;
        } else {
            return (V) decryptedValue;
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
