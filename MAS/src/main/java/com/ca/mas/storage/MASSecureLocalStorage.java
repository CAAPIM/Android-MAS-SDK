/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import android.content.AsyncTaskLoader;
import android.content.Context;
import android.support.annotation.NonNull;

import com.ca.mas.core.BuildConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.datasource.LocalStoreDataSource;
import com.ca.mas.core.datasource.LocalStoreEntity;
import com.ca.mas.core.datasource.LocalStoreKey;
import com.ca.mas.core.util.Functions;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;

import java.util.HashSet;
import java.util.Set;

public class MASSecureLocalStorage extends AbstractMASStorage {

    private static final String TAG = MASSecureLocalStorage.class.getCanonicalName();

    private DataSource<LocalStoreKey, LocalStoreEntity> dataSource;
    private Context context;
    private MASEncryptionProvider encProvider;

    public MASSecureLocalStorage() {
        this(new DefaultEncryptionProvider(MAS.getContext()));
    }

    public MASSecureLocalStorage(MASEncryptionProvider encryptionProvider) {

        this.context = MAS.getContext();

        //Set Default MASEncryptionProvider.
        setEncryptionProvider(encryptionProvider);

        //Register all default DataMarshaller
        setDefaultDataMarshallers();

        //Create/get the LocalStoreDataSource
        dataSource = DataSourceFactory.getStorage(this.context, LocalStoreDataSource.class, null, null);

    }

    public void save(@NonNull final String key, @NonNull final Object value, @MASStorageSegment final int segment, final MASCallback<Void> callback) {

        checkNull(key, value);
        execute(new Functions.UnaryVoid<String>() {
            @Override
            public void call(String s) {
                try {
                    DataMarshaller relevantM = findMarshaller(value);
                    byte[] data;
                    data = relevantM.marshall(value);
                    byte[] encryptedData = data;
                    if (encProvider != null) {
                        encryptedData = encProvider.encrypt(data);
                    }
                    if (segment == MASConstants.MAS_APPLICATION) {
                        s = context.getPackageName();
                    }
                    dataSource.put(new Key(key, segment, s), new LocalStoreEntity(relevantM.getTypeAsString(), encryptedData));
                    Callback.onSuccess(callback, null);
                } catch (Exception e) {
                    Callback.onError(callback, e);
                }
            }
        }, segment, callback);


    }

    @Override
    public void findByKey(@NonNull final String key, @MASStorageSegment final int segment, final MASCallback callback) {

        checkNull(key);
        execute(new Functions.UnaryVoid<String>() {
            @Override
            public void call(String s) {

                try {
                    Object value = null;
                    LocalStoreEntity result = dataSource.get(new Key(key,segment, s));
                    if (result != null) {
                        byte[] decryptedData = result.getData();
                        if (encProvider != null) {
                            decryptedData = encProvider.decrypt(result.getData());
                        }
                        DataMarshaller relevantM = findMarshaller(result.getType());
                        value = relevantM.unmarshall(decryptedData);
                    }
                    Callback.onSuccess(callback, value);
                } catch (Exception e) {
                    Callback.onError(callback, e);
                }
            }
        }, segment, callback);
    }

    @Override
    public void delete(@NonNull final String key, @MASStorageSegment final int segment, final MASCallback<Void> callback) {
        checkNull(key);
        execute(new Functions.UnaryVoid<String>() {
            @Override
            public void call(String s) {
                try {
                    dataSource.remove(new Key(key,segment, s));
                    Callback.onSuccess(callback, null);
                } catch (Exception e) {
                    Callback.onError(callback, e);
                }
            }
        }, segment, callback);
    }

    /**
     * Delete all objects from local storage based on a given MASStorageSegment
     *  @param segment     The MASStorageSegment to be used in the search
     * @param callback The standard (BOOL success, NSError *error) completion block
     */

    public void deleteAll(@MASStorageSegment final int segment, final MASCallback<Void> callback) {
        execute(new Functions.UnaryVoid<String>() {
            @Override
            public void call(String s) {
                try {
                    dataSource.removeAll(new Key(null, segment, s));
                    Callback.onSuccess(callback, null);
                } catch (Exception e) {
                    Callback.onError(callback, e);
                }
            }
        }, segment, callback);
    }

    @Override
    public void keySet(@MASStorageSegment final int segment, final MASCallback<Set<String>> callback) {
        execute(new Functions.UnaryVoid<String>() {
            @Override
            public void call(String s) {
                try {
                    Set<LocalStoreKey> localStoreKeys = new HashSet<LocalStoreKey>(dataSource.getKeys(new Key(null, segment, s)));
                    Set<String> keys = new HashSet<String>();
                    for (LocalStoreKey k : localStoreKeys) {
                        keys.add(k.getKey());
                    }
                    Callback.onSuccess(callback, keys);
                } catch (Exception e) {
                    Callback.onError(callback, e);
                }
            }
        }, segment, callback);
    }

    private void setEncryptionProvider(@NonNull MASEncryptionProvider provider) {
        encProvider = provider;
    }

    private void execute(final Functions.UnaryVoid<String> function, @MASStorageSegment int segment, final MASCallback callback) {
        switch (segment) {
            case MASConstants.MAS_USER:
            case MASConstants.MAS_USER | MASConstants.MAS_APPLICATION:
                execute(function, callback);
                break;
            case MASConstants.MAS_APPLICATION:
                execute(function, (String)null);
                break;
            default:
                throw new IllegalArgumentException("Unsupported storage segment");
        }
    }

    /**
     * Execute with user
     */
    private void execute(final Functions.UnaryVoid<String> function, final MASCallback callback) {
        if (MASUser.getCurrentUser() != null && MASUser.getCurrentUser().getUserName() != null) {
            execute(function, MASUser.getCurrentUser().getUserName());
        } else {
            MASUser.login(new MASCallback<MASUser>() {

                @Override
                public void onSuccess(MASUser result) {
                    execute(function, result.getUserName());
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });

        }
    }

    /**
     * Execute function asynchronously
     */
    private void execute(final Functions.UnaryVoid<String> function, final String username) {
        new AsyncTaskLoader<Void>(context) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                forceLoad();
            }

            @Override
            public Void loadInBackground() {
                function.call(username);
                return null;
            }

        }.startLoading();
    }

    private void setDefaultDataMarshallers() {

        register(new StringDataMarshaller());
        register(new ByteArrayDataMarshaller());
        register(new JsonDataMarshaller());
        register(new BitmapDataMarshaller());
        //Disable, iOS platform doesn't support the following type
        //register(new SerializableDataMarshaller());
        //register(new ParcelDataMarshaller());

    }

    private static class Key extends LocalStoreKey {

        public Key(String key, Integer segment, String createdBy) {
            super(key, segment, createdBy);
        }

        @Override
        public Integer getSegment() {
            switch (super.getSegment()) {
                case MASConstants.MAS_USER:
                case MASConstants.MAS_USER | MASConstants.MAS_APPLICATION:
                    return MASConstants.MAS_USER;
                case MASConstants.MAS_APPLICATION:
                    return MASConstants.MAS_APPLICATION;
                default:
                    throw new IllegalArgumentException("Unsupported storage segment");
            }
        }
    }

}
