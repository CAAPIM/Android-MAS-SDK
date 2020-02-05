/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.io.ssl;

import com.ca.mas.foundation.MASConfiguration;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class TLSSocketFactory extends SSLSocketFactory {

    private SSLSocketFactory sslSocketFactory;
    private static final String SSL_V3_PROTOCOL = "SSLv3";
    private static final String SSL_TLS_V1_PROTOCOL = "TLSv1";
    private static final String SSL_TLS_V1_1_PROTOCOL = "TLSv1.1";
    private static final String SSL_TLS_V1_2_PROTOCOL = "TLSv1.2";
    private static final String[] SUPPORTED_TLS =  {SSL_V3_PROTOCOL, SSL_TLS_V1_PROTOCOL, SSL_TLS_V1_1_PROTOCOL, SSL_TLS_V1_2_PROTOCOL};

    TLSSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
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
        return enableTLS(sslSocketFactory.createSocket(s, host, port, autoClose));
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

    private Socket enableTLS(Socket socket) throws SSLHandshakeException {
        if (socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket) socket).setEnabledProtocols(SUPPORTED_TLS);
            final String str = MASConfiguration.getCurrentConfiguration().getGatewayHostName();
            HostnameVerifier hv = HttpsURLConnection.getDefaultHostnameVerifier();
            SSLSession s = (SSLSession) ((SSLSocket) socket).getSession();
            if (!hv.verify(str, s)) {
                throw new SSLHandshakeException("Expected mail.google.com, found ");
            }
        }
        return socket;
    }
}
