/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

//TODO MultiServer
public interface MASSecurityConfiguration {

    String getHost();
    boolean isPublic();
    boolean isPrimary();
    List<Certificate> getCertificates();
    List<String> getPublicKeyHashes();

    enum PINNING_TYPE {
        certificate,
        publicKeyHash
    }

    class Builder {

        private boolean isPublic;
        private boolean isPrimary;
        private boolean trustPublicPKI;

        private PINNING_TYPE pinningType;
        private List<Certificate> certificates;
        private List<String> publicKeyHashes;

        //The host should contain host and port
        private String host;

        //Only one MASSecurityConfiguration should ever represent the primary gateway
        Builder isPrimary(boolean p) {
            this.isPrimary = p;
            return this;
        }

        public Builder isPublic(boolean p) {
            this.isPublic = p;
            return this;
        }

        public Builder trustPublicPKI(boolean trust) {
            this.trustPublicPKI = trust;
            return this;
        }

        public Builder host(String host) {
            this.host = host;
            return this;
        }

        public Builder add(Certificate certificate) {
            if (certificates == null) {
                certificates = new ArrayList<>();
            }
            certificates.add(certificate);
            return this;
        }

        public Builder add(String publicKeyHash) {
            if (publicKeyHashes == null) {
                publicKeyHashes = new ArrayList<>();
            }
            publicKeyHashes.add(publicKeyHash);
            return this;
        }

        public MASSecurityConfiguration build() {
            if (host == null) {
                throw new IllegalArgumentException("Missing host.");
            }

            if (certificates != null && !certificates.isEmpty()) {
                pinningType = PINNING_TYPE.certificate;
            } else if (publicKeyHashes != null && !publicKeyHashes.isEmpty()) {
                pinningType = PINNING_TYPE.publicKeyHash;
            }

            if (!trustPublicPKI && pinningType == null) {
                throw new IllegalArgumentException("Missing pinning type, cannot establish SSL.");
            }

            return new MASSecurityConfiguration() {
                @Override
                public String getHost() {
                    return host;
                }

                @Override
                public boolean isPublic() {
                    return isPublic;
                }

                @Override
                public boolean isPrimary() {
                    return isPrimary;
                }

                @Override
                public List<Certificate> getCertificates() {
                    return certificates;
                }

                @Override
                public List<String> getPublicKeyHashes() {
                    return publicKeyHashes;
                }
            };
        }
    }
}
