/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.cert.PublicKeyHash;
import com.ca.mas.core.http.ContentType;
import com.squareup.okhttp.mockwebserver.Dispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.security.auth.x500.X500Principal;

public class MASMultiServerTest extends MASLoginTestBase {

    private static MockWebServer mockServer;
    private static int PORT = 41980;
    private X509Certificate certificate;
    private JSONObject expectResponse;

    @Before
    public void setUp() throws Exception {
        expectResponse = new JSONObject();
        expectResponse.put("test", "value");
        mockServer = new MockWebServer();
        mockServer.useHttps(getSSLSocketFactory(), false);
        mockServer.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                return new MockResponse().setResponseCode(200)
                        .setBody(expectResponse.toString())
                        .addHeader("Content-type", ContentType.APPLICATION_JSON.toString());

            }
        });
        mockServer.start(PORT);
    }

    @Test
    public void testMultiServerCertificatePinning() throws Exception {
        Assert.assertNotNull(certificate);
        Assert.assertNotNull(certificate.getPublicKey());
        Assert.assertNotNull(PublicKeyHash.fromPublicKey(certificate.getPublicKey()));

        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .add(certificate)
                .add(PublicKeyHash.fromPublicKey(certificate.getPublicKey()).getHash())
                .build();

        MASConfiguration.getCurrentConfiguration().add(configuration);
        MASRequest request = new MASRequest.MASRequestBuilder(new URL("https://localhost:41980/test"))
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());

    }

    @Test
    public void testMultiServerCertificatePinningFailed() throws Exception {
    }

    @Test
    public void testMultiServerPublicKeyPinningFailed() throws Exception {
    }

    @Test
    public void testMultiServerPublicKeyPinning() throws Exception {
    }

    @Test
    public void testMultiServerMultiplePublicKeyPinning() throws Exception {
    }

    @Test
    public void testMultiServerNoPinning() throws Exception {
    }

    @Test
    public void testMultiServerIsPublicFlagOverride() throws Exception {
        //Set isPublic on MASRequest
    }

    @Test
    public void testMultiServerMssoConfigOverride() throws Exception {
        //Update msso-config setting
    }

    @Test
    public void testMultiServerConfigurationRuntimeUpdated() throws Exception {
        //Update new configuration during runtime
    }

    @Test
    public void testMultiServerDynamicSDK() throws Exception {
        //Update the msso-config using MAS.start
    }

    @Test
    public void testMultiServerIsPublic() throws Exception {
        //No access token header
        //No mag-identifier header
    }

    @Test
    public void testMultiServerIsNotPublic() throws Exception {
        //Contain access token header
        //ContainHas mag-identifier header
    }

    @After
    public void shutDownServer() throws Exception {
        if (mockServer != null) {
            mockServer.shutdown();
        }
    }

    private SSLSocketFactory getSSLSocketFactory() throws Exception {
        char[] password = "password".toCharArray();

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA", "BC");
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
                .setProvider("BC")
                .build(keyPair.getPrivate());

        builder.build(contentSigner);

        certificate = new JcaX509CertificateConverter().setProvider("BC").getCertificate(builder.build(contentSigner));

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
}