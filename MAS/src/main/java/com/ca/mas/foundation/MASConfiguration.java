/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.conf.Config;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.foundation.util.FoundationConsts;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;

public class MASConfiguration {

    private static Config USERINFO = new Config(false, FoundationConsts.KEY_CONFIG_USER_INFO, "oauth.oauth_protected_endpoints.userinfo_endpoint_path", String.class);
    private static Config MAS_SCIM = new Config(false, FoundationConsts.KEY_CONFIG_SCIM_PATH, "mas.scim-path", String.class);
    private static Config MAS_STORAGE = new Config(false, FoundationConsts.KEY_CONFIG_CLOUD_STORAGE_PATH, "mas.mas-storage-path", String.class);

    private static Config APP_NAME = new Config(false, FoundationConsts.KEY_CONFIG_APP_NAME, "oauth.client.client_name", String.class);
    private static Config APP_ORGANIZATION = new Config(false, FoundationConsts.KEY_CONFIG_APP_ORGANIZATION, "oauth.client.organization", String.class);
    private static Config APP_REGISTERED_BY = new Config(false, FoundationConsts.KEY_CONFIG_APP_REGISTERED_BY, "oauth.client.registered_by", String.class);
    private static Config APP_DESCRIPTION = new Config(false, FoundationConsts.KEY_CONFIG_APP_DESCRIPTION, "oauth.client.description", String.class);
    private static Config APP_TYPE = new Config(false, FoundationConsts.KEY_CONFIG_APP_TYPE, "oauth.client.client_type", String.class);

    private static MASConfiguration current;


    public static MASConfiguration getCurrentConfiguration() {
        if (current == null) {
            throw new IllegalStateException("MAS.start() has not been invoked.");
        }
        return current;
    }

    private Context mContext;

    protected MASConfiguration(Context context) {
        this.mContext = context.getApplicationContext();
        ConfigurationManager.getInstance().init(mContext);
        ConfigurationManager.getInstance().setAppConfigs(Arrays.asList(USERINFO, MAS_SCIM, MAS_STORAGE, APP_NAME,
                APP_ORGANIZATION, APP_REGISTERED_BY, APP_DESCRIPTION, APP_TYPE));
        current = this;
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
        return ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTrustedCertificatePinnedPublicKeyHashes() != null;
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
}
