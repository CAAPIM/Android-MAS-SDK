/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.io.ssl;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.io.http.SingleKeyX509KeyManager;
import com.ca.mas.core.io.http.TrustedCertificateConfigurationTrustManager;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASSecurityConfiguration;

import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

public class MAGSocketFactory {

    public static final String SSL_TLS_PROTOCOL = "TLS";

    private static final SecureRandom secureRandom = new SecureRandom();
    private MASSecurityConfiguration securityConfiguration;
    private PrivateKey clientCertPrivateKey = null;
    private X509Certificate[] clientCertChain = null;

    /**
     * Create an SocketFactory factory that will create clients which trust the specified server certs
     * and use the specified client cert for client cert authentication.
     * <br>
     * It retrieves the trustConfig, clientCertPrivateKey and clientCertChain
     * from the {@link com.ca.mas.core.store.TokenManager}.
     */
    public MAGSocketFactory(@NonNull MASSecurityConfiguration config) {
        securityConfiguration = config;
        try {
            if (MASConfiguration.getCurrentConfiguration() != null) {
                clientCertPrivateKey = StorageProvider.getInstance().getTokenManager().getClientPrivateKey();
                clientCertChain = StorageProvider.getInstance().getTokenManager().getClientCertificateChain();
            }
        } catch (IllegalStateException e) {
            //The SDK has not been started yet and is in the enrollment URL flow
        }
    }

    public SSLSocketFactory createTLSSocketFactory() {
        SSLSocketFactory sslSocketFactory = createSslContext().getSocketFactory();
        return new TLSSocketFactory(securityConfiguration);
    }

    private SSLContext createSslContext() {
        try {
            SSLContext sslContext = SSLContext.getInstance(SSL_TLS_PROTOCOL);
            TrustManager manager = new TrustedCertificateConfigurationTrustManager(securityConfiguration);
            TrustManager[] trustManagers = {manager};
            KeyManager[] keyManagers = clientCertPrivateKey == null || clientCertChain == null
                    ? new KeyManager[0]
                    : new KeyManager[]{new SingleKeyX509KeyManager(clientCertPrivateKey, clientCertChain)};
            sslContext.init(keyManagers, trustManagers, secureRandom);
//            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
//            final String str = MASConfiguration.getCurrentConfiguration().getGatewayHostName();
//            SSLSession session = sslSocket.getSession();
//            if (!hv.verify(str, session)) {
//                throw new SSLHandshakeException("Expected echo.websocket.org, found " + session.getPeerPrincipal());
//            } else {
//                Log.i("Client", "Success");
//            }
            return sslContext;
        } catch (Exception e) {
            throw new RuntimeException("Unable to create SSL Context: " + e.getMessage(), e);
        }
    }
}
