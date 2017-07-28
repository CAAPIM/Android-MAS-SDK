/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import java.security.cert.Certificate;
import java.util.List;

//TODO MultiServer
public interface MASSecurityConfiguration {

    String getHost();
    boolean isProtected();
    List<Certificate> getCertificates();
    List<String> getPublicKeyHashes();


    class Builder {

        private boolean isPublic = false;

        //The host should contain host and port
        private String host;

        Builder isPublic(boolean p) {
            this.isPublic = p;
            return this;
        }

        Builder host(String host) {
            this.host = host;
            return this;
        }

        Builder add(Certificate certificate) {
            return this;
        }

        Builder add(String publicKeyHash) {
            return this;
        }


        MASSecurityConfiguration build() {
            //Make sure Host is defined.
            if (host == null) {
                throw new IllegalArgumentException("");
            }

            //If trustPublicPKI == false && no pinning defined throw IllegalArgumentException

            return new MASSecurityConfiguration() {
                @Override
                public String getHost() {
                    return host;
                }

                @Override
                public boolean isProtected() {
                    return isPublic;
                }

                @Override
                public List<Certificate> getCertificates() {
                    return null;
                }

                @Override
                public List<String> getPublicKeyHashes() {
                    return null;
                }
            };
        }
    }
}
