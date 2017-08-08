/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.net.Uri;

import java.net.URL;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

public interface MASSecurityConfiguration {

    Uri getHost();
    boolean isPublic();
    PINNING_TYPE getPinningType();
    List<Certificate> getCertificates();
    List<String> getPublicKeyHashes();

    enum PINNING_TYPE {
        certificate,
        publicKeyHash
    }

    class Builder {

        private boolean isPublic;
        private boolean trustPublicPKI;

        private PINNING_TYPE pinningType;
        private List<Certificate> certificates;
        private List<String> publicKeyHashes;

        private Uri host;

        public Builder isPublic(boolean p) {
            this.isPublic = p;
            return this;
        }

        public Builder trustPublicPKI(boolean trust) {
            this.trustPublicPKI = trust;
            return this;
        }

        public Builder host(Uri host) {
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
                public PINNING_TYPE getPinningType() {
                    return pinningType;
                }
            };
        }
    }
}
