/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.io.ssl;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.io.http.SingleKeyX509KeyManager;
import com.ca.mas.core.io.http.TrustedCertificateConfigurationTrustManager;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASSecurityConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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

class TLSSocketFactory extends SSLSocketFactory {

    private MASSecurityConfiguration securityConfiguration=null;
    private SSLSocketFactory sslSocketFactory;
    private static final String SSL_V3_PROTOCOL = "SSLv3";
    private static final String SSL_TLS_V1_PROTOCOL = "TLSv1";
    private static final String SSL_TLS_V1_1_PROTOCOL = "TLSv1.1";
    private static final String SSL_TLS_V1_2_PROTOCOL = "TLSv1.2";
    private static final String[] SUPPORTED_TLS =  {SSL_V3_PROTOCOL, SSL_TLS_V1_PROTOCOL, SSL_TLS_V1_1_PROTOCOL, SSL_TLS_V1_2_PROTOCOL};

    private PrivateKey clientCertPrivateKey = null;
    private X509Certificate[] clientCertChain = null;
    private static final SecureRandom secureRandom = new SecureRandom();

    TLSSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public TLSSocketFactory(@NonNull MASSecurityConfiguration config) {
        securityConfiguration = config;try {
            if (MASConfiguration.getCurrentConfiguration() != null) {
                clientCertPrivateKey = StorageProvider.getInstance().getTokenManager().getClientPrivateKey();
                clientCertChain = StorageProvider.getInstance().getTokenManager().getClientCertificateChain();
            }
        } catch (IllegalStateException e) {
            //The SDK has not been started yet and is in the enrollment URL flow
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket() throws IOException {
        return enableTLS(sslSocketFactory.createSocket());
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
//        return enableTLS(sslSocketFactory.createSocket(s, host, port, autoClose));

        SSLContext sslContext = null;
        try {
            sslContext = SSLContext.getInstance(MAGSocketFactory.SSL_TLS_PROTOCOL);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        TrustManager manager = new TrustedCertificateConfigurationTrustManager(securityConfiguration);
        TrustManager[] trustManagers = {manager};
        KeyManager[] keyManagers = clientCertPrivateKey == null || clientCertChain == null
                ? new KeyManager[0]
                : new KeyManager[]{new SingleKeyX509KeyManager(clientCertPrivateKey, clientCertChain)};
        try {
            sslContext.init(keyManagers, trustManagers, secureRandom);
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        SSLSocket sslSocket = (SSLSocket) sslContext.getSocketFactory().createSocket(s, host, port, autoClose);
        ((SSLSocket) sslSocket).setEnabledProtocols(SUPPORTED_TLS);
            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
            final String str = MASConfiguration.getCurrentConfiguration().getGatewayHostName();
            SSLSession session = sslSocket.getSession();
            if (!hv.verify(str, session)) {
                throw new SSLHandshakeException("Expected echo.websocket.org, found " + session.getPeerPrincipal());
            } else {
                Log.i("Client", "Success");
            }
        return sslSocket;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return enableTLS(sslSocketFactory.createSocket(host, port));
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return enableTLS(sslSocketFactory.createSocket(host, port, localHost, localPort));
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return enableTLS(sslSocketFactory.createSocket(host, port));
    }
    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return enableTLS(sslSocketFactory.createSocket(address, port, localAddress, localPort));
    }
//
//    @Override
//    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
//        SSLSocket  sslSocket;
//        if (hv.verify(address.getHostName(), session)) {
//            sslSocket = (SSLSocket) sslSocketFactory.createSocket(address, port, localAddress, localPort);
//        final String str = MASConfiguration.getCurrentConfiguration().getGatewayHostName();
//        ((SSLSocket) sslSocket).startHandshake();
//        SSLSession session = sslSocket.getSession();
//        HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
//            // throw some exception or do something similar.
//        }
//        return sslSocket;
//    }

    private Socket enableTLS(Socket socket) throws IOException {
        if (socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket) socket).setEnabledProtocols(SUPPORTED_TLS);






//            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
//            SSLSession s = (SSLSession) ((SSLSocket) socket).getSession();
//            if (!hv.verify(str, s)) {
//                throw new SSLHandshakeException("Expected mail.google.com, found ");
//            }
        }
        return socket;
    }
}
