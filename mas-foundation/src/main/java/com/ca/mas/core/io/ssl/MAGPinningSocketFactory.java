/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.io.ssl;

import android.util.Base64;

import com.ca.mas.core.cert.PublicKeyHash;

import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MAGPinningSocketFactory {

    private static final String SSL_TLS_PROTOCOL = "TLS";

    private PublicKeyHash publicKeyHash;

    public MAGPinningSocketFactory(String publicKeyHash) {
        this.publicKeyHash = PublicKeyHash.fromHashString(publicKeyHash, Base64.URL_SAFE);
    }

    public SSLSocketFactory createTLSSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSL_TLS_PROTOCOL);
            TrustManager[] trustManagers = {new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    throw new CertificateException("This trust manager is only for clients");
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    boolean sawPin = false;
                    for (X509Certificate cert : chain) {
                        if (publicKeyHash.matches(cert)) {
                            sawPin = true;
                            break;
                        }
                    }
                    if (!sawPin)
                        throw new CertificateException("Server certificate chain did not contain any of the pinned public keys");
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }
            }};
            sslContext.init(new KeyManager[0], trustManagers, new SecureRandom());
            return new TLSSocketFactory(sslContext.getSocketFactory());

        } catch (Exception e) {
            throw new RuntimeException("Unable to create SSL Context: " + e.getMessage(), e);
        }
    }
}
