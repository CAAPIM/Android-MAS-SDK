/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.datasource;
import android.content.ComponentName;
import android.content.Context;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.util.Log;

import com.ca.mas.core.storage.MASSecureSharedStorage;
import com.ca.mas.core.storage.sharedstorage.MASAuthenticatorService;
import com.ca.mas.foundation.MAS;

import org.json.JSONObject;

import java.util.List;

import static com.ca.mas.core.datasource.KeystoreDataSource.SHARE;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

@Keep
public class MASSecureStorageDataSource<K, V> implements DataSource<K, V>  {

    private MASSecureSharedStorage storage;
    private Context context;

    // - This returning type of the GET method depend on this field
    private DataConverter converter;

    private static final String ACCOUNT_NAME = "account.name";
    private static final String CA_MAS = "CA MAS Secure";

    public MASSecureStorageDataSource(Context context, JSONObject param, DataConverter conv){

        if (param == null) {
            return;
        }

        boolean shared = param.optBoolean(SHARE, false);
        this.converter = conv;
        this.context = context;


        // - For this DataSource we will always encrypt
        this.storage = new MASSecureSharedStorage(getAccountName(), true, shared, converter == null);
    }

    @Override
    public void put(K key, V value) {
        if (isKeyString(key)) {
            return;
        }

        String keyString = (String) key;
        if (value == null) {
            remove(key);
            return;
        }

        if (value instanceof byte[]) {
            storage.save(keyString, (byte[]) value);
        } else if (value instanceof String) {
            storage.save(keyString, (String) value);
        } else {
            if (MAS.DEBUG) {
                Log.e(TAG, "Value type not supported");
            }
            throw new UnsupportedOperationException("Value type not supported");
        }
    }

    @Override
    public void put(K key, V value, DataSourceCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(K key) {
        V retValue = null;

        String keyString = (String) key;

        try {
            if (converter == null) {
                retValue = (V) storage.getBytes(keyString);
            }else {
                retValue = (V) storage.getString(keyString);
            }
        } catch (UnsupportedOperationException e){

            if (MAS.DEBUG) {
                Log.e(TAG, e.getMessage());
            }
        }

        return retValue;
    }


    @Override
    public void get(K key, DataSourceCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void remove(K key) {
        if (isKeyString(key)) {
            return;
        }

        String keyString = (String) key;

        storage.delete(keyString);
    }

    @Override
    public void remove(K key, DataSourceCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeAll(Object filter) {
        Log.d(TAG,"Escalation MASSecureStorageDataSource removeAll");
        storage.removeAll();
    }

    @Override
    public void removeAll(Object filter, DataSourceCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<K> getKeys(Object filter) {
        return (List<K>) storage.getKeys();
    }

    @Override
    public void getKeys(Object filter, DataSourceCallback callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void unlock() {
        throw new UnsupportedOperationException();
    }

    private boolean isKeyString(Object key) {
        return !(key instanceof String);
    }

    /**
     * Retrieve the Account Name from metadata.
     * <pre>
     *   &lt;meta-data android:name="account.name"
     *             android:resource="@string/acc_name" /&gt;
     * </pre>
     *
     * @return The Account name or "CA MAS Secure" if account name is not defined
     */
    private String getAccountName() {
        ComponentName myService = new ComponentName(context, MASAuthenticatorService.class);
        try {
            Bundle data = context.getPackageManager().getServiceInfo(myService, PackageManager.GET_META_DATA).metaData;
            int resourceId = data.getInt(ACCOUNT_NAME);

            if (resourceId != 0) {
                return context.getResources().getString(resourceId);
            } else {
                return data.getString(ACCOUNT_NAME, CA_MAS);
            }
        } catch (PackageManager.NameNotFoundException e) {
            if (DEBUG)
                Log.d(TAG, String.format("Account name is not provided, use %S", CA_MAS));
        }
        return CA_MAS;
    }
}
