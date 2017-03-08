/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.store;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.datasource.AccountManagerStoreDataSource;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.io.Charsets;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.util.KeyUtils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * A simple persistent token store that uses the system's key store daemon to store the objects.
 * <p/>
 * Within the key store, the objects will be visible to any other app that runs as the same OS-level UID
 * as the current app.
 * <p/>
 * Before the token manager can be used the {@link #isTokenStoreReady()} ()} method must return true.
 * If it doesn't, it may be necessary to start the android.credentials.UNLOCK intent to give the user a
 * chance to set an unlock code and/or unlock the device.
 */
public class DefaultTokenManager implements TokenManager {

    private static final String MSSO_USER_PROFILE = "msso.userProfile";
    private static final String MSSO_MAG_IDENTIFIER = "msso.magIdentifier";
    private static final String MSSO_CLIENT_PRIVATE_KEY = "msso.clientCertPrivateKey";
    private static final String MSSO_CLIENT_CERT_CHAIN_PREFIX = "msso.clientCertChain_";
    private static final String MSSO_DN = "cn=msso";
    private static final String MSSO_ID_TOKEN = "msso.idToken";
    private static final String MSSO_ID_TOKEN_TYPE = "msso.idTokenType";
    private static final String MSSO_SECURE_ID_TOKEN = "msso.secureIdToken";
    protected DataSource<String, byte[]> storage;

    public DefaultTokenManager(@NonNull DataSource storage) {
        this.storage = storage;
    }

    @Override
    public void saveUserProfile(String userProfile) throws TokenStoreException {
        storeSecureItem(MSSO_USER_PROFILE, userProfile.getBytes(Charsets.UTF8));
    }

    @Override
    public void saveMagIdentifier(String deviceIdentifier) throws TokenStoreException {
        storeSecureItem(MSSO_MAG_IDENTIFIER, deviceIdentifier.getBytes(Charsets.UTF8));
    }

    @Override
    public void saveClientCertificateChain(X509Certificate[] chain) throws TokenStoreException {
        try {
            KeyUtils.setCertificateChain(MSSO_CLIENT_CERT_CHAIN_PREFIX, chain);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable to save client certificate chain: " + e.getMessage(), e);
        }
    }

    @Override
    public void saveIdToken(IdToken idToken) throws TokenStoreException {
        storeSecureItem(MSSO_ID_TOKEN, idToken.getValue().getBytes(Charsets.UTF8));
        storeSecureItem(MSSO_ID_TOKEN_TYPE, idToken.getType().getBytes(Charsets.UTF8));
    }

    @Override
    public void deleteIdToken() throws TokenStoreException {
        deleteSecureItem(MSSO_ID_TOKEN);
        deleteSecureItem(MSSO_ID_TOKEN_TYPE);
    }

    @Override
    public void saveSecureIdToken(byte[] idToken) throws TokenStoreException {
        storeSecureItem(MSSO_SECURE_ID_TOKEN, idToken);
    }

    @Override
    public void deleteSecureIdToken() throws TokenStoreException {
        deleteSecureItem(MSSO_SECURE_ID_TOKEN);
    }

    @Override
    public void deleteUserProfile() throws TokenStoreException {
        deleteSecureItem(MSSO_USER_PROFILE);
    }

    @Override
    public void clear() throws TokenStoreException {
        deleteIdToken();
        deleteUserProfile();
        KeyUtils.deletePrivateKey(MSSO_CLIENT_PRIVATE_KEY);
        KeyUtils.clearCertificateChain(MSSO_CLIENT_CERT_CHAIN_PREFIX);
        deleteSecureItem(MSSO_MAG_IDENTIFIER);
    }

    @Override
    public void clearAll() throws TokenStoreException {
        storage.removeAll(null);
    }

    @Override
    public boolean isTokenStoreReady() {
        return storage.isReady();
    }

    public DataSource getTokenStore() {
        return storage;
    }

    @Override
    public String getUserProfile() {
        try {
            byte[] userProfileBytes = retrieveSecureItem(MSSO_USER_PROFILE);
            if (userProfileBytes == null)
                return null;
            return new String(userProfileBytes, Charsets.UTF8);
        } catch (TokenStoreException e) {
            if (DEBUG) Log.e(TAG, "Unable to access client username: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getMagIdentifier() {
        try {
            byte[] identBytes = retrieveSecureItem(MSSO_MAG_IDENTIFIER);
            if (identBytes == null)
                return null;
            return new String(identBytes, Charsets.UTF8);
        } catch (TokenStoreException e) {
            if (DEBUG) Log.e(TAG, "Unable to access client device identifier: " + e.getMessage(), e);
            return null;
        }
    }


    @Override
    public PrivateKey createPrivateKey(Context ctx, int keyBits)
    {
        try {
            if (storage instanceof AccountManagerStoreDataSource) {

                // don't require a pin/password/swipe
                return KeyUtils.generateRsaPrivateKey(ctx, keyBits, MSSO_CLIENT_PRIVATE_KEY,
                     MSSO_DN, false, false, 100000, false);

            } else {

                // for pre-marshmallow devices, require a pin/password/swipe
                //    which will encrypt the keys at rest
                // otherwise, the keys are already protected from extraction and use
                //    except by apps with same signing key + shared user id
                return KeyUtils.generateRsaPrivateKey(ctx, keyBits, MSSO_CLIENT_PRIVATE_KEY,
                        MSSO_DN, true, false, 100000, false);
            }

        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable to create client private key: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public PrivateKey getClientPrivateKey() {
        try {
            return KeyUtils.getRsaPrivateKey(MSSO_CLIENT_PRIVATE_KEY);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable to get client private key: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public PublicKey getClientPublicKey() {
        try {
            return  KeyUtils.getRsaPublicKey(MSSO_CLIENT_PRIVATE_KEY);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable to get client public key: " + e.getMessage(), e);
            return null;
        }
    }


    @Override
    public boolean isClientCertificateChainAvailable() {
        try {
            return KeyUtils.getCertificateChain(MSSO_CLIENT_CERT_CHAIN_PREFIX) != null;
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable to access client cert chain: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public X509Certificate[] getClientCertificateChain() {
        try {
            return KeyUtils.getCertificateChain(MSSO_CLIENT_CERT_CHAIN_PREFIX);
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Unable to access client cert chain: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public IdToken getIdToken() {
        try {
            byte[] bytes = retrieveSecureItem(MSSO_ID_TOKEN);
            if (bytes == null)
                return null;

            String idToken = new String(bytes, Charsets.UTF8);

            bytes = retrieveSecureItem(MSSO_ID_TOKEN_TYPE);
            String idTokenType = null;
            if (bytes != null) {
                idTokenType = new String(bytes, Charsets.UTF8);
            }

            return new IdToken(idToken, idTokenType);
        } catch (TokenStoreException e) {
            if (DEBUG) Log.e(TAG, "Unable to access ID token: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public byte[] getSecureIdToken() {
        try {
            return retrieveSecureItem(MSSO_SECURE_ID_TOKEN);
        } catch (TokenStoreException e) {
            if (DEBUG) Log.e(TAG, "Unable to retrieve encrypted ID token: " + e.getMessage(), e);
            return null;
        }
    }

    void deleteSecureItem(String name) throws TokenStoreException {
        try {
            storage.remove(getKey(name));
        } catch (Exception e) {
            throw new TokenStoreException(e);
        }
    }

    void storeSecureItem(String name, byte[] item) throws TokenStoreException {
        try {
            storage.put(getKey(name), item);
        } catch (Exception e) {
            throw new TokenStoreException(e);
        }
    }

    byte[] retrieveSecureItem(String name) throws TokenStoreException {
        try {
            return storage.get(getKey(name));
        } catch (Exception e) {
            throw new TokenStoreException(e);
        }
    }

    private String getKey(String name) {
        return ConfigurationManager.getInstance().getConnectedGateway().toString() + name;
    }
}
