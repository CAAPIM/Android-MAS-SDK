/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core;

/**
 * Configuration property names and data types.
 */
public interface MobileSsoConfig {

    // If you add any properties to this file, you must update MobileSsoFactory.createConfig()
    // or they will be ignored.

    /**
     * String.  The token server hostname or IP address, eg "oauth.example.com".  Required.
     */
    String PROP_TOKEN_HOSTNAME = "msso.token.hostname";

    /**
     * String.  URI prefix to include on token server URIs, eg "/someprefix".  Optional.
     */
    String PROP_TOKEN_URI_PREFIX = "msso.token.uri.prefix";

    /**
     * Integer.  HTTP port on token server, eg 8080.  Optional.
     */
    String PROP_TOKEN_PORT_HTTP = "msso.token.port.http";

    /**
     * Integer.  HTTPS port on token server, eg 8443.  Optional.
     */
    String PROP_TOKEN_PORT_HTTPS = "msso.token.port.https";

    /**
     * String.  The organization name to include in the client cert DN, eg "Exampletronics Ltd".  Required.
     */
    String PROP_ORGANIZATION = "msso.organization";

    /**
     * Integer.  The size in bits of the RSA keypair to generate for the client cert, eg 2048.  Optional.
     */
    String PROP_CLIENT_CERT_RSA_KEYBITS = "msso.cert.rsa.keybits";

    /**
     * String.  The OAuth scope string that should be requested when obtaining an access token that will be used
     * to consume service from an API endpoint.  If not provided, a default value will be used.
     */
    String PROP_OAUTH_SCOPE = "msso.oauth.scope";

    /**
     * String. The application's client id for the initial OAuth token request, eg "846955e8-a8fb-4bea-bdd7-a16b39770c3d".  Required.
     */
    String PROP_CLIENT_ID = "msso.oauth.client.id";

    /**
     * String.  The application's client secret for the initial OAuth token request, eg "6ed4ffcb-4110-4c68-b280-cda17f127374".
     */
    String PROP_CLIENT_SECRET = "msso.oauth.client.secret";

    /**
     * Boolean.  Check if single sign on should be used for this app.
     * <p/>
     * If single sign on is enabled, then we will check for a JWT in the token store and use it
     * if it is available; we will also save any JWT we obtain from the token server into the token store.
     * <p/>
     * If single sign on is not enabled, we will ignore any JWT in the token store and will not save any JWTs
     * to the token store.
     * <p/>
     * True if single sign on is enabled, and shared JWTs should be used.  False if shared JWTs should not be used.
     */
    String PROP_SSO_ENABLED = "msso.sso.enabled";

    /**
     * Boolean, default=true.  Check if responses will be fully read and buffered before the result receiver is notified
     * of the response.
     * <p/>
     * This can be set to false to pass the response back immediately, before its body has been read.
     * The receiver of the response can then stream a large response without buffering, but is then
     * responsible for ensuring they do not do so while blocking the UI thread.
     */
    String PROP_RESPONSE_BUFFERING_ENABLED = "msso.response.buffer.enabled";

    /**
     * Integer, default=10485760.  Maximum size of response that will be buffered in RAM if response buffering
     * is enabled.
     */
    String PROP_RESPONSE_BUFFERING_MAX_SIZE = "msso.response.buffer.maxSize";

    /**
     * Boolean.  Check if location information should be included in outbound requests.
     * <p/>
     * If location is enabled, outbound HTTP requests will include a "geo-location" HTTP header whose value
     * is in the format "latitude,longitude,extra".  The latitude and longitude will always be present
     * (as decimal numbers).  One or more additional comma-delimited values may be present after the longitude.
     *
     * True if location information should be included.  False if location information should not be included.
     */
    String PROP_LOCATION_ENABLED = "msso.location.enabled";

    /**
     * String.  Get the location provider to use if location is enabled.
     * <p/>
     * Location headers will not be included if the permission is lacking to use the specified location provider.
     * <p/>
     * The location provider name, ie "network", or null to allow the policy to select a default provider.
     */
    String PROP_LOCATION_PROVIDER_NAME = "msso.location.provider.name";

    /**
     * Boolean.  Controls whether public CAs recognized by the OS should be accepted as TLS server certs in addition
     * to the list returned by {@link #PROP_TRUSTED_CERTS_PEM}.
     * <p/>
     * true if public CAs known to the OS should be trusted for outbound TLS.
     * false if only the certs returned by {@link #PROP_TRUSTED_CERTS_PEM} should be trusted.
     */
    String PROP_TRUSTED_PUBLIC_PKI = "msso.trust.public.pki";

    /**
     * ArrayList of String.  Configures the server certificate trust anchors that should be trusted for outbound TLS.
     * <p/>
     * If {@link #PROP_TRUSTED_PUBLIC_PKI} is false then only TLS server certs on this list
     * (or directly or indirectly signed by certs on this list) will be trusted for outbound TLS.
     * <p/>
     * The value is an ArrayList of String where each entry is a certificate trust anchor in PEM format
     * (eg, starting with "-----BEGIN CERTIFICATE-----\n").
     */
    String PROP_TRUSTED_CERTS_PEM = "msso.trust.certs.pem";

    /**
     * ArrayList of String.  Controls whether TLS server certificate public key pinning is in use and, if so, what pinned
     * public key hashes to permit within server cert chains.
     * <p/>
     * This is only really useful if {@link #PROP_TRUSTED_PUBLIC_PKI} is true.  If you don't trust public
     * PKI certs and are relying on internal PKI or self-signed certificates built into the app then you probably
     * do not need to worry about certificate pinning.
     * <p/>
     * If this list is nonempty then a TLS server cert will be accepted only if one of the pinned public keys
     * appears somewhere in its certificate chain.
     * <p/>
     * This prevents CAs -- any public CA, not necessarily the CA that signed the server cert originally -- from being
     * able to create a forged server certificate for an arbitrary target domain.
     * <p/>
     * The value is an ArrayList of pinned server certificate public keys, expressed as hexadecimal Strings.
     * Each value is the lowercase hex dump of an SHA-256 hash of a SubjectPublicKeyInfo structure.
     * This value should be null if public key pinning should not be used.
     */
    String PROP_TRUSTED_CERT_PINNED_PUBLIC_KEY_HASHES = "msso.trust.certs.pins.sha256";

    // If you add any properties to this file, you must update MobileSsoFactory.createConfig()
    // or they will be ignored.

    /**
     * String.  URL suffix for token server's request_token endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to request an access_token/id_token (JWT)
     * using username/password credentials.
     * <p/>
     * If not specified, will default to "/auth/oauth/v2/token".
     */
    String PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN = "msso.url.request_token";

    /**
     * String.  URL suffix for token server's request_token_sso endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to request an access_token usin gan ID token (JWT).
     * <p/>
     * If not specified, will default to "/auth/oauth/v2/token" (the same as
     * the regular request_token endpoint).
     */
    String PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN_SSO = "msso.url.request_token_sso";

    /**
     * String.  URL suffix for token server's register_device endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to register the device the first time it is used.
     * <p/>
     * If not specified, will default to "/connect/device/register".
     */
    String PROP_TOKEN_URL_SUFFIX_REGISTER_DEVICE = "msso.url.register_device";

    /**
     * String.  URL suffix for token server's renew_device endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to renew the device if client certificate has expired.
     * <p/>
     * If not specified, will default to "/connect/device/renew".
     */
    String PROP_TOKEN_URL_SUFFIX_RENEW_DEVICE = "msso.url.renew_device";

    /**
     * String.  URL suffix for token server's register_device_client endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to register the device the first time it is used.
     * <p/>
     * If not specified, will default to "/connect/device/register/client".
     */
    String PROP_TOKEN_URL_SUFFIX_REGISTER_DEVICE_CLIENT = "msso.url.register_device_client";


    /**
     * String.  URL suffix for token server's resource_owner_logout endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to log out the current device
     * by invalidating the ID token (JWT).
     * <p/>
     * If not specified, will default to "/connect/session/logout".
     */
    String PROP_TOKEN_URL_SUFFIX_RESOURCE_OWNER_LOGOUT = "msso.url.resource_owner_logout";

    /**
     * String.  URL suffix for token server's remove_device_x509 endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to remove the current
     * device registration, identifying the device by its TLS client certificate.
     * <p/>
     * If not specified, will default to "/connect/device/remove".
     */
    String PROP_TOKEN_URL_SUFFIX_REMOVE_DEVICE_X509 = "msso.url.remove_device_x509";

    /**
     * String.  URL suffix for token server's authorize endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to request for Social login
     * using authorization_code credentials.
     * <p/>
     * If not specified, will default to "/auth/oauth/v2/authorize".
     */
    String PROP_TOKEN_URL_SUFFIX_AUTHORIZE = "msso.url.authorize";

    /**
     * String.  URL suffix for server's enterprise apps endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to request for
     * enterprise apps.
     * <p/>
     * If not specified, will default to "/connect/enterprise/browser".

     */
    String PROP_TOKEN_URL_SUFFIX_ENTERPRISE_APPS = "msso.url.enterprise_apps";


    /**
     * String. The redirect URI that provided to the third-party-login platform.
     * <p/>
     * If not specified, will default to null, and third-party-login will not be supported.
     */
    String PROP_AUTHORIZE_REDIRECT_URI = "msso.authorize.redirect.uri";

    /**
     * Boolean.  Check if MSISDN information should be included in the outbound requests.
     * <p/>
     * If MSISDN is enabled, outbound HTTP requests will include a "MSISDN" HTTP header whose value
     * is in the phone number string, the MSISDN for a GSM phone.  Return null if is unavailable
     *
     * True if MSISDN information should be included.  False if MSISDN information should not be included.
     */
     String PROP_MSISDN_ENABLED = "msso.msisdn.enabled";

    /**
     * String.  URL suffix for client credentials endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to request for
     * client credentials.
     * <p/>
     * If not specified, will default to "/connect/client/initialize".
     */
    String PROP_TOKEN_URL_SUFFIX_CLIENT_CREDENTIALS = "msso.url.client_credentials";


    /**
     * String.  URL suffix for authenticate otp endpoint.
     * Used with {@link #PROP_TOKEN_URI_PREFIX} to build the URL to request for
     * otp generation and delivery.
     */
    String AUTHENTICATE_OTP_PATH = "msso.url.auth_otp";

    /**
     * String. The Service UUID for BluetoothLe to support cross-platform user session sharing.
     */
    String PROP_BLE_SERVICE_UUID = "msso.ble.service.uuid";

    /**
     * String. The Characteristic UUID for BluetoothLe to support cross-platform user session sharing.
     */
    String PROP_BLE_CHARACTERISTIC_UUID = "msso.ble.characteristic.uuid";

    /**
     * Integer. The received signal strength indication for BluetoothLe to support cross-platform user
     * session sharing.
     */
    String PROP_BLE_RSSI = "msso.ble.rssi";

    /**
     * JSON String. This represents the storage (and its default properties) to use
     * by the SDK for persistence.
     * {@code
     *  "storage": {
     *      "class": "<Canonical name of the Storage class>",
     *      "share" "true/false"
     *  }
     * }
     */
    String PROP_STORAGE = "msso.storage";

    /**
     * String.  URL suffix for Device Metadata endpoint.
     * matches the device metadata scope in the msso_config file
     * If not specified, will default to "/connect/device/metadata".
     */
    String DEVICE_METADATA_PATH = "msso_device_metadata";

    // If you add any properties to this file, you must update MobileSsoFactory.createConfig()
    // or they will be ignored.

}
