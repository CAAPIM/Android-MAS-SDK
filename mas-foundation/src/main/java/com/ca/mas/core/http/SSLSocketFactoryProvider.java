/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.http;

import android.net.Uri;

import com.ca.mas.core.io.ssl.MAGSocketFactory;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASSecurityConfiguration;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import javax.net.ssl.SSLSocketFactory;

public class SSLSocketFactoryProvider {

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
     * @return the SSLSocketFactory
     */
    public SSLSocketFactory get(URL url) {
        Uri sanitized = new Uri.Builder().encodedAuthority(url.getHost() + ":" + url.getPort()).build();
        SSLSocketFactory factory = factories.get(sanitized);

        //If not found in the cache, we create one and add it
        if (factory == null) {
            factory = getSSLSocketFactory(sanitized);
            factories.put(sanitized, factory);
        }
        return factory;
    }

    /**
     * Returns the SSLSocketFactory associated with the primary gateway configuration.
     * @return the SSLSocketFactory
     */
    public SSLSocketFactory getPrimaryGatewaySocketFactory() {
        MASConfiguration currentConfiguration = MASConfiguration.getCurrentConfiguration();
        Uri uri = new Uri.Builder().encodedAuthority(currentConfiguration.getGatewayHostName()
                + ":"
                + currentConfiguration.getGatewayPort())
                .build();
        return getSSLSocketFactory(uri);
    }

    /**
     * Attempts to return the SSLSocketFactory associated with the host configuration.
     * If the SSLSocketFactory is not found, it will create a configuration and map it to the host.
     * Otherwise, we return null.
     * @param hostname
     * @return the SSLSocketFactory or null
     */
    public SSLSocketFactory getSSLSocketFactory(Uri hostname) {
        MASConfiguration config = MASConfiguration.getCurrentConfiguration();
        if (config != null) {
            MASSecurityConfiguration securityConfig = config.getSecurityConfiguration(hostname);
            if (securityConfig != null) {
                return createSSLSocketFactory(securityConfig);
            }
        }
        return null;
    }

    /**
     * Creates a SSLSocketFactory for this configuration.
     * @param configuration
     * @return the primary SSLSocketFactory
     */
    public SSLSocketFactory createSSLSocketFactory(MASSecurityConfiguration configuration) {
        return new MAGSocketFactory(configuration).createTLSSocketFactory();
    }

}
