/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.cert;

import java.security.cert.X509Certificate;
import java.util.Collection;

/**
 * Interface that describes trusted TLS server certs (including self-signed certs and public PKI, with or without
 * public key pinning).
 */
public interface TrustedCertificateConfiguration {
    /**
     * Get the server certificate trust anchors that should be trusted for outbound TLS.
     * <p/>
     * If {@link #isAlsoTrustPublicPki()} returns false, then only TLS server certs on this list
     * (or directly or indirectly signed by certs on this list) will be trusted for outbound TLS.
     *
     * @return a collection of certificates trusted as
     */
    Collection<X509Certificate> getTrustedCertificateAnchors();

    /**
     * Check whether public CAs recognized by the OS should be accepted as TLS server certs in addition
     * to the list returned by {@link #getTrustedCertificateAnchors()}.
     *
     * @return true if public CAs known to the OS should be trusted for outbound TLS.
     *         false if only the certs returned by {@link #getTrustedCertificateAnchors()} should be trusted.
     */
    boolean isAlsoTrustPublicPki();

    /**
     * Get server certificate pinned public keys.
     * <p/>
     * This is only really useful if {@link #isAlsoTrustPublicPki()} returns true.  If you don't trust public
     * PKI certs and are relying on internal PKI or self-signed certificates built into the app then you probably
     * do not need to worry about certificate pinning.
     * <p/>
     * If this list is nonempty then a TLS server cert will be accepted only if one of the pinned public keys
     * appears somewhere in its certificate chain.
     * <p/>
     * This prevents CAs -- any public CA, not necessarily the CA that signed the server cert originally -- from being
     * able to create a forged server certificate for an arbitrary target domain.
     *
     * @return a list of pinned server certificate public keys, or empty (or null) if pinning should not be used.
     */
    Collection<PublicKeyHash> getTrustedCertificatePinnedPublicKeyHashes();
}
