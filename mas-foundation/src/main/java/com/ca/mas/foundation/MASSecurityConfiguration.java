/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.net.Uri;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;
/**
 * MASSecurityConfiguration class is an object that determines security measures for communication between the target host.
 * The class is mainly responsible for the SSL pinning mechanism, as well as for including/excluding credentials from the primary gateway in the network communication to the target host.
 * If trustPublicPKI is false and no pinning information is set (i.e. no certificates or public key hashes), the connection will be rejected due to a lack of security measures.
 */
public interface MASSecurityConfiguration {

    Uri getHost();
    boolean isPublic();
    boolean trustPublicPki();
    boolean allowSSLPinning();
    List<Certificate> getCertificates();
    List<String> getPublicKeyHashes();

    class Builder {

        private boolean isPublic;
        private boolean allowSSLPinning = true;
        private boolean trustPublicPKI;

        private List<Certificate> certificates;
        private List<String> publicKeyHashes;

        private Uri host;

        /**
         * Determines whether or not to include sensitive credentials from primary gateway in the network communication with the target host.
         * @param p to include or not
         * @return the builder object
         */
        public Builder isPublic(boolean p) {
            this.isPublic = p;
            return this;
        }

        /**
         * Determines whether or not to validate the server trust against Android's trusted root certificates.
         * @param trust to trust or not
         * @return the builder object
         */
        public Builder trustPublicPKI(boolean trust) {
            this.trustPublicPKI = trust;
            return this;
        }

        /**
         * Determines whether or not to enable ssl pinning to primary gateway.
         * @param sslPinning to include or not
         * @return the builder object
         */
        public Builder allowSSLPinning(boolean sslPinning) {
            this.allowSSLPinning = sslPinning;
            return this;
        }

        /**
         * The URI of the designated host.
         * @param host URI for the host
         * @return the builder object
         */
        public Builder host(Uri host) {
            this.host = new Uri.Builder()
                    .encodedAuthority(host.getHost() + ":" + host.getPort())
                    .build();
            return this;
        }

        /**
         * Adds a certificate to the security configuration.
         * @param certificate the certificate to be added
         * @return the builder object
         */
        public Builder add(Certificate certificate) {
            if (certificates == null) {
                certificates = new ArrayList<>();
            }
            certificates.add(certificate);
            return this;
        }

        /**
         * Adds a public key hash to the security configuration.
         * @param publicKeyHash the hash to be added
         * @return the builder object
         */
        public Builder add(String publicKeyHash) {
            if (publicKeyHashes == null) {
                publicKeyHashes = new ArrayList<>();
            }
            publicKeyHashes.add(publicKeyHash);
            return this;
        }

        /**
         * Constructs a MASSecurityConfiguration object from the builder.
         * @return the MASSecurityConfiguration object
         */
        public MASSecurityConfiguration build() {
            if (host == null) {
                throw new IllegalArgumentException("Missing host.");
            }

            // If trustPublicPKI is false and no pinning information is found, throw an exception.
            if (allowSSLPinning && !trustPublicPKI && publicKeyHashes == null && certificates == null) {
                throw new IllegalArgumentException("Missing pinning type, cannot establish SSL.");
            }

            return new MASSecurityConfiguration() {
                @Override
                public Uri getHost() {
                    return host;
                }

                @Override
                public boolean isPublic() {
                    return isPublic;
                }

                @Override
                public List<Certificate> getCertificates() {
                    return certificates;
                }

                @Override
                public List<String> getPublicKeyHashes() {
                    return publicKeyHashes;
                }

                @Override
                public boolean trustPublicPki() {
                    return trustPublicPKI;
                }

                @Override
                public boolean allowSSLPinning() {
                    return allowSSLPinning;
                }
            };
        }
    }
}