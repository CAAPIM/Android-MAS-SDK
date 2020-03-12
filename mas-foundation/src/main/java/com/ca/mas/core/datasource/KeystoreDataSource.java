/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.Keep;

import com.ca.mas.core.storage.StorageException;
import com.ca.mas.core.storage.StorageResult;
import com.ca.mas.core.storage.StorageResultReceiver;
import com.ca.mas.core.storage.implementation.KeyStoreStorage;
import com.ca.mas.core.storage.implementation.MASStorageManager;

import org.json.JSONObject;

import java.util.List;

@Keep
public class KeystoreDataSource<K, V> implements DataSource<K, V> {

    public static final String SHARE = "share";

    private KeyStoreStorage storage;
    private DataConverter converter;
    private Context context;
    private boolean isShared;

    public KeystoreDataSource(Context context, JSONObject param, DataConverter converter) {
        this.converter = converter;
        this.context = context;

        try {
            if (param != null) {
                isShared = param.optBoolean(SHARE, false);
            }
            storage = (KeyStoreStorage) new MASStorageManager().getStorage(KeyStoreStorage.class,
                    new Object[]{context, isShared});
        } catch (StorageException e) {
            if (e.getCode() != StorageException.STORE_NOT_UNLOCKED) {
                throw new DataSourceException(e);
            }
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
                if (converter != null) {
                    return (V) converter.convert(key, (byte[]) result.getData());
                }
                return (V) result.getData();
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
                        if (converter != null) {
                            callback.onSuccess(converter.convert(key, (byte[]) result.getData()));
                        } else {
                            callback.onSuccess(result.getData());
                        }
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

        if (storage == null) {
            try {
                storage = (KeyStoreStorage) new MASStorageManager().getStorage(KeyStoreStorage.class,
                        new Object[]{context, isShared});
            } catch (StorageException e) {
                if (e.getCode() == StorageException.STORE_NOT_UNLOCKED) {
                    return false;
                } else {
                    throw new DataSourceException(e);
                }
            }
        }

        StorageResult result = null;
        try {
            result = storage.readString(KeystoreDataSource.class.getCanonicalName());
            if (result.getStatus() != StorageResult.StorageOperationStatus.SUCCESS) {
                if (((StorageException) result.getData()).getCode() != StorageException.STORE_NOT_UNLOCKED) {
                    return true;
                } else {
                    return false;
                }

            }
            throw new IllegalStateException("Should has no value in storage.");
        } catch (StorageException e) {
            throw new DataSourceException(e);
        }
    }

    @Override
    public void unlock() {

        try {
            Intent intent = new Intent("com.android.credentials.UNLOCK");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);

        } catch (Exception e) {
            throw new DataSourceException("Error unlocking KeyStore storage", e);
        }
    }


}
