/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.store;

import android.util.Log;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.MssoException;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceException;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.datasource.StringDataConverter;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * Utility class to retrieve the Storage interface.
 */
public class StorageProvider {
    private StorageConfig mStorageConfig;
    private TokenManager tokenManager;
    private OAuthTokenContainer oAuthTokenContainer;
    private ClientCredentialContainer clientCredentialContainer;
    private static StorageProvider instance;
    private static Object mutex = new Object();

    private StorageProvider(ConfigurationProvider configurationProvider) {
        mStorageConfig = new StorageConfig(configurationProvider);
        tokenManager = createTokenManager();
        oAuthTokenContainer = createOAuthTokenContainer();
        clientCredentialContainer = createClientCredentialContainer();
    }

    public static StorageProvider getInstance() {
        if (instance == null) {
            synchronized (mutex) {
                if (instance == null) {
                    if (ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider() == null) {
                        throw new IllegalStateException("ConfigurationManager not initialized.");
                    }
                    instance = new StorageProvider(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider());
                }
            }
        }
        return instance;
    }

    public TokenManager getTokenManager() {
        return tokenManager;
    }

    public OAuthTokenContainer getOAuthTokenContainer() {
        return oAuthTokenContainer;
    }

    public ClientCredentialContainer getClientCredentialContainer() {
        return clientCredentialContainer;
    }

    public void reset() {
        instance = null;
    }

    /**
     * Return the {@link TokenManager} that manage the share tokens and certificate.
     *
     * @return The {@link TokenManager}
     */
    private TokenManager createTokenManager() {
        ConfigurationProvider provider = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider();
        String tm = provider.getProperty(MobileSsoConfig.PROP_SHARE_TOKEN_MANAGER);
        if (tm == null) {
            JSONObject params = new JSONObject();
            try {
                params = new JSONObject(mStorageConfig.getStorageConfig().toString());
                params.put(StorageConfig.PROP_SHARE_STATUS,
                        provider.getProperty(ConfigurationProvider.PROP_SSO_ENABLED) ?
                                Boolean.TRUE :
                                Boolean.FALSE);
            } catch (JSONException e) {
                if (DEBUG) Log.w(TAG, "failed to set sharing property " + e);
            }
            DataSource storage = DataSourceFactory.getStorage(
                    ConfigurationManager.getInstance().getContext(),
                    mStorageConfig.getStorageClass(), params, null);
            return new DefaultTokenManager(storage);
        } else {
            return (TokenManager) create(tm);
        }
    }

    /**
     * Return the {@link OAuthTokenContainer} that manage the private oauth tokens.
     *
     * @return The {@link OAuthTokenContainer}
     */
    private OAuthTokenContainer createOAuthTokenContainer() {
        String pt = ConfigurationManager.getInstance()
                .getConnectedGatewayConfigurationProvider()
                .getProperty(MobileSsoConfig.PROP_PRIVATE_TOKEN_MANAGER);
        if (pt == null) {
            DataSource storage = DataSourceFactory.getStorage(
                    ConfigurationManager.getInstance().getContext(),
                    mStorageConfig.getStorageClass(), mStorageConfig.getStorageConfig(),
                    new StringDataConverter());
            return new PrivateTokenStorage(storage);
        } else {
            return (OAuthTokenContainer) create(pt);
        }
    }

    /**
     * Return the {@link ClientCredentialContainer} that manage the dynamic client id and client credentials.
     *
     * @return The {@link ClientCredentialContainer}
     */
    private ClientCredentialContainer createClientCredentialContainer() {
        String cc = ConfigurationManager.getInstance()
                .getConnectedGatewayConfigurationProvider()
                .getProperty(MobileSsoConfig.PROP_CLIENT_CREDENTIAL_MANAGER);
        if (cc == null) {
            DataSource storage = DataSourceFactory.getStorage(
                    ConfigurationManager.getInstance().getContext()
                    , mStorageConfig.getStorageClass(), mStorageConfig.getStorageConfig()
                    , new StringDataConverter());
            return new ClientCredentialStorage(storage);
        } else {
            return (ClientCredentialContainer) create(cc);
        }
    }

    private Object create(String c) {
        try {
            Object o = Class.forName(c).newInstance();
            return o;
        } catch (Exception e) {
            throw new MssoException(e);
        }
    }

    /**
     * Checks if the Storage provider has a valid store to work with.
     *
     * @return True the storage is ready to use, False when the storage is not ready to use.
     */
    public boolean hasValidStore() {
        DataSource temp = DataSourceFactory.getStorage(
                ConfigurationManager.getInstance().getContext(),
                mStorageConfig.getStorageClass(),
                mStorageConfig.getStorageConfig(),
                new StringDataConverter());
        return temp != null && temp.isReady();
    }

    /**
     * The SDK's Storage Configuration. say
     * {@code
     * "storage": {
     * "class": "<Canonical name of the Storage class>",
     * "share": "true/false"
     * "bootprovider": "com.com.ca.mas.AStorageBootProvider"
     * }
     * }
     * <p/>
     * Responsibility:
     * - Parses the storage configuration from the JSON configuration file
     * - Validates the storage
     * - Falls back to the default, if there is no valid Storage configuration.
     */
    private static class StorageConfig {
        Class storageClass;
        JSONObject storageConfig = new JSONObject();
        /**
         * Common config properties expected for DataSource. Storage specif properties should be defined
         * in the individual DataSource files
         */
        static String PROP_STORAGE_CLASS = "class";
        static final String PROP_SHARE_STATUS = "share";

        StorageConfig(ConfigurationProvider configurationProvider) {

            JSONObject storageJson = configurationProvider.getProperty(MobileSsoConfig.PROP_STORAGE);
            if (storageJson == null) {
                if (DEBUG)
                    Log.d(TAG, "No storage configuration found in JSON config, falling back to DEFAULT ");
                storageClass = KeystoreDataSource.class;
                storageConfig = new JSONObject();
            } else {
                try {
                    storageClass = Class.forName("" + storageJson.get(PROP_STORAGE_CLASS));
                    storageConfig = storageJson;
                } catch (ClassNotFoundException e) {
                    throw new DataSourceException(String.format("Provided Storage configuration %s cannot be found ", storageJson.toString()), e);
                } catch (JSONException e) {
                    throw new DataSourceException("Invalid Storage Config", e);
                }
            }
        }

        Class getStorageClass() {
            return storageClass;
        }

        JSONObject getStorageConfig() {
            return storageConfig;
        }
    }
}
