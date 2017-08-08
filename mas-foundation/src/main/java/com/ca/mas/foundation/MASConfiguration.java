/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.content.Context;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.cert.PublicKeyHash;
import com.ca.mas.core.conf.Config;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MASConfiguration {

    private static Config USERINFO = new Config(false, FoundationConsts.KEY_CONFIG_USER_INFO, "oauth.oauth_protected_endpoints.userinfo_endpoint_path", String.class);
    private static Config MAS_SCIM = new Config(false, FoundationConsts.KEY_CONFIG_SCIM_PATH, "mas.scim-path", String.class);
    private static Config MAS_STORAGE = new Config(false, FoundationConsts.KEY_CONFIG_CLOUD_STORAGE_PATH, "mas.mas-storage-path", String.class);
    private static Config APP_NAME = new Config(false, FoundationConsts.KEY_CONFIG_APP_NAME, "oauth.client.client_name", String.class);
    private static Config APP_ORGANIZATION = new Config(false, FoundationConsts.KEY_CONFIG_APP_ORGANIZATION, "oauth.client.organization", String.class);
    private static Config APP_REGISTERED_BY = new Config(false, FoundationConsts.KEY_CONFIG_APP_REGISTERED_BY, "oauth.client.registered_by", String.class);
    private static Config APP_DESCRIPTION = new Config(false, FoundationConsts.KEY_CONFIG_APP_DESCRIPTION, "oauth.client.description", String.class);
    private static Config APP_TYPE = new Config(false, FoundationConsts.KEY_CONFIG_APP_TYPE, "oauth.client.client_type", String.class);
    public static final EventDispatcher SECURITY_CONFIGURATION_CHANGED = new EventDispatcher();
    public static final EventDispatcher SECURITY_CONFIGURATION_RESET = new EventDispatcher();
    private static MASConfiguration primary;
    private static Map<URL, MASSecurityConfiguration> securityConfigurations = new HashMap<>();

    public static MASConfiguration getCurrentConfiguration() {
        if (primary == null) {
            throw new IllegalStateException("MAS.start() has not been invoked.");
        }
        return primary;
    }

    protected MASConfiguration(Context context) {
        Context appContext = context.getApplicationContext();
        ConfigurationManager manager = ConfigurationManager.getInstance();
        ConfigurationProvider provider = manager.getConnectedGatewayConfigurationProvider();
        manager.init(appContext);
        manager.setAppConfigs(Arrays.asList(USERINFO, MAS_SCIM, MAS_STORAGE, APP_NAME,
                APP_ORGANIZATION, APP_REGISTERED_BY, APP_DESCRIPTION, APP_TYPE));
        primary = this;

        SECURITY_CONFIGURATION_RESET.notifyObservers();
        //Remove the previous MASSecurityConfiguration for the MSSO config gateway
        for (MASSecurityConfiguration config : securityConfigurations.values()) {
            if (config.isPrimary()) {
                securityConfigurations.remove(config.getHost());
            }
        }

        //Construct the MSSO config security configuration and put it back into the map
        URL host = sanitizeURL(getGatewayUrl());
        MASSecurityConfiguration.Builder configBuilder = new MASSecurityConfiguration.Builder()
                .isPrimary(true)
                //By default the gateway is not public
                .isPublic(false)
                .host(host)
                .trustPublicPKI(isEnabledTrustedPublicPKI());

        //Add certificates, if any exist
        Collection<X509Certificate> certificates = provider.getTrustedCertificateAnchors();
        if (certificates != null) {
            for (X509Certificate cert : certificates) {
                configBuilder.add(cert);
            }
        }

        //Add public key hashes, if any exist
        Collection<PublicKeyHash> publicKeyHashes = provider.getTrustedCertificatePinnedPublicKeyHashes();
        if (publicKeyHashes != null) {
            for (PublicKeyHash hash : publicKeyHashes) {
                String hashString = hash.getHashString();
                configBuilder.add(hashString);
            }
        }

        MASSecurityConfiguration config = configBuilder.build();
        securityConfigurations.put(config.getHost(), config);
    }

    /**
     * This indicates the status of the configuration loading. YES if it has successfully loaded and is ready for use.
     * NO if not yet loaded or perhaps an error has occurred during attempting to load.
     */
    public boolean isLoaded() {
        return true;
    }

    /**
     * The name of the applications.
     */
    public String getApplicationName() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(FoundationConsts.KEY_CONFIG_APP_NAME);
    }

    /**
     * The type of the application.
     */
    public String getApplicationType() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(FoundationConsts.KEY_CONFIG_APP_TYPE);
    }

    /**
     * The description of the application.
     */
    public String getApplicationDescription() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(FoundationConsts.KEY_CONFIG_APP_DESCRIPTION);
    }

    /**
     * The organization name of the application.
     */
    public String getApplicationOrganization() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(FoundationConsts.KEY_CONFIG_APP_ORGANIZATION);
    }

    /**
     * The name of the entity that registered the application.
     */
    public String getApplicationRegisteredBy() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(FoundationConsts.KEY_CONFIG_APP_REGISTERED_BY);
    }

    /**
     * The public server certificate of the Gateway as obtained from the configuration.
     */
    public Collection<X509Certificate> getGatewayCertificates() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTrustedCertificateAnchors();
    }

    /**
     * The host name of the Gateway.
     */
    public String getGatewayHostName() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTokenHost();
    }

    /**
     * The port assigned on the Gateway.
     */
    public int getGatewayPort() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTokenPort();
    }

    /**
     * The prefix assigned on the Gateway.
     */
    public String getGatewayPrefix() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getPrefix();
    }

    /**
     * The full URL of the Gateway including the prefix, hostname and port in a ://: format.
     */
    public URL getGatewayUrl() {
        try {
            return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUri(null).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Determines if a user’s location coordinates are required. This read only value is within the JSON configuration file and is
     * set as a requirement of the application on the Gateway. This means that a set of location coordinates must be sent in the
     * header of all protected endpoint HTTP request to the API on the Gateway.
     */
    public boolean getLocationIsRequired() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.PROP_LOCATION_ENABLED);
    }

    /**
     * Determines SDK is enabled for public key pinning for authentication challenge. This read only value is within the JSON configuration file.
     */
    public boolean isEnabledPublicKeyPinning() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTrustedCertificatePinnedPublicKeyHashes() != null &&
                ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTrustedCertificatePinnedPublicKeyHashes().size() > 0;
    }

    /**
     * Determines SDK is enabled for trusted public PKI for authentication challenge. This read only value is within the JSON configuration file.
     */
    public boolean isEnabledTrustedPublicPKI() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().isAlsoTrustPublicPki();
    }

    /**
     * Determines if the client’s SSO is enabled or not. This value is read from JSON configuration, if there is no value defined in keychain.
     */
    public boolean isSsoEnabled() {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.PROP_SSO_ENABLED);
    }

    /**
     * Retrieves an endpoint path fragment for a given endpoint key, the keys can be one of the following
     * <ul>
     * <li>   msso.url.request_token</li>
     * <li>   msso.url.request_token_sso</li>
     * <li>   msso.url.register_device</li>
     * <li>   msso.url.register_device_client</li>
     * <li>   msso.url.resource_owner_logout</li>
     * <li>   msso.url.remove_device_x509</li>
     * <li>   msso.url.authorize</li>
     * <li>   msso.url.enterprise_apps</li>
     * <li>   msso.url.client_credentials</li>
     * <li>   msso.url.authorize</li>
     * <li>   mas.url.scim_path</li>
     * <li>   mas.url.mas_storage_path</li>
     * <li>   mas.url.user_info</li>
     * </ul>
     */
    public String getEndpointPath(String key) {
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(key);
    }

    public void setCertificateAdvancedRenewTimeframe(int numDays) {
        ConfigurationManager.getInstance().setCertificateAdvancedRenewTimeframe(numDays);
    }

    public int getCertificateAdvancedRenewTimeframe() {
        return ConfigurationManager.getInstance().getCertificateAdvancedRenewTimeframe();
    }

    /**
     * Adds the security configuration to the list of configurations.
     *
     * @param securityConfiguration the configuration to be added
     */
    public void add(MASSecurityConfiguration securityConfiguration) {
        if (securityConfiguration != null) {
            URL key = sanitizeURL(securityConfiguration.getHost());
            securityConfigurations.put(key, securityConfiguration);
            SECURITY_CONFIGURATION_CHANGED.notifyObservers(securityConfiguration.getHost());
        }
    }

    /**
     * Attempts to remove the security configuration from the list of configurations
     * with the host and port information.
     *
     * @param url the full URL
     */
    public void remove(URL url) {
        if (securityConfigurations != null) {
            URL key = sanitizeURL(url);
            MASSecurityConfiguration config = securityConfigurations.get(key);
            if (config != null && securityConfigurations.containsValue(config)) {
                securityConfigurations.remove(config);
                SECURITY_CONFIGURATION_CHANGED.notifyObservers(config.getHost());
            }
        }
    }

    /**
     * Finds a configuration in the list by the host name.
     *
     * @param url the full URL
     * @return the host and port URL
     */
    public MASSecurityConfiguration findByHost(URL url) {
        URL key = sanitizeURL(url);
        if (securityConfigurations != null) {
            return securityConfigurations.get(key);
        }
        return null;
    }

    /**
     * Strips the URL of everything but host and port information.
     *
     * @param url the full URL
     * @return the host and port URL
     */
    public static URL sanitizeURL(URL url) {
        String host = url.getHost();
        int port = url.getPort();

        try {
            return new URL(null, host, port, null);
        } catch (MalformedURLException e) {
            return null;
        }
    }
}
