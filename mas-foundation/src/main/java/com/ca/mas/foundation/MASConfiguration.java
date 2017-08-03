/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.content.Context;
import android.util.Log;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.conf.Config;
import com.ca.mas.core.conf.ConfigurationManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

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
    private static Map<String, MASSecurityConfiguration> securityConfigurations = new HashMap<>();

    public static MASConfiguration getCurrentConfiguration() {
        if (primary == null) {
            throw new IllegalStateException("MAS.start() has not been invoked.");
        }
        return primary;
    }

    protected MASConfiguration(Context context) {
        Context appContext = context.getApplicationContext();
        ConfigurationManager manager = ConfigurationManager.getInstance();
        manager.init(appContext);
        manager.setAppConfigs(Arrays.asList(USERINFO, MAS_SCIM, MAS_STORAGE, APP_NAME,
                APP_ORGANIZATION, APP_REGISTERED_BY, APP_DESCRIPTION, APP_TYPE));
        primary = this;
        //TODO
        SECURITY_CONFIGURATION_RESET.notifyObservers();
        //May need to Synchronize for concurrent request.
        //transform the msso config to MASSecurityConfiguration
        //Rebuild the map which store the MASSecurityConfiguration, a indicator may required to identify which is the one from msso_config
        //When rebuild the map, we don't want the old msso_config
        //Add the msso_config MASSecurityConfiguration to the map.
        for (MASSecurityConfiguration config : securityConfigurations.values()) {
            if (config.isPrimary()) {
                securityConfigurations.remove(config.getHost());
            }
        }

        MASSecurityConfiguration.Builder configBuilder = new MASSecurityConfiguration.Builder()
                .isPrimary(true)
                //Gateway by default is not public
                .isPublic(false)
                .host(getGatewayHostName())
                .trustPublicPKI(isEnabledTrustedPublicPKI());

        JSONObject jsonConfig = manager.getConnectedGatewayConfig();
        try {
            configBuilder = extractCertificates(configBuilder, jsonConfig);
            configBuilder = extractPublicKeyHashes(configBuilder, jsonConfig);
        } catch (JSONException e) {
            if (DEBUG) Log.e(TAG, "Failed to parse the MSSO config.");
        }

        MASSecurityConfiguration config = configBuilder.build();
        securityConfigurations.put(config.getHost(), config);
    }

    private MASSecurityConfiguration.Builder extractCertificates(MASSecurityConfiguration.Builder configBuilder, JSONObject jsonConfig) throws JSONException {
        JSONObject server = jsonConfig.getJSONObject("server");
        JSONArray serverCerts = server.getJSONArray("server_certs");
        int certsLength = serverCerts.length();
        MASSecurityConfiguration.Builder result = configBuilder;
        for (int i = 0; i < certsLength; i++) {
            JSONArray certStrings = serverCerts.getJSONArray(i);
            String certString = convertCertArrayToString(certStrings);
            result = addDecodedPemCertificate(configBuilder, certString);
        }

        return result;
    }

    private String convertCertArrayToString(JSONArray certStrings) throws JSONException {
        StringBuilder cert = new StringBuilder();
        int certLength = certStrings.length();
        for (int j = 0; j < certLength; j++) {
            cert.append(certStrings.get(j));
            if (j != certLength - 1) {
                cert.append('\n');
            }
        }
        return cert.toString();
    }

    private MASSecurityConfiguration.Builder addDecodedPemCertificate(MASSecurityConfiguration.Builder configBuilder, String certString) {
        try {
            X509Certificate cert = CertUtils.decodeCertFromPem(certString);
            configBuilder.add(cert);
        } catch (IOException e) {
            if (DEBUG) Log.e(TAG, "Failed to decode the PEM certificate.");
        }

        return configBuilder;
    }

    private MASSecurityConfiguration.Builder extractPublicKeyHashes(MASSecurityConfiguration.Builder configBuilder, JSONObject jsonConfig) throws JSONException {
        JSONObject oauth = jsonConfig.getJSONObject("mag");
        JSONObject mobileSdk = oauth.getJSONObject("mobile_sdk");
        JSONArray publicKeyHashes = mobileSdk.getJSONArray("trusted_cert_pinned_public_key_hashes");
        int hashLength = publicKeyHashes.length();
        for (int j = 0; j < hashLength; j++) {
            String hash = publicKeyHashes.getString(j);
            configBuilder.add(hash);
        }

        return configBuilder;
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

    //TODO MultiServer
    public void add(MASSecurityConfiguration securityConfiguration) {
        if (securityConfigurations == null) {
            securityConfigurations = new HashMap<>();
        }
        securityConfigurations.put(securityConfiguration.getHost(), securityConfiguration);
        SECURITY_CONFIGURATION_CHANGED.notifyObservers(securityConfiguration.getHost());
    }

    //TODO MultiServer
    public void removeSecurityConfiguration(String host) {
        if (securityConfigurations != null) {
            MASSecurityConfiguration config = securityConfigurations.get(host);
            if (config != null) {
                securityConfigurations.remove(config);
            }
        }
        SECURITY_CONFIGURATION_CHANGED.notifyObservers(host);
    }

    //TODO MultiServer
    public MASSecurityConfiguration findByHost(String host) {
        if (securityConfigurations != null) {
            return securityConfigurations.get(host);
        }
        return null;
    }
}
