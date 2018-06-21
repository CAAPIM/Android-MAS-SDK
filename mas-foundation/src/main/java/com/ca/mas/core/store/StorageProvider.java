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
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceException;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.datasource.KeystoreDataSource;
import com.ca.mas.core.datasource.StringDataConverter;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Utility class to retrieve the Storage interface.
 */
public class StorageProvider {
    private StorageConfig mStorageConfig;
    private TokenManager tokenManager;
    private OAuthTokenContainer oAuthTokenContainer;
    private ClientCredentialContainer clientCredentialContainer;
    private static StorageProvider instance;

    private StorageProvider(ConfigurationProvider configurationProvider) {
        mStorageConfig = new StorageConfig(configurationProvider);
        tokenManager = createTokenManager();
        oAuthTokenContainer = createOAuthTokenContainer();
        clientCredentialContainer = createClientCredentialContainer();
    }

    public static synchronized StorageProvider getInstance() {
        if (instance == null) {
            if (ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider() == null) {
                throw new IllegalStateException("ConfigurationManager not initialized.");
            }
            instance = new StorageProvider(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider());
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
        JSONObject params = new JSONObject();
        try {
            params = new JSONObject(mStorageConfig.getConfig().toString());
            params.put(StorageConfig.PROP_SHARE_STATUS,
                    provider.getProperty(ConfigurationProvider.PROP_SSO_ENABLED));
        } catch (JSONException e) {
            if (DEBUG) Log.w(TAG, "failed to set sharing property " + e);
        }
        DataSource storage = DataSourceFactory.getStorage(
                ConfigurationManager.getInstance().getContext(),
                mStorageConfig.getStorageClass(), params, null);
        return new DefaultTokenManager(storage);
    }

    /**
     * Return the {@link OAuthTokenContainer} that manage the private oauth tokens.
     *
     * @return The {@link OAuthTokenContainer}
     */
    private OAuthTokenContainer createOAuthTokenContainer() {
        DataSource storage = DataSourceFactory.getStorage(
                ConfigurationManager.getInstance().getContext(),
                mStorageConfig.getStorageClass(), mStorageConfig.getConfig(),
                new StringDataConverter());
        return new PrivateTokenStorage(storage);
    }

    /**
     * Return the {@link ClientCredentialContainer} that manage the dynamic client id and client credentials.
     *
     * @return The {@link ClientCredentialContainer}
     */
    private ClientCredentialContainer createClientCredentialContainer() {
        DataSource storage = DataSourceFactory.getStorage(
                ConfigurationManager.getInstance().getContext()
                , mStorageConfig.getStorageClass(), mStorageConfig.getConfig()
                , new StringDataConverter());
        return new ClientCredentialStorage(storage);
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
                mStorageConfig.getConfig(),
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
        JSONObject config;
        /**
         * Common config properties expected for DataSource. Storage specif properties should be defined
         * in the individual DataSource files
         */
        static final String PROP_STORAGE_CLASS = "class";
        static final String PROP_SHARE_STATUS = "share";

        StorageConfig(ConfigurationProvider configurationProvider) {

            JSONObject storageJson = configurationProvider.getProperty(MobileSsoConfig.PROP_STORAGE);
            if (storageJson == null) {
                if (DEBUG)
                    Log.d(TAG, "No storage configuration found in JSON config, falling back to DEFAULT ");
                storageClass = KeystoreDataSource.class;
                config = new JSONObject();
            } else {
                try {
                    storageClass = Class.forName("" + storageJson.get(PROP_STORAGE_CLASS));
                    config = storageJson;
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

        JSONObject getConfig() {
            return config;
        }
    }
}
