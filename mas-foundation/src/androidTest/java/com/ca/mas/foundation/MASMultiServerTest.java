/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.net.Uri;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.cert.PublicKeyHash;
import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.io.Charsets;

import junit.framework.Assert;

import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static com.ca.mas.TestUtils.getJSONObject;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class MASMultiServerTest extends MASLoginTestBase {

    private static MockWebServer mockServer;
    private static int PORT = 41980;
    private X509Certificate certificate;
    private JSONObject expectResponse;
    private String HOST = "localhost:41980";

    @Before
    public void multiServerSetup() throws Exception {
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
    public void testHttpOverrideContentType() throws InterruptedException {

        final ContentType customCharset = new ContentType("application/x-www-form-urlencoded", Charsets.UTF8);

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
                .header("Content-Type", customCharset.toString())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        RecordedRequest req = mockServer.takeRequest();
        String head = req.getHeader("Content-type");

        assertEquals(head, customCharset.toString());
    }

    @Test
    public void testMultiServerCertificatePinningWithCertChain() throws Exception {
        URL url = new URL("https://swapi.co:443");

        MASSecurityConfiguration.Builder configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build());

        Certificate[] certificates = getCert(url);
        for (Certificate certificate : certificates) {
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
        Assert.assertNotNull(result);
        Assert.assertEquals("Luke Skywalker", result.getString("name"));
    }

    @Test
    public void testMultiServerCertificatePinningFailed() throws Exception {
        URL url = new URL("https://swapi.co:443");
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
            assertTrue(e.getCause().getCause() instanceof SSLHandshakeException);
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
            assertTrue(e.getCause().getCause() instanceof SSLHandshakeException);
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
        URL url = new URL("https://swapi.co:443");
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
        URL url = new URL("https://swapi.co:443");

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
        Assert.assertNotNull(result);
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
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add("ZHVtbXk=") //Dummy
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        MASRequest request = new MASRequest.MASRequestBuilder(
//        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                new Uri.Builder().encodedAuthority(HOST)
                        .scheme("https")
                        .path("test")
                        .build())
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();

        MAS.invoke(request, callback);
        try {
            callback.get().getBody();
            Assert.fail();
        } catch (ExecutionException e) {
            //Should throw InvalidServerException or IOException or SSL....Exception
            assertTrue(e.getCause().getCause() instanceof SSLHandshakeException);
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
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration2);

        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request, callback2);
        try {
            callback2.get();
            Assert.fail();
        } catch (ExecutionException e) {
            //Should throw InvalidServerException or IOException or SSL....Exception
            assertTrue(e.getCause().getCause() instanceof SSLHandshakeException);
        }
    }

    @Test
    public void testMultiServerDynamicSDK() throws Exception {
        Uri orig = new Uri.Builder().encodedAuthority(getHost() + ":" + getPort()).build();
        Assert.assertNotNull(MASConfiguration.getCurrentConfiguration().getSecurityConfiguration(orig));

        MAS.start(getContext(), getJSONObject("/msso_config_multi_server.json"));
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
            assertTrue(e.getCause().getCause() instanceof SSLHandshakeException);
        }
    }

    @Test
    public void testMultiServerIsNotPublic() throws Exception {
        //Contain access token header
        //Contains mag-identifier header
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
        //https://swapi.co/api/people/1/
        //https://en.wikipedia.org/w/api.php?action=query&titles=CA_Technologies&prop=revisions&rvprop=content&format=json
        //https://itunes.apple.com/search?term=red+hot+chili+peppers&entity=musicVideo
        //https://www.googleapis.com/books/v1/volumes?q=patrick+rothfuss
        //https://api.ipify.org?format=json

        //Star Wars API
        URL url = new URL("https://swapi.co:443");
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build())
                .trustPublicPKI(true)
                .isPublic(true)
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        Uri uri = new Uri.Builder().encodedAuthority(url.getAuthority())
                .scheme(url.getProtocol())
                .appendPath("api").appendPath("people").appendPath("1")
                .build();
        MASRequest request = new MASRequest.MASRequestBuilder(uri).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        JSONObject result = callback.get().getBody().getContent();
        Assert.assertNotNull(result);
        Assert.assertEquals("Luke Skywalker", result.getString("name"));

        //Google API
        URL url2 = new URL("https://www.googleapis.com:443");
        MASSecurityConfiguration configuration2 = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url2.getHost() + ":" + url2.getPort()).build())
                .trustPublicPKI(true)
                .isPublic(true)
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration2);

        Uri uri2 = new Uri.Builder().encodedAuthority(url2.getAuthority())
                .scheme(url2.getProtocol())
                .appendPath("books").appendPath("v1")
                .appendPath("volumes")
                .appendQueryParameter("q", "patrick+rothfuss")
                .build();
        MASRequest request2 = new MASRequest.MASRequestBuilder(uri2).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);

        JSONObject result2 = callback2.get().getBody().getContent();
        Assert.assertNotNull(result2);
        String expectedAuthor = result2.getJSONArray("items").getJSONObject(0).getJSONObject("volumeInfo").getJSONArray("authors").getString(0);
        Assert.assertEquals(expectedAuthor, "Patrick Rothfuss");
    }

    @Test
    public void testNoPinningAndNonTrustPublicPKI() throws Exception {
        URL url = new URL("https://swapi.co:443");
        try {
            MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build())
                .trustPublicPKI(false)
                .build();
        } catch (Exception e) {
            //Should throw InvalidServerException or IOException or SSL....Exception
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testMultiServerGeneratedSecurityConfiguration() throws Exception {
        List<MASSecurityConfiguration> securityConfigs = getSecurityConfiguration("multiservercerts.json");
        MASConfiguration config = MASConfiguration.getCurrentConfiguration();
        for (MASSecurityConfiguration securityConfig : securityConfigs) {
            config.addSecurityConfiguration(securityConfig);
        }

        URL url = new URL("https://en.wikipedia.org:443");
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build())
                .trustPublicPKI(true)
                .isPublic(true)
                .build();
        MASConfiguration.getCurrentConfiguration().addSecurityConfiguration(configuration);

        Uri uri = new Uri.Builder().encodedAuthority(url.getAuthority())
                .scheme(url.getProtocol())
                .appendPath("w").appendPath("api.php")
                .appendQueryParameter("action", "query")
                .appendQueryParameter("titles", "CA_Technologies")
                .appendQueryParameter("prop", "revisions")
                .appendQueryParameter("rvprop", "content")
                .appendQueryParameter("format", "json")
                .build();
        MASRequest request = new MASRequest.MASRequestBuilder(uri).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        JSONObject result = callback.get().getBody().getContent();
        Assert.assertNotNull(result);
        String expectedResult = result.getJSONObject("query").getJSONArray("normalized").getJSONObject(0).getString("to");
        Assert.assertEquals(expectedResult, "CA Technologies");
    }

    @Test
    public void testMultiServerEnableSSLPinning() throws Exception {
        MAS.setSSLPinningEnabled(false);
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

        // Should pass as enableSSLPinning is false.
        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());
    }

    @Test
    public void testMultiServerAllowSSLPinning() throws Exception {
        MAS.setSSLPinningEnabled(true);
        MASSecurityConfiguration configuration = new MASSecurityConfiguration.Builder()
                .host(new Uri.Builder().encodedAuthority(HOST).build())
                .add("ZHVtbXk=") //Dummy
                .allowSSLPinning(false)
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

        // Should pass as allowSSLPinning is set to False
        Assert.assertEquals(expectResponse.toString(), callback.get().getBody().getContent().toString());
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

    @Override
    @After
    public void deregister() throws InterruptedException, ExecutionException {
        super.deregister();
        try {
            if (mockServer != null) {
                mockServer.shutdown();
            }
        } catch (IOException e) {
            //Couldn't shut down the mock server
        }
        Map securityConfigurations = getValue(MASConfiguration.getCurrentConfiguration(), "securityConfigurations", Map.class);
        securityConfigurations.clear();
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
                .build(keyPair.getPrivate());

        builder.build(contentSigner);

        certificate = new JcaX509CertificateConverter().getCertificate(builder.build(contentSigner));

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

    private List<MASSecurityConfiguration> getSecurityConfiguration(String filename) throws IOException, JSONException {
        List<MASSecurityConfiguration> securityConfigurations = new ArrayList<>();
        JSONObject sc = getJSONObject("/" + filename);

        for (int i = 0; i < sc.names().length(); i++) {
            MASSecurityConfiguration.Builder builder = new MASSecurityConfiguration.Builder();
            URL url = new URL(sc.names().getString(i));
            Uri uri = new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build();
            builder.host(uri);

            JSONObject config = sc.getJSONObject(sc.names().getString(i));

            //Certificate
            JSONArray certificates = config.optJSONArray("certificates");
            if (certificates != null) {
                for (int j = 0; j < certificates.length(); j++) {
                    JSONArray certificate = certificates.getJSONArray(j);
                    String c = certificate.join("\n").replace("\"", "");
                    Certificate cert = CertUtils.decodeCertFromPem(c);
                    builder.add(cert);
                }
            }

            //PublicKeyHash
            JSONArray hashes = config.optJSONArray("publicKeyHashes");
            if (hashes != null) {
                for (int j = 0; j < hashes.length(); j++) {
                    builder.add(hashes.getString(j));
                }
            }

            builder.isPublic(config.optBoolean("isPublic", false));
            builder.trustPublicPKI(config.optBoolean("trustPublicPKI", false));
            securityConfigurations.add(builder.build());
        }

        return securityConfigurations;
    }
}
