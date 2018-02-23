/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.store;

import android.content.Context;

import com.ca.mas.core.security.KeyStoreException;
import com.ca.mas.core.token.IdToken;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

/**
 * Interface implemented by a persistent token store that provides read-only access to tokens, certs and keys.
 */
public interface TokenProvider {


    /**
     * Get the persistent client username, if available.
     *
     * @return the user profile, or null if a user profile has not been saved.
     */
    String getUserProfile();

    /**
     * Get the device identifier assigned to this device by the token server when the device was registered, or null.
     * <p/>
     * This is a Base-64 encoded string whose contents are opaque to the client.
     *
     * @return the device identifier, or null if a device identifier has not been saved.
     */
    String getMagIdentifier();

    /**
     * Get the persisted client private key, if available.
     * <p/>
     * A key pair may become available in the persistent store some time before an actual certificate chain
     * becomes available if the client needs to wait for the server to approve a certificate signing request.
     *
     * @return the client key pair, or null if a client key pair has not yet been saved.
     */
    PrivateKey getClientPrivateKey();

    /**
     * Get the persisted client public key, if available.
     * <p/>
     * A key pair may become available in the persistent store some time before an actual certificate chain
     * becomes available if the client needs to wait for the server to approve a certificate signing request.
     *
     * @return the client key pair, or null if a client key pair has not yet been saved.
     */
    PublicKey getClientPublicKey();

    /**
     * Create an RSA private key.
     * <p/>
     * This will create a private key with a self-signed public key.
     *
     * @param ctx Android Context
     * @param keyBits the size of the key, 2048 default
     * @return the client key pair, or null if a client key pair has not yet been saved.
     */
    PrivateKey createPrivateKey(Context ctx, int keyBits) throws KeyStoreException;


    /**
     * Quickly check if a persisted client cert chain is available without actually reading and instantiating it.
     *
     * @return true if and only if getClientCertificateChain() would have returned non-null.
     */
    boolean isClientCertificateChainAvailable();

    /**
     * Get the persisted client cert chain, if available.
     * <p/>
     * A certificate chain may not be available even though a client key pair is available.  For example,
     * the server may not yet have approved the client's certificate signing request.
     *
     * @return the client cert chain, with the client cert itself in the zeroth position; or null
     *         if a client cert chain has not yet been obtained or saved.
     */
    X509Certificate[] getClientCertificateChain();

    /**
     * Get the persisted OAuth ID token, if available.
     * <p/>
     * As far as this SDK is concerned this is just a long opaque string that is meaningful to the server.
     * It happens to be Base64-encoded JSON but we as the client will never need to parse it.
     *
     * @return the ID token, or null if one has not yet been saved.
     */
    IdToken getIdToken();

    /**
     * Get the encrypted OAuth ID token, if available.
     *
     * @return the encrypted ID token, or null if one has not yet been saved.
     */
    byte[] getSecureIdToken();

}
