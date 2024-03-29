/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.conf;

import android.net.Uri;
import android.util.Base64;

import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.cert.PublicKeyHash;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGRuntimeException;
import com.ca.mas.foundation.FoundationConsts;

import org.json.JSONObject;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple configuration provider to use if something more elaborate is not required.
 */
public class DefaultConfiguration implements ConfigurationProvider {
    private final String clientId;
    private final String clientSecret;
    private final String tokenHost;
    private final String scope;
    private final String redirectUri;

    private final Map<String, Object> properties = new HashMap<>();
    private final JSONObject raw;
    private final Server server;

    private Map<String, String> operationUriSuffixes = new HashMap<String, String>() {{
        put(PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN, "/auth/oauth/v2/token");
        put(PROP_TOKEN_URL_SUFFIX_REQUEST_TOKEN_SSO, "/auth/oauth/v2/token");
        put(PROP_TOKEN_URL_SUFFIX_REGISTER_DEVICE, "/connect/device/register");
        put(PROP_TOKEN_URL_SUFFIX_RENEW_DEVICE, "/connect/device/renew");
        put(PROP_TOKEN_URL_SUFFIX_REGISTER_DEVICE_CLIENT, "/connect/device/register/client");
        put(PROP_TOKEN_URL_SUFFIX_RESOURCE_OWNER_LOGOUT, "/connect/session/logout");
        put(PROP_TOKEN_URL_SUFFIX_REMOVE_DEVICE_X509, "/connect/device/remove");
        put(PROP_TOKEN_URL_SUFFIX_AUTHORIZE, "/auth/oauth/v2/authorize");
        put(PROP_TOKEN_URL_SUFFIX_ENTERPRISE_APPS, "/connect/enterprise/browser");
        put(PROP_TOKEN_URL_SUFFIX_CLIENT_CREDENTIALS, "/connect/client/initialize");
        put(PROP_DEVICE_METADATA_PATH, "/connect/device/metadata");
    }};

    private List<X509Certificate> trustedCertificateAnchors = new ArrayList<X509Certificate>();
    private boolean alsoTrustPublicPki = true;
    private boolean allowSSLPinning = true;
    private Set<PublicKeyHash> trustedCertificatePinnedPublicKeyHashes = new HashSet<PublicKeyHash>();

    /**
     * Create a DefaultConfiguration that uses the specified token hostname, client ID, client secret,
     * and organization name.
     *
     * @param tokenHost      the token host, eg "oath.example.com".  Required.
     * @param tokenUriPrefix URI prefix of token server URIs, eg "/custom", or null to use the empty string.
     * @param clientId       the application's client id for the initial OAuth token request, eg "846955e8-a8fb-4bea-bdd7-a16b39770c3d".  Required.
     * @param clientSecret   the application's client secret for the initial OAuth token request, eg "6ed4ffcb-4110-4c68-b280-cda17f127374".  Required.
     * @param organization   the organization name for the O component of the client certificate DN, eg "Exampletronics Ltd".  Optional.
     * @param scope          the scope for the O component of the client certificate DN, eg "msso phone profile address email ".  Optional.
     * @param redirectUri    the redirectUri name for the O component of the client certificate DN, eg "camsso://com.ca.mas".  Optional.
     */
    public DefaultConfiguration(JSONObject raw, String tokenHost, Integer port, String tokenUriPrefix, String clientId, String clientSecret, String organization, String scope, String redirectUri) {
        if (tokenHost == null)
            throw new NullPointerException("tokenHost");
        if (clientId == null)
            throw new NullPointerException("clientId");
        if (tokenUriPrefix == null)
            tokenUriPrefix = "";
        this.raw = raw;
        this.clientId = clientId;
        if (clientSecret != null && clientSecret.trim().length() > 0) {
            this.clientSecret = clientSecret;
        } else {
            this.clientSecret = null;
        }
        this.tokenHost = tokenHost;
        this.scope = scope;
        this.redirectUri = redirectUri;
        putProperty(PROP_TOKEN_URI_PREFIX, tokenUriPrefix);
        putProperty(PROP_ORGANIZATION, organization);
        putProperty(PROP_SSO_ENABLED, true);
        putProperty(PROP_LOCATION_ENABLED, false);
        putProperty(PROP_TOKEN_PORT_HTTP, 8080);
        if (port == null) {
            port = 8443;
        }
        putProperty(PROP_TOKEN_PORT_HTTPS, port);
        putProperty(PROP_CLIENT_CERT_RSA_KEYBITS, 2048);
        putProperty(PROP_RESPONSE_BUFFERING_ENABLED, true);
        putProperty(PROP_RESPONSE_BUFFERING_MAX_SIZE, 10485760);
        this.server = new Server(getTokenHost(), getTokenPort(), getPrefix());
    }

    /**
     * Set whether public CAs recognized by the OS should be accepted as TLS server certs in addition
     * to the list returned by {@link #getTrustedCertificateAnchors()}.
     *
     * @param alsoTrustPublicPki true to trust public PKI CAs in additional to the list returned from {@link #getTrustedCertificateAnchors()}.
     *                           false to trust only the list returned from {@link #getTrustedCertificateAnchors()}.
     */
    public void setAlsoTrustPublicPki(boolean alsoTrustPublicPki) {
        this.alsoTrustPublicPki = alsoTrustPublicPki;
    }

    public void setAllowSSLPinning(boolean allowSSLPinning) {
        this.allowSSLPinning = allowSSLPinning;
    }

    /**
     * Add one or more trusted certificates to be returned by {@link #getTrustedCertificateAnchors()}.
     *
     * @param certs trusted certificates to add, in Base-64 format with or without PEM markers.
     * @throws MAGRuntimeException if PEM or X.509 decoding fails for at least one cert.
     */
    public void addTrustedCertificateAnchors(String... certs) throws MAGRuntimeException {
        for (String cert : certs) {
            try {
                trustedCertificateAnchors.add(CertUtils.decodeCertFromPem(cert));
            } catch (IOException e) {
                throw new MAGRuntimeException(MAGErrorCode.FAILED_JSON_VALIDATION, e);
            }
        }
    }

    /**
     * Add one or more pinned trusted certificate public keys to be returned by {@link #getTrustedCertificatePinnedPublicKeyHashes()}.
     *
     * @param hashes public key pin hashes to add, as hex-encoded SHA-256 hashes of the SubjectPublicKeyInfo structure from the certificate.
     */
    public void addTrustedCertificatePinnedPublicKeyHashes(String... hashes) {
        for (String hash : hashes) {
            trustedCertificatePinnedPublicKeyHashes.add(PublicKeyHash.fromHashString(hash, Base64.NO_WRAP | Base64.URL_SAFE));
        }
    }

    @Override
    public String getClientId() {
        return clientId;
    }

    @Override
    public String getClientSecret() {
        return clientSecret;
    }

    @Override
    public String getClientScope() {
        return getProperty(ConfigurationProvider.PROP_OAUTH_SCOPE);
    }

    @Override
    public <T> T getProperty(String propertyName) {
        //noinspection unchecked
        return (T) properties.get(propertyName);
    }

    /**
     * Set a configuration property.
     *
     * @param propertyName the property name.  Required.
     * @param value        the property value
     * @param <T>          the expected type of the property value
     */
    public <T> void putProperty(String propertyName, T value) {
        properties.put(propertyName, value);
    }

    @Override
    public JSONObject getRaw() {
        return raw;
    }

    @Override
    public Server getServer() {
        return server;
    }

    @Override
    public String getTokenHost() {
        return tokenHost;
    }

    public String getScope() {
        return scope;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    @Override
    public int getTokenPort() {
        Integer tokenPortHttps = getProperty(PROP_TOKEN_PORT_HTTPS);
        if (tokenPortHttps == null)
            return 8443;
        else {
            return tokenPortHttps;
        }
    }

    @Override
    public URI getTokenUri(String operation) {
        String suffix = getProperty(operation);
        if (suffix == null) {
            //Find the default path
            suffix = operationUriSuffixes.get(operation);
        }
        return getUri(suffix);
    }

    @Override
    public URI getUserInfoUri() {
        String userInfo = getProperty(FoundationConsts.KEY_CONFIG_USER_INFO);
        if (userInfo == null) {
            userInfo = "/openid/connect/v1/userinfo";
        }
        return getUri(userInfo);
    }

    @Override
    public URI getUri(String relativePath) {
        String suffix = relativePath;
        try {
            if (relativePath == null) {
                suffix = "";
            }
            if (isAbsolute(suffix))
                return new URL(suffix).toURI();

            return new URL("https", getTokenHost(), getTokenPort(), getPrefix() + suffix).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new MAGRuntimeException(MAGErrorCode.INVALID_ENDPOINT, "Unable to create URL for operation \"" + relativePath + "\": " + e.getMessage(), e);
        }
    }

    @Override
    public String getPrefix() {
        String prefix = getProperty(PROP_TOKEN_URI_PREFIX);
        if (prefix == null) {
            return "";
        }
        return prefix;
    }


    private static boolean isAbsolute(String suffix) {
        return suffix.toLowerCase().startsWith("https:") || suffix.toLowerCase().startsWith("http:");
    }

    @Override
    public Collection<X509Certificate> getTrustedCertificateAnchors() {
        return Collections.unmodifiableCollection(trustedCertificateAnchors);
    }

    @Override
    public boolean isAlsoTrustPublicPki() {
        return alsoTrustPublicPki;
    }

    @Override
    public boolean isSSLPinningAllowed() { return allowSSLPinning; }

    @Override
    public Collection<PublicKeyHash> getTrustedCertificatePinnedPublicKeyHashes() {
        return Collections.unmodifiableCollection(trustedCertificatePinnedPublicKeyHashes);
    }
}
