/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.conf;

import com.ca.mas.core.MobileSsoConfig;

import java.util.List;

public class Config {

    //Server setting
    public static final Config HOSTNAME = new Config(true, MobileSsoConfig.PROP_TOKEN_HOSTNAME, "server.hostname", String.class);
    public static final Config PORT = new Config(false, MobileSsoConfig.PROP_TOKEN_PORT_HTTPS, "server.port", Integer.class);
    public static final Config PREFIX = new Config(false, MobileSsoConfig.PROP_TOKEN_URI_PREFIX, "server.prefix", String.class);
    public static final Config SERVER_CERTS = new Config(false, MobileSsoConfig.PROP_TRUSTED_CERTS_PEM, "server.server_certs", List.class);

    //Oauth.client
    public static final Config ORGANIZATION = new Config(true, MobileSsoConfig.PROP_ORGANIZATION, "oauth.client.organization", String.class);
    //Oauth.client.client_ids
    public static final Config CLIENT_KEY = new Config(true, MobileSsoConfig.PROP_CLIENT_ID, "oauth.client.client_ids.0.client_id", String.class);
    public static final Config CLIENT_SECRET = new Config(false, MobileSsoConfig.PROP_CLIENT_SECRET, "oauth.client.client_ids.0.client_secret", String.class);
    public static final Config SCOPE = new Config(false, MobileSsoConfig.PROP_OAUTH_SCOPE, "oauth.client.client_ids.0.scope", String.class);
    public static final Config REDIRECT_URI = new Config(false, MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI, "oauth.client.client_ids.0.redirect_uri", String.class);

    //Oauth.system_endpoints
    public static final Config AUTHORIZE_PATH = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_AUTHORIZE, "oauth.system_endpoints.authorization_endpoint_path", String.class);
    public static final Config REGISTER_TOKEN_PATH = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN, "oauth.system_endpoints.token_endpoint_path", String.class);
    public static final Config REGISTER_TOKEN_PATH_SSO = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN_SSO, "oauth.system_endpoints.token_endpoint_path", String.class);
    public static final Config LOGOUT_DEVICE_PATH = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_RESOURCE_OWNER_LOGOUT, "oauth.system_endpoints.usersession_logout_endpoint_path", String.class);
    public static final Config REVOKE_PATH = new Config(false, MobileSsoConfig.REVOKE_ENDPOINT, "oauth.system_endpoints.token_revocation_endpoint_path", String.class);

    //mag.system_endpoints
    public static final Config REMOVE_DEVICE_PATH = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REMOVE_DEVICE_X509, "mag.system_endpoints.device_remove_endpoint_path", String.class);
    public static final Config REGISTER_DEVICE_PATH = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REGISTER_DEVICE, "mag.system_endpoints.device_register_endpoint_path", String.class);
    public static final Config RENEW_DEVICE_PATH = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_RENEW_DEVICE, "mag.system_endpoints.device_renew_endpoint_path", String.class);
    public static final Config REGISTER_DEVICE_PATH_CLIENT = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REGISTER_DEVICE_CLIENT, "mag.system_endpoints.device_register_client_endpoint_path", String.class);
    public static final Config CLIENT_CREDENTIAL_INIT_PATH = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_CLIENT_CREDENTIALS, "mag.system_endpoints.client_credential_init_endpoint_path", String.class);
    public static final Config AUTHENTICATE_OTP_PATH = new Config(false, MobileSsoConfig.AUTHENTICATE_OTP_PATH, "mag.system_endpoints.authenticate_otp_endpoint_path", String.class);

    //mag.oauth_protected_endpoints
    public static final Config ENTERPRISE_APP_PATH = new Config(false, MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_ENTERPRISE_APPS, "mag.oauth_protected_endpoints.enterprise_browser_endpoint_path", String.class);

    //mag.mobile_sdk
    public static final Config SSO_ENABLED = new Config(false, MobileSsoConfig.PROP_SSO_ENABLED, "mag.mobile_sdk.sso_enabled", Boolean.class);
    public static final Config LOCATION_ENABLED = new Config(false, MobileSsoConfig.PROP_LOCATION_ENABLED, "mag.mobile_sdk.location_enabled", Boolean.class);
    public static final Config LOCATION_PROVIDER = new Config(false, MobileSsoConfig.PROP_LOCATION_PROVIDER_NAME, "mag.mobile_sdk.location_provider", String.class);
    public static final Config MSISDN_ENABLED = new Config(false, MobileSsoConfig.PROP_MSISDN_ENABLED, "mag.mobile_sdk.msisdn_enabled", Boolean.class);
    public static final Config TRUSTED_PUBLIC_PKI = new Config(false, MobileSsoConfig.PROP_TRUSTED_PUBLIC_PKI, "mag.mobile_sdk.trusted_public_pki", Boolean.class);
    public static final Config TRUSTED_CERT_PINNED_PUBLIC_KEY_HASHES = new Config(false, MobileSsoConfig.PROP_TRUSTED_CERT_PINNED_PUBLIC_KEY_HASHES, "mag.mobile_sdk.trusted_cert_pinned_public_key_hashes", List.class);
    public static final Config CLIENT_CERT_RSA_KEYBITS = new Config(false, MobileSsoConfig.PROP_CLIENT_CERT_RSA_KEYBITS, "mag.mobile_sdk.client_cert_rsa_keybits", Integer.class);
    public static final Config CLIENT_STORAGE = new Config(false, MobileSsoConfig.PROP_STORAGE, "mag.mobile_sdk.storage", String.class);

    //mag.ble
    public static final Config BLE_SERVICE_UUID = new Config(false, MobileSsoConfig.PROP_BLE_SERVICE_UUID, "mag.ble.msso_ble_service_uuid", String.class);
    public static final Config BLE_USER_SESSION_CHARACTERISTIC_UUID = new Config(false, MobileSsoConfig.PROP_BLE_CHARACTERISTIC_UUID, "mag.ble.msso_ble_characteristic_uuid", String.class);
    public static final Config BLE_RSSI = new Config(false, MobileSsoConfig.PROP_BLE_RSSI, "mag.ble.msso_ble_rssi", Integer.class);

    public static Config[] values = {
            HOSTNAME, PORT, PREFIX, SERVER_CERTS, ORGANIZATION, CLIENT_KEY, CLIENT_SECRET, SCOPE, REDIRECT_URI, AUTHORIZE_PATH, REGISTER_TOKEN_PATH, REGISTER_TOKEN_PATH_SSO, LOGOUT_DEVICE_PATH, REVOKE_PATH,
            REMOVE_DEVICE_PATH, REGISTER_DEVICE_PATH, RENEW_DEVICE_PATH, REGISTER_DEVICE_PATH_CLIENT, CLIENT_CREDENTIAL_INIT_PATH, ENTERPRISE_APP_PATH, SSO_ENABLED, LOCATION_ENABLED, LOCATION_PROVIDER,
            MSISDN_ENABLED, TRUSTED_PUBLIC_PKI, TRUSTED_CERT_PINNED_PUBLIC_KEY_HASHES, CLIENT_CERT_RSA_KEYBITS, CLIENT_STORAGE, BLE_SERVICE_UUID, BLE_USER_SESSION_CHARACTERISTIC_UUID,
            BLE_RSSI, AUTHENTICATE_OTP_PATH
    };

    public boolean mandatory;
    public String key;
    public String path;
    public Class type;

    /**
     * SDK Configuration
     *
     * @param mandatory
     * @param key       The Bundle Key
     * @param path      The path which may to the JSON Configuration.
     * @param type      Type of the attribute.
     */
    public Config(boolean mandatory, String key, String path, Class type) {
        this.mandatory = mandatory;
        this.key = key;
        this.path = path;
        this.type = type;
    }

}
