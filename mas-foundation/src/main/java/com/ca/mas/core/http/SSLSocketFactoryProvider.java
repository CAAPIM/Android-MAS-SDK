package com.ca.mas.core.http;

import android.net.Uri;

import com.ca.mas.core.io.ssl.MAGSocketFactory;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASSecurityConfiguration;

import java.net.URL;
import java.security.cert.Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.net.ssl.SSLSocketFactory;

class SSLSocketFactoryProvider {

    private static SSLSocketFactoryProvider instance = new SSLSocketFactoryProvider();
    private Map<Uri, SSLSocketFactory> factories = new HashMap<>();

    private SSLSocketFactoryProvider() {
        MASConfiguration.SECURITY_CONFIGURATION_CHANGED.addObserver(
                new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        factories.remove((Uri)arg);
                    }
                }
        );

        MASConfiguration.SECURITY_CONFIGURATION_RESET.addObserver(
                new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        factories.clear();
                    }
                }
        );
    }

    public static SSLSocketFactoryProvider getInstance() {
        return instance;
    }

    /**
     * Gets the SSLSocketFactory associated with the specified URL host and port.
     * @param url url
     * @return
     */
    public SSLSocketFactory get(URL url) {
        Uri sanitized = new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build();
        SSLSocketFactory factory = factories.get(sanitized);

        //If not found in the cache, we create one and add it
        if (factory == null) {
            factory = createSSLSocketFactory(sanitized);
            factories.put(sanitized, factory);
        }
        return factory;
    }

    //TODO
    //SSLSocketFactoryProvider -> make SSLSocketFactory via TrustManager objects which have pinning logic inside TrustedCertificateConfigurationTrustManager
    //MAGSocketFactory won't be needed anymore
    public SSLSocketFactory createSSLSocketFactory(MASSecurityConfiguration configuration) {
        MASSecurityConfiguration.PINNING_TYPE pinType = configuration.getPinningType();
        SSLSocketFactory sslSocketFactory = new MAGSocketFactory().createTLSSocketFactory();

        if (pinType != null) {
            List<Certificate> certs = configuration.getCertificates();
            if (!certs.isEmpty()) {
                for (Certificate cert : certs) {

                }
            }
            if (pinType == MASSecurityConfiguration.PINNING_TYPE.publicKeyHash) {
                List<String> hashes = configuration.getPublicKeyHashes();
                boolean valid;
                for (String hash : hashes) {

                }
            }
        }
        return null;
    }

    public SSLSocketFactory createSSLSocketFactory(Uri hostname) {
        MASConfiguration config = MASConfiguration.getCurrentConfiguration();
        if (config != null) {
            MASSecurityConfiguration securityConfig = config.findByHost(hostname);
            if (securityConfig != null) {
                createSSLSocketFactory(securityConfig);
            }
        }
        return null;
    }
}

