/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import android.net.Uri;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASConnectionListener;

import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(AndroidJUnit4.class)
public abstract class MASMockGatewayTestBase extends MASTestBase {

    private static MockWebServer ssg;
    private HashMap<String, RecordedRequest> recordedRequests = new HashMap<>();
    private HashMap<String, RecordedRequest> recordRequestWithQueryParameters = new HashMap<>();
    private int requestTaken = 0;
    private GatewayDefaultDispatcher gatewayDefaultDispatcher;

    @Before
    public void startServer() throws Exception {

        ssg = new MockWebServer();
        gatewayDefaultDispatcher = new GatewayDefaultDispatcher();
        setupDispatcher(gatewayDefaultDispatcher);
        ssg.setDispatcher(gatewayDefaultDispatcher);
        final SSLSocketFactory sslSocketFactory = getSSLSocketFactory();
        ssg.useHttps(sslSocketFactory, false);
        ssg.start(getPort());

        //Turn on debug by default
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                return true;
            }
        });
        MAS.debug();

        MAS.setConnectionListener(new MASConnectionListener() {
            @Override
            public void onObtained(HttpURLConnection connection) {
                //If connect to localhost
                if (connection.getURL().getHost().equals(getHost()) && connection.getURL().getPort() == getPort()) {
                    //           ((HttpsURLConnection) connection).setSSLSocketFactory(SslContextBuilder.localhost().getSocketFactory());
                    ((HttpsURLConnection) connection).setSSLSocketFactory(sslSocketFactory);
                }

            }

            @Override
            public void onConnected(HttpURLConnection connection) {

            }
        });
    }
    private SSLSocketFactory getSSLSocketFactory() throws Exception {
        char[] password = "password".toCharArray();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(1024, new SecureRandom());
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        Calendar notAfter = Calendar.getInstance();
        notAfter.add(Calendar.DAY_OF_YEAR, 1);
        X509v3CertificateBuilder builder = new JcaX509v3CertificateBuilder(
                new X500Principal("CN=localhost"),
                new BigInteger("1"),
                new Date(),
                notAfter.getTime(),
                new X500Principal("CN=localhost"),
                keyPair.getPublic());

        ContentSigner contentSigner = new JcaContentSignerBuilder("SHA256WithRSAEncryption")
                //.setProvider("BC")
                .build(keyPair.getPrivate());

        builder.build(contentSigner);

        X509Certificate certificate = new JcaX509CertificateConverter()
                //.setProvider("BC")
                .getCertificate(builder.build(contentSigner));

        KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
        InputStream in = null;
        keyStore.load(in, password);
        Certificate[] certificateChain = {certificate};
        keyStore.setKeyEntry("private", keyPair.getPrivate(), password, certificateChain);
        keyStore.setCertificateEntry("cert", certificate);

        // Wrap it up in an SSL context.
        KeyManagerFactory keyManagerFactory =
                KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keyManagerFactory.init(keyStore, password);
        TrustManagerFactory trustManagerFactory =
                TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        trustManagerFactory.init(keyStore);
        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(),
                new SecureRandom());
        return sslContext.getSocketFactory();
    }
    @After
    public void shutDownServer() throws Exception {
        if (isSkipped) return;
        if (ssg != null) {
            ssg.shutdown();
        }
        recordedRequests.clear();
        recordRequestWithQueryParameters.clear();
        requestTaken = 0;
    }

    private void flushRequest() throws InterruptedException {
        int count = ssg.getRequestCount();
        count = count - requestTaken;
        for (int i = 0; i < count; i++) {
            RecordedRequest rr = ssg.takeRequest();
            Uri uri = Uri.parse(rr.getPath());
            recordedRequests.put(uri.getPath(), rr);
            recordRequestWithQueryParameters.put(rr.getPath(), rr);
        }
        requestTaken = ssg.getRequestCount();
    }

    protected RecordedRequest getRecordRequest(String path) throws InterruptedException {
        flushRequest();
        return recordedRequests.get(path);
    }

    protected RecordedRequest getRecordRequestWithQueryParameter(String url) throws InterruptedException {
        flushRequest();
        return recordRequestWithQueryParameters.get(url);
    }

    protected void setupDispatcher(GatewayDefaultDispatcher gatewayDefaultDispatcher) {

    }

    protected void setDispatcher(Dispatcher dispatcher) {
        ssg.setDispatcher(dispatcher);
    }

    protected int getPort() {
        return 41979;
    }

    protected String getHost() {
        return "localhost";
    }

}


