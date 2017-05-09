/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.io.http;

import java.net.Socket;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLEngine;
import javax.net.ssl.X509ExtendedKeyManager;

/**
 * An SSL X509KeyManager that holds a single pre-configured client cert and private key, and always uses
 * it to respond to challenges.
 */
public class SingleKeyX509KeyManager extends X509ExtendedKeyManager {
    private final X509Certificate[] certChain;
    private final PrivateKey privateKey;
    private final String alias;

    /**
     * Create a a KeyManager that only knows about a single X.509 certificate.
     *
     * @param privateKey  the private key for this certificate chain.  Required.
     * @param certChain the certificate chain to present when challenged (when acting as a client), or to use as the server cert (when acting as a server).  Required.
     */
    public SingleKeyX509KeyManager(PrivateKey privateKey, X509Certificate[] certChain) {
        if (certChain == null)
            throw new NullPointerException("certChain");
        if (certChain.length < 1)
            throw new IllegalArgumentException("certChain is empty");
        if (privateKey == null)
            throw new NullPointerException("privateKey");

        this.certChain = certChain;
        this.privateKey = privateKey;
        this.alias = "clientCert";
    }

    public String[] getClientAliases(String s, Principal[] principals) {
        return new String[] { alias };
    }

    public String chooseClientAlias(String[] strings, Principal[] principals, Socket socket) {
        return alias;
    }

    public String[] getServerAliases(String s, Principal[] principals) {
        return new String[] { alias };
    }

    public String chooseServerAlias(String s, Principal[] principals, Socket socket) {
        return alias;
    }

    public X509Certificate[] getCertificateChain(String s) {
        return alias.equals(s)
            ? certChain
            : null;
    }

    public PrivateKey getPrivateKey(String s) {
        return alias.equals(s)
            ? privateKey
            : null;
    }

    public String chooseEngineClientAlias(String[] strings, Principal[] principals, SSLEngine sslEngine) {
        return alias;
    }

    public String chooseEngineServerAlias(String string, Principal[] principals, SSLEngine sslEngine) {
        return alias;
    }
}
