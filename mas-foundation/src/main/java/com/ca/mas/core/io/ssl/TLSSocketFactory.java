/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.io.ssl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.SQLOutput;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

class TLSSocketFactory {

    private SSLSocketFactory sslSocketFactory;
    private static final String SSL_V3_PROTOCOL = "SSLv3";
    private static final String SSL_TLS_V1_PROTOCOL = "TLSv1";
    private static final String SSL_TLS_V1_1_PROTOCOL = "TLSv1.1";
    private static final String SSL_TLS_V1_2_PROTOCOL = "TLSv1.2";
    private static final String[] SUPPORTED_TLS =  {SSL_V3_PROTOCOL, SSL_TLS_V1_PROTOCOL, SSL_TLS_V1_1_PROTOCOL, SSL_TLS_V1_2_PROTOCOL};

    TLSSocketFactory(SSLSocketFactory sslSocketFactory) {
        this.sslSocketFactory = sslSocketFactory;
    }

    public String[] getDefaultCipherSuites() {
        return sslSocketFactory.getDefaultCipherSuites();
    }

    public String[] getSupportedCipherSuites() {
        return sslSocketFactory.getSupportedCipherSuites();
    }

    public Socket createSocket() throws IOException {
        return enableTLS(sslSocketFactory.createSocket());
    }

    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        int i=0;
        System.out.println("i = "+i++);
        return enableTLS(sslSocketFactory.createSocket(s, host, port, autoClose));
    }

    public Socket createSocket(String host, int port) throws IOException {
        int j=0;
        System.out.println("j = "+j++);
        return enableTLS(sslSocketFactory.createSocket(host, port));
    }

    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        int k=0;
        System.out.println("k = "+k++);
        return enableTLS(sslSocketFactory.createSocket(host, port, localHost, localPort));
    }

    public Socket createSocket(InetAddress host, int port) throws IOException {
        int l=0;
        System.out.println("l = "+l++);
        return enableTLS(sslSocketFactory.createSocket(host, port));
    }

    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        int m=0;
        System.out.println("m = "+m++);
        return enableTLS(sslSocketFactory.createSocket(address, port, localAddress, localPort));
    }

    private Socket enableTLS(Socket socket) {
        if (socket != null && (socket instanceof SSLSocket)) {
            ((SSLSocket) socket).setEnabledProtocols(SUPPORTED_TLS);
        }
        return socket;
    }
}
