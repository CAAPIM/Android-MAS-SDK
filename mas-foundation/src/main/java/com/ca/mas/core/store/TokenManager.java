/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.store;

import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.token.IdToken;

import java.security.cert.X509Certificate;

/**
 * Interface implemented by a persistent token store that provides read and write access to tokens, certs, and keys.
 */
public interface TokenManager extends TokenProvider {

    /**
     * Save the specified client username to the persistent store.
     *
     * @param userProfile the user profile to save.  Required.
     * @throws TokenStoreException if there is a problem saving to the persistent store
     */
    void saveUserProfile(String userProfile) throws TokenStoreException;

    /**
     * Save the specified server-assigned device identifier string.
     * <p/>
     * The identifier is assigned by the token server when we register the device.
     * It is expected to be a base-64 encoded token whose contents are opaque to the client.
     *
     * @param deviceIdentifier a device identifier assigned to this device by the token server upon registration.  Required.
     * @throws TokenStoreException if there is a problem saving to the persistent store
     */
    void saveMagIdentifier(String deviceIdentifier) throws TokenStoreException;

    /**
     * Save the specified certificate chain to the persistent store.
     * <p/>
     * The chain must be non-empty and the certificate in the zeroth index
     * must have a public key which matches the saved client public key.
     *
     * @param chain the new client certificate chain.  Required.
     * @throws TokenStoreException if there is a problem saving to the persistent store
     */
    void saveClientCertificateChain(X509Certificate[] chain) throws TokenStoreException;

    /**
     * Save the specified ID token to the persistent store.
     * <p/>
     * Any existing ID token in the store will be replaced.
     *
     * @param idToken the new ID token.  Required.
     * @throws TokenStoreException if there is a problem saving to the persistent store
     */
    void saveIdToken(IdToken idToken) throws TokenStoreException;

    /**
     * Delete any saved ID token from the persistent store.
     *
     * @throws TokenStoreException if there is a problem deleting from the persistent store
     */
    void deleteIdToken() throws TokenStoreException;

    /**
     * Save the specified encrypted ID token to the persistent store.
     * <p/>
     * Any existing ID token in the store will be replaced.
     *
     * @param idToken the new ID token.  Required.
     * @throws TokenStoreException if there is a problem saving to the persistent store
     */
    void saveSecureIdToken(byte[] idToken) throws TokenStoreException;

    /**
     * Delete any saved encrypted ID token from the persistent store.
     *
     * @throws TokenStoreException if there is a problem deleting from the persistent store
     */
    void deleteSecureIdToken() throws TokenStoreException;

    /**
     * Delete any saved user profile from the persistent store.
     *
     * @throws TokenStoreException if there is a problem deleting from the persistent store
     */
    void deleteUserProfile() throws TokenStoreException;


    /**
     * Delete shared objects from the persistent store which associate withe the current connected gateway.
     * This includes any ID token, client cert public and private key, and client cert chain.
     *
     * @throws TokenStoreException if there is a problem deleting from the persistent store
     */
    void clear() throws TokenStoreException;

    /**
     * Delete all shared objects from the persistent store.
     * This includes any ID token, client cert public and private key, and client cert chain.
     *
     * @throws TokenStoreException if there is a problem deleting from the persistent store
     */
    void clearAll() throws TokenStoreException;

    /**
     * Check that the token store is unlocked and ready to use.
     *
     * @return true if the token store is ready to use, and the various save methods should be expected to succeed.
     *         false if the token store needs to be initialized (pass code set, device unlocked, etc)
     */
    boolean isTokenStoreReady();

    /**
     * Gets the DataSource used to store the tokens.
     * @return Token Store as DataSource.
     */
    DataSource getTokenStore();
}
