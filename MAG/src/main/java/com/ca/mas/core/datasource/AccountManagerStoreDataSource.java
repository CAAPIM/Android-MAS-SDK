/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

import android.content.Context;

import com.ca.mas.core.storage.StorageException;
import com.ca.mas.core.storage.StorageResult;
import com.ca.mas.core.storage.StorageResultReceiver;
import com.ca.mas.core.storage.implementation.AccountManagerStorage;
import com.ca.mas.core.storage.implementation.MASStorageManager;

import org.json.JSONObject;

import java.util.List;

/**
 * SDK uses this utility wrapper class to use {@link com.ca.mas.core.storage.implementation.AccountManagerStorage}.
 */
public class AccountManagerStoreDataSource<K, V> implements DataSource<K, V> {

    public static final String TAG = AccountManagerStoreDataSource.class.getCanonicalName();
    public static final String SHARE = "share";

    private AccountManagerStorage storage;
    private DataConverter converter;
    private Context context;
    private boolean share;


    public AccountManagerStoreDataSource(Context context, JSONObject param, DataConverter converter) {
        this.converter = converter;
        this.context = context;

        try {
            if (param != null) {
                share = param.optBoolean(SHARE, false);
            }

            storage = (AccountManagerStorage) new MASStorageManager().getStorage(AccountManagerStorage.class,
                    new Object[]{context, share});
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void put(K key, V value) {
        try {
            StorageResult result = null;
            if (value instanceof byte[]) {
                result = storage.writeOrUpdateData((String) key, (byte[]) value);
            } else if (value instanceof String) {
                result = storage.writeOrUpdateString((String) key, (String) value);
            } else if (value == null) {
                remove(key);
                return;
            } else {
                throw new UnsupportedOperationException("Value type not supported");
            }
            if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                throw (StorageException) result.getData();
            }
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void put(K key, V value, final DataSourceCallback callback) {
        try {
            if (value instanceof byte[]) {
                storage.writeOrUpdateData((String) key, (byte[]) value, new StorageResultReceiver(callback.getHandler()) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                            callback.onError(new DataSourceError(((StorageException) result.getData())));
                        } else {
                            callback.onSuccess(null);
                        }
                    }
                });
            } else if (value instanceof String) {
                storage.writeOrUpdateString((String) key, (String) value, new StorageResultReceiver(callback.getHandler()) {
                    @Override
                    public void onReceiveResult(StorageResult result) {
                        if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                            callback.onError(new DataSourceError(((StorageException) result.getData())));
                        } else {
                            callback.onSuccess(null);
                        }
                    }
                });
            } else {
                throw new UnsupportedOperationException("Value type not supported");
            }
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }


    }

    @Override
    public V get(K key) {
        try {
            StorageResult result = null;
            result = storage.readData((String) key);
            if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                StorageException exception = (StorageException) result.getData();
                if (exception.getCode() == StorageException.READ_DATA_NOT_FOUND) {
                    return null;
                } else {
                    throw exception;
                }
            } else {
                return getData(key, result);
            }
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void get(final K key, final DataSourceCallback callback) {

        try {
            storage.readData((String) key, new StorageResultReceiver(callback.getHandler()) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                        callback.onError(new DataSourceError(((StorageException) result.getData())));
                    } else {
                        V data = getData(key, result);
                        callback.onSuccess(data);
                    }
                }
            });
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void remove(K key) {
        try {
            StorageResult result = storage.deleteData((String) key);
            if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                if (((StorageException) result.getData()).getCode() != StorageException.READ_DATA_NOT_FOUND) {
                    throw (StorageException) result.getData();
                }
            }
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void remove(K key, final DataSourceCallback callback) {
        try {
            storage.deleteData((String) key, new StorageResultReceiver(callback.getHandler()) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                        callback.onError(new DataSourceError(((StorageException) result.getData())));
                    } else {
                        callback.onSuccess(null);
                    }
                }
            });
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }


    }

    @Override
    public void removeAll(Object filter) {
        try {
            StorageResult result = storage.deleteAll();
            if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                if (((StorageException) result.getData()).getCode() != StorageException.READ_DATA_NOT_FOUND) {
                    throw (StorageException) result.getData();
                }
            }
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void removeAll(Object filter, final DataSourceCallback callback) {
        try {
            storage.deleteAll(new StorageResultReceiver(callback.getHandler()) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                        callback.onError(new DataSourceError(((StorageException) result.getData())));
                    } else {
                        callback.onSuccess(null);
                    }
                }
            });
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public List<K> getKeys(Object filter) {
        StorageResult result = storage.getAllKeys();
        if (result.getStatus() == StorageResult.StorageOperationStatus.SUCCESS) {
            return ((List) result.getData());
        } else {
            throw new DataSourceException((Throwable) result.getData());
        }
    }

    @Override
    public void getKeys(Object filter, final DataSourceCallback callback) {
        try {
            storage.getAllKeys(new StorageResultReceiver(callback.getHandler()) {
                @Override
                public void onReceiveResult(StorageResult result) {
                    if (result.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                        callback.onError(new DataSourceError(((StorageException) result.getData())));
                    } else {
                        callback.onSuccess(result.getData());
                    }
                }
            });
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public boolean isReady() {
        return true;
    }

    @Override
    public void unlock() {
        //nothing to do
    }

    protected V getData(K key, StorageResult result) {
        if (converter != null) {
            return (V) converter.convert(key, (byte[]) result.getData());
        }
        return (V) result.getData();
    }

    protected DataConverter getConverter() {
        return converter;
    }
}
