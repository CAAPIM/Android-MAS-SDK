/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.store;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.io.Charsets;
import com.ca.mas.core.util.KeyUtils;
import com.ca.mas.core.token.IdToken;

import java.security.KeyPair;
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
    private static final String MSSO_CLIENT_CERT_PRIVATE_KEY = "msso.clientCertPrivateKey";
    private static final String MSSO_CLIENT_CERT_PUBLIC_KEY = "msso.clientCertPublicKey";
    private static final String MSSO_CLIENT_CERT_CHAIN = "msso.clientCertChain";
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
    public void saveClientKeyPair(KeyPair keyPair) throws TokenStoreException {
        storeSecureItem(MSSO_CLIENT_CERT_PRIVATE_KEY, KeyUtils.encodeRsaPrivateKey(keyPair.getPrivate()));
        storeSecureItem(MSSO_CLIENT_CERT_PUBLIC_KEY, KeyUtils.encodeRsaPublicKey(keyPair.getPublic()));
    }

    @Override
    public void saveClientCertificateChain(X509Certificate[] chain) throws TokenStoreException {
        storeSecureItem(MSSO_CLIENT_CERT_CHAIN, CertUtils.encodeCertificateChain(chain));
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
        deleteSecureItem(MSSO_CLIENT_CERT_PRIVATE_KEY);
        deleteSecureItem(MSSO_CLIENT_CERT_PUBLIC_KEY);
        deleteSecureItem(MSSO_CLIENT_CERT_CHAIN);
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
    public KeyPair getClientKeyPair() {
        try {
            byte[] publicBytes = retrieveSecureItem(MSSO_CLIENT_CERT_PUBLIC_KEY);
            if (publicBytes == null)
                return null;
            byte[] privateBytes = retrieveSecureItem(MSSO_CLIENT_CERT_PRIVATE_KEY);
            if (privateBytes == null)
                return null;

            PublicKey publicKey = KeyUtils.decodeRsaPublicKey(publicBytes);
            PrivateKey privateKey = KeyUtils.decodeRsaPrivateKey(privateBytes);
            return new KeyPair(publicKey, privateKey);
        } catch (IllegalArgumentException e) {
            if (DEBUG) Log.e(TAG, "Unable to decode client cert key pair: " + e.getMessage(), e);
            return null;
        } catch (TokenStoreException e) {
            if (DEBUG) Log.e(TAG, "Unable to access client cert key pair: " + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean isClientCertificateChainAvailable() {
        try {
            return retrieveSecureItem(MSSO_CLIENT_CERT_CHAIN) != null;
            //return storage.getKeys().contains(getKey(MSSO_CLIENT_CERT_CHAIN));
        } catch (TokenStoreException e) {
            if (DEBUG) Log.e(TAG, "Unable to access client cert chain: " + e.getMessage(), e);
            return false;
        }
    }

    @Override
    public X509Certificate[] getClientCertificateChain() {
        try {
            byte[] bytes = retrieveSecureItem(MSSO_CLIENT_CERT_CHAIN);
            if (bytes == null)
                return null;

            return CertUtils.decodeCertificateChain(bytes);
        } catch (TokenStoreException e) {
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
