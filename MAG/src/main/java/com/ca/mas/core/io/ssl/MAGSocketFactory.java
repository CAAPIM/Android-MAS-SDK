/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.io.ssl;

import android.content.Context;

import com.ca.mas.core.cert.TrustedCertificateConfiguration;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.context.MssoException;
import com.ca.mas.core.io.http.SingleKeyX509KeyManager;
import com.ca.mas.core.io.http.TrustedCertificateConfigurationTrustManager;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class MAGSocketFactory {

    private static final String SSL_TLS_PROTOCOL = "TLS";
    private static final SecureRandom secureRandom = new SecureRandom();
    private TrustedCertificateConfiguration trustConfig;
    private PrivateKey clientCertPrivateKey = null;
    private X509Certificate[] clientCertChain = null;

    /**
     * Create an SocketFactory factory that will create clients that trust the specified server certs and that use the specified client cert
     * for client cert authentication.
     */
    public MAGSocketFactory(Context context) {
        this.trustConfig = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider();
        StorageProvider storageProvider = new StorageProvider(context);
        if (!storageProvider.hasValidStore()) {
            throw new MssoException("No valid Data Source was provided");
        }
        TokenManager tokenManager = storageProvider.createTokenManager();
        KeyPair keyPair = tokenManager.getClientKeyPair();
        if (keyPair != null) {
            clientCertPrivateKey = keyPair.getPrivate();
        }
        clientCertChain = tokenManager.getClientCertificateChain();
    }

    public SSLSocketFactory createSSLSocketFactory() {
        return createSslContext().getSocketFactory();
    }

    private SSLContext createSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSL_TLS_PROTOCOL);
            TrustManager[] trustManagers = {new TrustedCertificateConfigurationTrustManager(
                    trustConfig)};
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
