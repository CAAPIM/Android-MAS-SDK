package com.ca.mas.core.datasource;
import android.content.ComponentName;
import android.content.Context;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.ca.mas.core.storage.securestoragesource.MASSecureStorageSource;
import com.ca.mas.core.storage.storagesource.MASAuthenticatorService;
import com.ca.mas.foundation.MAS;

import org.json.JSONObject;

import java.util.List;

import static com.ca.mas.core.datasource.KeystoreDataSource.SHARE;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class SecureStorageDataSource<K, V> implements DataSource<K, V>  {

    private static final String LOGTAG = "SecureStorageDataSource";
    private MASSecureStorageSource storage;
    private Context context;

    // - This returning type of the GET method depend on this field
    private DataConverter converter;

    private static final String ACCOUNT_NAME = "account.name";
    private static final String CA_MAS = "CA MAS Secure";

    public SecureStorageDataSource(Context context, JSONObject param, DataConverter converter){

        if (param == null) {
            return;
        }

        boolean shared = param.optBoolean(SHARE, false);
        this.converter = converter;
        this.context = context;

        // - For this DataSource we will always encrypt
        this.storage = new MASSecureStorageSource(getAccountName(), true, shared);
    }

    @Override
    public void put(K key, V value) {
        if (isKeyString(key)) {
            return;
        }

        String keyString = (String) key;

        if (value instanceof byte[]) {
            storage.save(keyString, (byte[]) value);
        } else if (value instanceof String) {
            storage.save(keyString, (String) value);
        } else {
            if (MAS.DEBUG) {
                Log.e(LOGTAG, "Value type not supported");
            }
            throw new UnsupportedOperationException("Value type not supported");
        }
    }

    @Override
    public void put(K key, V value, DataSourceCallback callback) {
        // no implementation
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
                Log.e(LOGTAG, e.getMessage());
            }
        }

        return retValue;
    }


    @Override
    public void get(K key, DataSourceCallback callback) {
        // no implementation
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
        // no implementation
    }

    @Override
    public void removeAll(Object filter) {
        // no implementation
    }

    @Override
    public void removeAll(Object filter, DataSourceCallback callback) {
        // no implementation
    }

    @Override
    public List<K> getKeys(Object filter) {
        // no implementation
        return null;
    }

    @Override
    public void getKeys(Object filter, DataSourceCallback callback) {
        // no implementation
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void unlock() {
        // no implementation
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
