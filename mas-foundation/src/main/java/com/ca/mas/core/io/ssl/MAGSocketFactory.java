/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.io.ssl;

import android.net.Uri;

import com.ca.mas.core.io.http.SingleKeyX509KeyManager;
import com.ca.mas.core.io.http.TrustedCertificateConfigurationTrustManager;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASSecurityConfiguration;

import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class MAGSocketFactory {

    private static final String SSL_TLS_PROTOCOL = "TLS";

    private static final SecureRandom secureRandom = new SecureRandom();
    private Map<Uri, MASSecurityConfiguration> configs;
    private PrivateKey clientCertPrivateKey = null;
    private X509Certificate[] clientCertChain = null;

    /**
     * Create an SocketFactory factory that will create clients that trust the specified server certs and that use the specified client cert
     * for client cert authentication.
     * <p>
     * it retrieves the trustConfig, clientCertPrivateKey
     * and clientCertChain from the {@link com.ca.mas.core.store.TokenManager}
     */

    public MAGSocketFactory() {
        configs = MASConfiguration.getCurrentConfiguration().getSecurityConfigurations();
        clientCertPrivateKey = StorageProvider.getInstance().getTokenManager().getClientPrivateKey();
        clientCertChain = StorageProvider.getInstance().getTokenManager().getClientCertificateChain();
    }

    public SSLSocketFactory createTLSSocketFactory() {
        return new TLSSocketFactory(createSslContext().getSocketFactory());
    }

    private SSLContext createSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSL_TLS_PROTOCOL);
            List<TrustManager> trustManagerList = new ArrayList<>();
            for (MASSecurityConfiguration config : configs.values()) {
                trustManagerList.add(new TrustedCertificateConfigurationTrustManager(config));
            }

            TrustManager[] trustManagers = trustManagerList.toArray(new TrustManager[0]);
            KeyManager[] keyManagers = clientCertPrivateKey == null || clientCertChain == null
                    ? new KeyManager[0]
                    : new KeyManager[]{new SingleKeyX509KeyManager(clientCertPrivateKey, clientCertChain)};
            sslContext.init(keyManagers, trustManagers, secureRandom);
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create SSL Context: " + e.getMessage(), e);
        }
    }
}
