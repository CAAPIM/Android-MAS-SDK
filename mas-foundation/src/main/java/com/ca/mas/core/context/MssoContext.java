/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.context;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.core.auth.AuthenticationException;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.datasource.DataSourceException;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.oauth.OAuthClient;
import com.ca.mas.core.policy.PolicyManager;
import com.ca.mas.core.policy.RequestInfo;
import com.ca.mas.core.policy.exceptions.CertificateExpiredException;
import com.ca.mas.core.policy.exceptions.InvalidClientCredentialException;
import com.ca.mas.core.policy.exceptions.RetryRequestException;
import com.ca.mas.core.registration.RegistrationClient;
import com.ca.mas.core.request.MAGInternalRequest;
import com.ca.mas.core.request.internal.LocalRequest;
import com.ca.mas.core.security.SecureLockException;
import com.ca.mas.core.store.ClientCredentialContainer;
import com.ca.mas.core.store.OAuthTokenContainer;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.core.store.TokenStoreException;
import com.ca.mas.core.token.ClientCredentials;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.JWTValidation;
import com.ca.mas.core.token.JWTValidationException;
import com.ca.mas.foundation.MASAuthCredentials;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;

import java.io.IOException;
import java.util.Date;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Top-level context used by mobile single-sign-on library.
 * An application would normally only need to create one instance of this class.
 * <p/>
 * The context must be configured and initialized before it can be used.
 * <p/>
 * The {@link #executeRequest(Bundle, MASRequest)} method will process an outbound
 * web API request.  This may take a long time and involve multiple round trips to the token server, and should not
 * be executed on the UI thread.
 * To simplify running remote calls in the background from an Activity, the MssoClient and HttpResponseFragment
 * are provided.
 */
public class MssoContext {

    /**
     * Maximum number of RetryRequestExceptions to honor before giving up.
     */
    private static final int MAX_REQUEST_ATTEMPTS = 4;

    private Context appContext;

    private ConfigurationProvider configurationProvider;
    private PolicyManager policyManager;
    private TokenManager tokenManager;

    private OAuthTokenContainer privateTokens;
    private ClientCredentialContainer clientCredentialTokens;

    private String deviceName;

    private volatile MAGHttpClient magHttpClient;

    private volatile MASAuthCredentials credentials;

    private static final String MSSO_CONTEXT_NOT_INITIALIZED = "MssoContext not initialized, no token manager.";

    private MssoContext() {
    }

    /**
     * Create a new MssoContext.
     * <p/>
     * The context must be initialized before it can be used.
     *
     * @return a new MssoContext instance.
     * @see #init
     */
    public static MssoContext newContext() {
        return new MssoContext();
    }

    /**
     * Check if this context has been initialized.  The context must be initialized before it can be used.
     *
     * @return true if this context has been initialized.
     * false if initialization is required.
     */
    public boolean isInitialized() {
        return configurationProvider != null;
    }

    /**
     * Initialize the context.
     * <p/>
     * This method must be called before the context can be used for the first time.
     * <p/>
     * It is safe to call this method more than once.
     * <p/>
     * If the client device ID has not already been set, this method will generate it.
     * <p/>
     * If the device name has not already been set, this method will set it to android.os.Build.MODEL.
     *
     * @param context a context, for accessing system services such as telephony and location.  Required.
     *                The context must have a lifetime at least as long as this MssoContext.
     *                Typically this means you should not pass an Activity as the context unless you
     *                plan to close this MssoContext when the activity is destroyed.
     * @throws MssoException if the token store cannot be prepared
     */
    public void init(Context context) {
        this.appContext = context.getApplicationContext();

        this.configurationProvider = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider();

        if (tokenManager == null) {
            tokenManager = StorageProvider.getInstance().getTokenManager();
        }

        if (privateTokens == null) {
            privateTokens = StorageProvider.getInstance().getOAuthTokenContainer();
        }

        if (clientCredentialTokens == null) {
            clientCredentialTokens = StorageProvider.getInstance().getClientCredentialContainer();
        }

        if (deviceName == null) {
            deviceName = android.os.Build.MODEL;
        }
    }

    public void initPolicyManager() {
        if (policyManager == null) {
            policyManager = new PolicyManager(this);
        }
        policyManager.init(appContext);
    }

    /**
     * Shut down the context.
     * <p/>
     * Any resources will be freed and items such as location listeners will be unregistered.
     */
    public void close() {
        if (policyManager != null) {
            policyManager.close();
        }
    }

    /**
     * Get the token manager that will be used to persist keys, cert chains, and OAuth tokens.
     *
     * @return the token manager, or null.
     */
    public TokenManager getTokenManager() {
        return tokenManager;
    }

    /**
     * @return the device name, or null if {@link #init} has not yet been called.
     */
    public String getDeviceName() {
        return deviceName;
    }

    private boolean isSsoEnabled() {
        Boolean ssoEnabled = configurationProvider.getProperty(ConfigurationProvider.PROP_SSO_ENABLED);
        return ssoEnabled != null && ssoEnabled;
    }

    public void clearUserProfile() {
        try {
            tokenManager.deleteUserProfile();
        } catch (TokenStoreException e) {
            throw new MssoException("Failed to remove User Profile: " + e.getMessage(), e);
        }
    }

    public void clearIdToken() {
        try {
            tokenManager.deleteIdToken();
        } catch (TokenStoreException e) {
            throw new MssoException("Failed to remove ID token: " + e.getMessage(), e);
        }
    }

    public IdToken getIdToken() {
        return isSsoEnabled() && tokenManager != null
                ? tokenManager.getIdToken()
                : null;
    }

    private void setIdToken(IdToken idToken) {
        if (isSsoEnabled()) {
            try {
                tokenManager.saveIdToken(idToken);
            } catch (TokenStoreException e) {
                throw new MssoException("Unable to store ID token: " + e.getMessage(), e);
            }
        }
    }

    public MASAuthCredentials getCredentials() {
        return credentials;
    }

    public void setCredentials(MASAuthCredentials credentials) {
        this.credentials = credentials;
    }

    public void clearCredentials() {
        MASAuthCredentials cred = getCredentials();
        if (cred != null)
            cred.clear();
        this.credentials = null;
    }

    /**
     * Check if the device has already been registered.
     *
     * @return true if device registered has already completed and a client cert chain and ID token are present in the token store.
     * false if registration is required or unable to access the storage
     */
    public boolean isDeviceRegistered() {
        try {
            return tokenManager != null && tokenManager.isClientCertificateChainAvailable() && tokenManager.getMagIdentifier() != null;
        } catch (DataSourceException e) {
            if (DEBUG) Log.w(TAG, "Device not registered: " + e);
            return false;
        }
    }

    /**
     * Reset the HTTP client, causing a new one to be created.
     * Use this if the trusted certificate or client cert configuration changes.
     */
    public void resetHttpClient() {
        MASConfiguration.SECURITY_CONFIGURATION_RESET.notifyObservers();
    }

    /**
     * Get an HTTP client configured to trust server certs per our configuration, and to present a client
     * cert for mutual auth if one is available in our token store.
     *
     * @return an MAGHttpClient instance.  Never null.
     */
    public MAGHttpClient getMAGHttpClient() {
        if (magHttpClient == null) {
            magHttpClient = new MAGHttpClient();
        }
        return magHttpClient;
    }

    /**
     * Notify that an ID token is now available.  May be called from any thread.
     * <p/>
     * Setting a non-null ID token has the side-effect of causing any cached password in memory to be destroyed.
     *
     * @param idToken the ID token.  Required.
     */
    public void onIdTokenAvailable(IdToken idToken) throws JWTValidationException {
        clearCredentials();
        String deviceIdentifier = tokenManager.getMagIdentifier();
        String clientId = getClientId();
        String clientSecret = getClientSecret();

        if (idToken.getType().equals(IdToken.JWT_DEFAULT)) {
            if (JWTValidation.validateIdToken(this,idToken, deviceIdentifier, clientId, clientSecret)) {
                setIdToken(idToken);
            } else {
                throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, "JWT Token is not valid");
            }
        } else {
            setIdToken(idToken);
        }

    }

    /**
     * Notify that device registration has just completed.  May be called from any thread.
     */
    public void onDeviceRegistrationCompleted() {
        resetHttpClient();
    }

    /**
     * Notify that an access token was just obtained.  May be called from any thread.
     * <p/>
     * Setting a non-null access token has the side-effect of causing any cached password in memory to be destroyed.
     *
     * @param accessToken  the access token that was obtained.  Required.
     * @param refreshToken the refresh token to use to obtain a new access token.  Required.
     * @param expiresInSec number of seconds until the access token should be considered expired.  Required.
     */
    public void onAccessTokenAvailable(String accessToken, String refreshToken, long expiresInSec, String grantedScope) {
        if (accessToken != null) {
            clearCredentials();
        }
        privateTokens.saveAccessToken(accessToken, refreshToken, expiresInSec, grantedScope);
    }

    /**
     * Clear the access token, forcing the next request to obtain a new one.
     */
    public void clearAccessToken() {
        privateTokens.clearAccessToken();
    }

    /**
     * Clears the access token and refresh token, leaving the ID token, if present.
     */
    public void clearAccessAndRefreshTokens() { 
        privateTokens.clear(); 
    }

    /**
     * Get an access token, if one is presently available.
     *
     * @return an access token, or null.
     */
    public String getAccessToken() {
        return privateTokens.getAccessToken();
    }

    /**
     * Get the access token expiry date, if one is available.
     *
     * @return the access token expiry date as milliseconds since the epoch, or 0 if not available.
     */
    public long getAccessTokenExpiry() {
        return privateTokens.getExpiry();
    }

    public String getGrantedScope() {
        return privateTokens.getGrantedScope();
    }

    /**
     * Take and consume the refresh token, if one is available.
     * <p/>
     * If a refresh token is returned, it is removed from the private token store as well.
     *
     * @return the refresh token, or null.
     */
    public String takeRefreshToken() {
        return privateTokens.takeRefreshToken();
    }

    public String getRefreshToken() {
        return privateTokens.getRefreshToken();
    }

    /**
     * Get the configuration provider.
     *
     * @return the configuration provider for this MSSO context.
     */
    public ConfigurationProvider getConfigurationProvider() {
        return configurationProvider;
    }

    /**
     * Add an access token to the specified outbound request, transmit it to the target server, and return
     * the response.
     * <p/>
     * This method may take a long time to execute and should not be invoked on the GUI thread.
     *
     * @param request the request to decorate and send.  Required.
     * @return the final response to this request.
     * @throws MAGStateException if the request cannot be completed in the current MSSO state
     *                           (for example, if a username and password must be provided, or if the token store needs to be unlocked).
     * @throws IOException       if there is an error communicating with the target server.
     */
    public MASResponse executeRequest(Bundle extra, MASRequest request) throws Exception {
        RequestInfo requestInfo = new RequestInfo(this, request, extra);
        final MAGInternalRequest internalRequest = requestInfo.getRequest();

        Exception lastError = null;
        for (; requestInfo.getNumAttempts() < MAX_REQUEST_ATTEMPTS; requestInfo.incrementNumAttempts()) {
            try {
                //Do not execute the policy if this request is targeting an unprotected endpoint.
                if (request.isPublic()) {
                    return getMAGHttpClient().execute(internalRequest);
                }

                return policyManager.execute(requestInfo, new PolicyManager.Route<MASResponse>() {
                    @Override
                    public MASResponse invoke() throws IOException {
                        if (internalRequest.isLocalRequest()) {
                            return ((LocalRequest) internalRequest.getRequest()).send(MssoContext.this);
                        } else {
                            return getMAGHttpClient().execute(internalRequest);
                        }
                    }
                });
            } catch (MAGServerException e) {
                //This catch system endpoint error.
                if (DEBUG)
                    Log.d(TAG, String.format("Server returned x-ca-err %d", e.getErrorCode()));
                try {
                    rethrow(e);
                } catch (RetryRequestException rre) {
                    lastError = rre;
                    rre.recover(this);
                    if (DEBUG) Log.d(TAG, "Attempting to retry request. " + e.getClass());
                }
            } catch (RetryRequestException e) {
                lastError = e;
                e.recover(this);
                if (DEBUG) Log.d(TAG, "Attempting to retry request. " + e.getClass());
            } catch (Exception e) {
                clearCredentials();
                throw e;
            }
        }
        //All retries failed
        clearCredentials();
        if (lastError != null) {
            throw lastError;
        }
        throw new IOException("Too many attempts, giving up");
    }

    /**
     * Handle common server error defined under
     * Git: MAS/Gateway-SK-MAG/blob/develop/apidoc/errorcodes/error_codes_overview.xml
     */
    private void rethrow(MAGServerException e) throws RetryRequestException, MAGServerException {
        //We cannot reuse the credential for retry
        if (getCredentials() != null && !getCredentials().isReusable()) {
            clearCredentials();
        }
        int errorCode = e.getErrorCode();
        String s = Integer.toString(errorCode);
        if (s.endsWith(InvalidClientCredentialException.INVALID_CLIENT_CREDENTIAL_SUFFIX)) { //Invalid client - The given client credentials were not valid
            throw new InvalidClientCredentialException(e);
        }
        if (s.endsWith(AuthenticationException.INVALID_RESOURCE_OWNER_SUFFIX)) { //Invalid resource owner - The given resource owner credentials were not valid
            clearCredentials();
            throw new AuthenticationException(e);
        }
        if (s.endsWith(CertificateExpiredException.CERTIFICATE_EXPIRED_SUFFIX)) { //Invalid client Certificate - The given client certificate has expired
            throw new CertificateExpiredException(e);
        }
        //Remove credentials for exception which cannot be handled on SDK
        clearCredentials();
        throw e; //Cannot be handle on the client side, rethrow to caller
    }

    /**
     * Remove this device registration from the server.  The token server will identify the device making the request
     * by its TLS client certificate.
     * <p/>
     * This does not affect the local cached access token,
     * cached username and password, or the shared token storage in any way.  The client will continue to attempt
     * to present its TLS client certificate on future calls to the token server or a web API endpoint.
     * <p/>
     * To destroy the client-side record of the device registration, call {@link #destroyAllPersistentTokens()}.
     * <p/>
     * The communication with the token server will occur on the current thread.  As this may take some time,
     * callers running on the UI thread should consider running this method within an AsyncTask.
     *
     * @throws MssoException if there is an error while attempting to tell the token server to unregister this device.
     */
    public void removeDeviceRegistration() {
        EventDispatcher.BEFORE_DEREGISTER.notifyObservers();
        if (tokenManager == null) {
            throw new IllegalStateException(MSSO_CONTEXT_NOT_INITIALIZED);
        }
        try {
            if (isDeviceRegistered()) {
                //Server call to remove the registration record, will throw an exception if failed
                new RegistrationClient(this).removeDeviceRegistration();
            }
            EventDispatcher.AFTER_DEREGISTER.notifyObservers();
            resetHttpClient();
        } catch (Exception e) {
            if (DEBUG)
                Log.w(TAG, "Error in removing device registration details from the server " + e);
            throw new MssoException(e);
        }
    }

    /**
     * Clear all tokens in the shared token store.
     * <p/>
     * <b>NOTE: You should not normally use this method.</b>
     * This method destroys the client private key, effectively un-registering the device, and should only be used
     * for testing or recovery purposes.
     * <p/>
     * If you just wish to log out the current SSO user see the {@link #logout} method instead.
     *
     * @throws MssoException if there is an error while accessing the storage .
     */
    public void destroyAllPersistentTokens() {
        if (tokenManager == null)
            throw new IllegalStateException(MSSO_CONTEXT_NOT_INITIALIZED);
        clearCredentials();
        try {
            privateTokens.clearAll();
            clientCredentialTokens.clearAll();
            tokenManager.clearAll();
        } catch (TokenStoreException | DataSourceException e) {
            throw new MssoException(e);
        } finally {
            resetHttpClient();
        }
    }

    /**
     * Clear all tokens in the shared token store which associate with the current connected gateway.
     * <p/>
     * <b>NOTE: You should not normally use this method.</b>
     * This method destroys the client private key, effectively un-registering the device, and should only be used
     * for testing or recovery purposes.
     * <p/>
     * If you just wish to log out the current SSO user see the {@link #logout} method instead.
     *
     * @throws MssoException if there is an error while accessing the storage .
     */
    public void destroyPersistentTokens() {
        if (tokenManager == null)
            throw new IllegalStateException(MSSO_CONTEXT_NOT_INITIALIZED);
        try {
            privateTokens.clear();
            clientCredentialTokens.clear();
            tokenManager.clear();
        } catch (TokenStoreException | DataSourceException e) {
            throw new MssoException(e);
        } finally {
            resetHttpClient();
        }
    }

    /**
     * Check if the user has already been logon.
     *
     * @return true if the id token has been acquired and stored in the the device. false if the id token is not available.
     * For SSO disabled, id token is not issued by the server, check access token and refresh token instead.
     */
    public boolean isLogin() {

        //The access token is granted by Client Credential if refresh token is null
        //Please refer to https://tools.ietf.org/html/rfc6749#section-4.4.3 for detail
        return getIdToken() != null || getRefreshToken() != null;

    }

    public void setClientCredentials(ClientCredentials clientCredentials) {
        clientCredentialTokens.saveClientCredentials(clientCredentials);
    }

    public String getStoredClientId() {
        return clientCredentialTokens.getClientId();
    }

    public String getClientId() {
        String clientId = clientCredentialTokens.getClientId();
        if (clientId == null) {
            return configurationProvider.getClientId();
        }
        return clientId;
    }

    public String getClientSecret() {
        String clientSecret = clientCredentialTokens.getClientSecret();
        if (clientSecret == null) {
            clientSecret = configurationProvider.getClientSecret();
        }
        return clientSecret;
    }

    public Long getClientExpiration() {
        return clientCredentialTokens.getClientExpiration();
    }

    public void clearClientCredentials() {
        clientCredentialTokens.clear();
    }

    public boolean isClientCredentialExpired(Long clientExpiration) {
        return clientExpiration != 0 && clientExpiration < new Date().getTime() / 1000;
    }

}

