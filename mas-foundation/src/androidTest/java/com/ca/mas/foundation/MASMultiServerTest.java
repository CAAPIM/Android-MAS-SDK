/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.net.Uri;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.TestUtils;
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
import java.io.InvalidObjectException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import static junit.framework.Assert.assertTrue;

public class MASMultiServerTest extends MASLoginTestBase {

    private static MockWebServer mockServer;
    private static int PORT = 41980;
    private X509Certificate certificate;
    private JSONObject expectResponse;
    private String HOST = "localhost:41980";

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
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(certificate)
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);
        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());
    }

    @Test
    public void testMultiServerCertificatePinningWithCertChain() throws Exception {
        URL url = new URL("https://swapi.co");

        MASSecurityConfiguration.Builder configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build());

        Certificate[] certificates = getCert(url);
        for (Certificate certificate: certificates) {
            configuration.add(certificate);
        }

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration.build());

        Uri uri = new Uri.Builder().encodedAuthority(url.getAuthority())
                .scheme(url.getProtocol())
                .appendPath("api")
                .appendPath("people")
                .appendPath("1")
                .build();
        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        JSONObject result = callback.get().getBody().getContent();
        //assert the json result

    }


    @Test
    public void testMultiServerCertificatePinningFailed() throws Exception {
        URL url = new URL("https://swapi.co");
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(getCert(url)[0])
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        try {
            callback.get();
            Assert.fail();
        } catch (ExecutionException e) {
            //Should throw InvalidServerException or IOException or SSL....Exception
            assertTrue(e.getCause().getCause() instanceof InvalidObjectException);
        }
    }

    @Test
    public void testMultiServerPublicKeyPinningFailed() throws Exception {
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add("ZHVtbXk=") //Dummy
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        try {
            callback.get();
            Assert.fail();
        } catch (ExecutionException e) {
            //Should throw InvalidServerException or IOException or SSL....Exception
            assertTrue(e.getCause().getCause() instanceof InvalidObjectException);
        }
    }

    @Test
    public void testMultiServerPublicKeyPinning() throws Exception {
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(PublicKeyHash.fromCertificate(certificate).getHashString())
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);
        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());
    }

    @Test
    public void testMultiServerMultiplePublicKeyPinning() throws Exception {

        URL url = new URL("https://swapi.co");
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(PublicKeyHash.fromCertificate(certificate).getHashString())
                .add(PublicKeyHash.fromCertificate(getCert(url)[0]).getHashString())
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());

    }

    @Test
    public void testMultiServerNoPinning() throws Exception {
        URL url = new URL("https://swapi.co");

        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build())
                .trustPublicPKI(true)
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);
        Uri uri = new Uri.Builder().encodedAuthority(url.getAuthority())
                .scheme(url.getProtocol())
                .appendPath("api")
                .appendPath("people")
                .appendPath("1")
                .build();
        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        JSONObject result = callback.get().getBody().getContent();
    }

    @Test
    public void testMultiServerIsPublicFlagOverride() throws Exception {
        //Set isPublic on MASRequest

        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(certificate)
                .isPublic(false)
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);
        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .setPublic()
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assert.assertNull(recordedRequest.getHeader("mag-identifier"));
        Assert.assertNull(recordedRequest.getHeader("Authorization"));
    }

    @Test
    public void testMultiServerMssoConfigOverride() throws Exception {
        //Update msso-config setting
        //using a wrong hash for the msso-config
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(getHost() + ":" + getPort()).build())
                .add("ZHVtbXk=") //Dummy
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();

        MAS.invoke(request, callback);
        try {
            callback.get();
            Assert.fail();
        } catch (ExecutionException e) {
            //Should throw InvalidServerException or IOException or SSL....Exception
            assertTrue(e.getCause().getCause() instanceof InvalidObjectException);
        }

    }

    @Test
    public void testMultiServerConfigurationRuntimeUpdated() throws Exception {
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(certificate)
                .add(PublicKeyHash.fromCertificate(certificate).getHashString())
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());

        //Update new configuration during runtime
        MASSecurityConfiguration configuration2 = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(certificate)
                .add("ZHVtbXk=") //Dummy
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request, callback2);
        try {
            callback2.get();
            Assert.fail();
        } catch (ExecutionException e) {
            //Should throw InvalidServerException or IOException or SSL....Exception
            assertTrue(e.getCause().getCause() instanceof InvalidObjectException);
        }
    }

    @Test
    public void testMultiServerDynamicSDK() throws Exception {
        Uri orig = new Uri.Builder().encodedAuthority(getHost() + ":" + getPort()).build();
        Assert.assertNotNull(MASConfiguration.getCurrentConfiguration().getSecurityConfiguration(orig));

        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config_multi_server.json"));
        Assert.assertNull(MASConfiguration.getCurrentConfiguration().getSecurityConfiguration(orig));

        Uri newUri = new Uri.Builder().encodedAuthority("dummy:12345").build();
        Assert.assertNotNull(MASConfiguration.getCurrentConfiguration().getSecurityConfiguration(newUri));
    }

    @Test
    public void testMultiServerIsPublic() throws Exception {
        //No access token header
        //No mag-identifier header

        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(certificate)
                .add(PublicKeyHash.fromCertificate(certificate).getHashString())
                .isPublic(true)
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);
        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assert.assertNull(recordedRequest.getHeader("mag-identifier"));
        Assert.assertNull(recordedRequest.getHeader("Authorization"));

    }

    @Test
    public void testMultiServerWithUnknownHost() throws Exception {

    }

    @Test
    public void testMultiServerValidCertInvalidPin() throws Exception {

        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(certificate)
                .add("ZHVtbXk=") //Dummy
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);
        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        try {
            callback.get();
            Assert.fail();
        } catch (ExecutionException e) {
            //Should throw InvalidServerException or IOException or SSL....Exception
            assertTrue(e.getCause().getCause() instanceof InvalidObjectException);
        }
    }

    @Test
    public void testMultiServerIsNotPublic() throws Exception {
        //Contain access token header
        //ContainHas mag-identifier header

        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add(certificate)
                .add(PublicKeyHash.fromCertificate(certificate).getHashString())
                .isPublic(false)
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);
        MASRequest request = new MASRequest.MASRequestBuilder(
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());

        RecordedRequest recordedRequest = mockServer.takeRequest();
        Assert.assertNotNull(recordedRequest.getHeader("mag-identifier"));
        Assert.assertNotNull(recordedRequest.getHeader("Authorization"));
    }

    @Test
    public void testPublicCA() throws Exception {
        //https://swapi.co/api/people/4/
        //https://en.wikipedia.org/w/api.php?action=query&titles=CA_Technologies&prop=revisions&rvprop=content&format=json
        //https://itunes.apple.com/search?term=red+hot+chili+peppers&entity=musicVideo
        //https://www.googleapis.com/books/v1/volumes?q=patrick+rothfuss
        //https://api.ipify.org?format=json

        //May be with public key pinning only
        URL url = new URL("https://swapi.co");

        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build())
                .trustPublicPKI(true)
                .isPublic(true)
                .build();

        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);
        Uri uri = new Uri.Builder().encodedAuthority(url.getAuthority())
                .scheme(url.getProtocol())
                .appendPath("api")
                .appendPath("people")
                .appendPath("1")
                .build();
        MASRequest request = new MASRequest.MASRequestBuilder(uri)
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        JSONObject result = callback.get().getBody().getContent();
    }

    @Test
    public void testNoPinningAndNonTrustPublicPKI() throws Exception {
    }

    private Certificate[] getCert(URL url) throws Exception {

        //URL url = new URL("https://mobile-staging-androidautomation.l7tech.com:8443");
        //URL url = new URL("https://swapi.co");

        SSLContext sslCtx = SSLContext.getInstance("TLS");
        sslCtx.init(null, new TrustManager[]{new X509TrustManager() {

            private X509Certificate[] accepted;

            @Override
            public void checkClientTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                throw new CertificateException("This trust manager is only for clients");
            }

            @Override
            public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
                accepted = xcs;
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return accepted;
            }
        }}, null);

        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setSSLSocketFactory(sslCtx.getSocketFactory());
        connection.getResponseCode();
        Certificate[] certificates = connection.getServerCertificates();
        connection.disconnect();
        return certificates;
    }

    @After
    public void shutDownServer() throws Exception {
        if (mockServer != null) {
            mockServer.shutdown();
        }
        Map securityConfigurations = getValue(MASConfiguration.getCurrentConfiguration(), "securityConfigurations", Map.class);
        securityConfigurations.clear();
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
