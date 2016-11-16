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
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.creds.Credentials;
import com.ca.mas.core.datasource.DataSourceException;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;

/**
 * Top-level context used by mobile single-sign-on library.
 * An application would normally only need to create one instance of this class.
 * <p/>
 * The context must be configured and initialized before it can be used.
 * <p/>
 * The {@link #executeRequest(Bundle, MAGRequest)} method will process an outbound
 * web API request.  This may take a long time and involve multiple round trips to the token server, and should not
 * be executed on the UI thread.
 * To simplify running remote calls in the background from an Activity, the MssoClient and HttpResponseFragment
 * are provided.
 */
public class MssoContext {

    private static final String TAG = "MssoContext";

    /**
     * Maximum number of RetryRequestExceptions to honor before giving up.
     */
    private static final int MAX_REQUEST_ATTEMPTS = 4;

    private Context context;

    private ConfigurationProvider configurationProvider;
    private PolicyManager policyManager;
    private TokenManager tokenManager;

    private OAuthTokenContainer privateTokens;
    private ClientCredentialContainer clientCredentialTokens;

    private String deviceId;
    private String deviceName;

    private volatile MAGHttpClient magHttpClient;

    private volatile Credentials credentials;

    /**
     * Retain the container description.  If this app is not running in a container,
     * the value will be "".  If it is, then values include "-knox1", "-knox100", "-knox101"
     */
    protected String containerDescription = null;


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
    public void init(Context context) throws MssoException {
        this.context = context;
        this.configurationProvider = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider();

        StorageProvider storageProvider = new StorageProvider(context, configurationProvider);

        if (tokenManager == null) {
            tokenManager = storageProvider.createTokenManager();
        }

        if (privateTokens == null) {
            privateTokens = storageProvider.createOAuthTokenContainer();
        }

        if (clientCredentialTokens == null) {
            clientCredentialTokens = storageProvider.createClientCredentialContainer();
        }

        if (deviceId == null) {
            deviceId = generateDeviceId();
        }

        if (deviceName == null) {
            deviceName = android.os.Build.MODEL;
        }


    }

    public void initPolicyManager() {
        if (policyManager == null) {
            policyManager = new PolicyManager(this);
        }
        policyManager.init(context);
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
     * Set the token manager that will be used to persist keys, cert chains, and OAuth tokens.
     *
     * @param tokenManager the token manager to use, or null.
     */
    void setTokenManager(TokenManager tokenManager) {
        this.tokenManager = tokenManager;
    }

    /**
     * @return the device ID, or null if {@link #init} has not yet been called.
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * @return the device name, or null if {@link #init} has not yet been called.
     */
    public String getDeviceName() {
        return deviceName;
    }

    public boolean isSsoEnabled() {
        Boolean ssoEnabled = configurationProvider.getProperty(ConfigurationProvider.PROP_SSO_ENABLED);
        return ssoEnabled != null && ssoEnabled;
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

    public Credentials getCredentials() {
        return credentials;
    }

    public void setCredentials(Credentials credentials) {
        this.credentials = credentials;
    }

    public void clearCredentials() {
        Credentials cred = getCredentials();
        if (cred != null)
            cred.clear();
    }

    /**
     * Check if the device has already been registered.
     *
     * @return true if device registered has already completed and a client cert chain and ID token are present in the token store.
     * false if registration is required or unable to access the storage
     */
    public boolean isDeviceRegistered() {
        try {
            return tokenManager != null && tokenManager.isClientCertificateChainAvailable();
        } catch (DataSourceException e) {
            return false;
        }
    }

    /**
     * Reset the HTTP client, causing a new one to be created.
     * Use this if the trusted certificate or client cert configuration changes.
     */
    public void resetHttpClient() {
        magHttpClient = null;
    }

    /**
     * Get an HTTP client configured to trust server certs per our configuration, and to present a client
     * cert for mutual auth if one is available in our token store.
     *
     * @return an MAGHttpClient instance.  Never null.
     */
    public MAGHttpClient getMAGHttpClient() {
        MAGHttpClient client = magHttpClient;
        if (client != null)
            return client;

        client = new MAGHttpClient(context) {
            @Override
            protected void onConnectionObtained(HttpURLConnection connection) {
                super.onConnectionObtained(connection);
                String magIdentifier = getTokenManager().getMagIdentifier();
                if (magIdentifier != null) {
                    connection.setRequestProperty(ServerClient.MAG_IDENTIFIER, magIdentifier);
                }
            }
        };
        magHttpClient = client;
        return client;

    }

    /**
     * Notify that an ID token is now available.  May be called from any thread.
     * <p/>
     * Setting a non-null ID token has the side-effect of causing any cached password in memory to be destroyed.
     *
     * @param idToken the ID token.  Required.
     */
    public void onIdTokenAvailable(IdToken idToken) throws JWTValidationException {
        String deviceIdentifier = tokenManager.getMagIdentifier();
        String clientId = getClientId();
        String clientSecret = getClientSecret();

        if (idToken.getType().equals(IdToken.JWT_DEFAULT)) {
            if (JWTValidation.validateIdToken(idToken, deviceIdentifier, clientId, clientSecret)) {
                setIdToken(idToken);
            } else {
                throw new JWTValidationException(MAGErrorCode.TOKEN_INVALID_ID_TOKEN, "JWT Token is not valid");
            }
        } else {
            setIdToken(idToken);
        }

        clearCredentials();
    }

    /**
     * Notify that device registration has just completed.  May be called from any thread.
     */
    public void onDeviceRegistrationCompleted() {
        magHttpClient = null;
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
        privateTokens.saveAccessToken(accessToken, refreshToken, expiresInSec, grantedScope);
        if (accessToken != null)
            clearCredentials();
    }

    /**
     * Clear the access token, forcing the next request to obtain a new one.
     */
    public void clearAccessToken() {
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
    public MAGResponse executeRequest(Bundle extra, MAGRequest request) throws Exception {
        RequestInfo requestInfo = new RequestInfo(this, request, extra);
        MAGInternalRequest internalRequest = requestInfo.getRequest();

        MAGStateException lastError = null;
        for (; requestInfo.getNumAttempts() < MAX_REQUEST_ATTEMPTS; requestInfo.incrementNumAttempts()) {
            try {
                policyManager.processRequest(requestInfo);
                MAGResponse response;
                if (internalRequest.isLocalRequest()) {
                    response = ((LocalRequest) internalRequest.getRequest()).send(this);
                } else {
                    response = getMAGHttpClient().execute(internalRequest);
                }
                policyManager.processResponse(requestInfo, response);
                return response;
            } catch (MAGServerException e ) {
                rethrow(e);
            } catch (RetryRequestException e) {
                lastError = e;
                e.recover(this);
                Log.d(TAG, "Attempting to retry request");
            }
        }
        if (lastError != null && lastError.getCause() != null) {
            throw (Exception) lastError.getCause();
        }
        throw new IOException("Too many attempts, giving up: " + (lastError == null ? null : lastError.getMessage()));
    }

    /**
     * Handle common server error defined under
     * Git: MAS/Gateway-SK-MAG/blob/develop/apidoc/errorcodes/error_codes_overview.xml
     */
    private void rethrow(MAGServerException e) throws Exception {
        int errorCode = e.getErrorCode();
        String s = Integer.toString(errorCode);
        if (s.endsWith("201")) { //Invalid client - The given client credentials were not valid
            throw new InvalidClientCredentialException();
        }
        if (s.endsWith("202")) { //Invalid resource owner - The given resource owner credentials were not valid
            throw new AuthenticationException(e);
        }
        if (s.endsWith("206")) { //Invalid client Certificate - The given client certificate has expired
            throw new CertificateExpiredException(e);
        }
        throw e; //Cannot be handle on the client side, rethrow to caller
    }

    /**
     * Log out the current user (and, if SSO is enabled, all SSO apps on this device), leaving the device registered, and
     * optionally informing the token server of the logout.
     * <p/>
     * This method takes no action if the use is already logged out.
     * <p/>
     * t * This method destroys the access token and cached password (if any).  If SSO is enabled, this method also
     * removes the ID token from the shared token store and, if contactServer is true, makes a single best-effort
     * attempt to notify the server that the ID token should be invalidated.
     * <p/>
     * If the server needs to be contact, this will be done on the current thread.  As this may take some time,
     * callers running on the UI thread and passing true for contactServer should consider running this method
     * within an AsyncTask.
     * <p/>
     * <b>NOTE:</b> It is extremely important to make at least one attempt to inform the server
     * of the logout before destroying the tokens client side to try and prevent the server
     * from getting out of sync with the client.  If SSO is enabled, <b>pass contactServer=false only if
     * absolutely necessary</b> (such as to avoid blocking the GUI if you have already made
     * at least one attempt to contact the server).
     *
     * @param contactServer true to make a single best-effort attempt to notify the server of the logout so that
     *                      it can revoke the tokens.  This may fail if we lack network connectivity.
     *                      <p/>
     *                      false to destroy the tokens client side but make no effort to inform the server that
     *                      it needs to revoke them.
     *                      <p/>
     *                      This option is ignored unless SSO is enabled and an ID token exists.
     * @throws MssoException if contactServer is true, SSO is enabled, an ID token is present,
     *                       and there is an error while attempting to notify the server to log out the ID token or
     *                       error access the data source.
     */
    public void logout(boolean contactServer) throws MssoException {

        EventDispatcher.LOGOUT.notifyObservers();

        if (configurationProvider == null)
            throw new IllegalStateException("MssoContext not initialized, no configuration provider");
        if (tokenManager == null)
            throw new IllegalStateException("MssoContext not initialized, no token manager");
        final IdToken idToken = getIdToken();

        Exception exception = null;

        //Not allow to logout if the session is locked.
        byte[] secureIdToken =  tokenManager.getSecureIdToken();
        if (secureIdToken != null) {
            throw new SecureLockException("The session is currently locked.");
        }

        try {
            if (isSsoEnabled()) {
                try {
                    tokenManager.deleteIdToken();
                } catch (TokenStoreException e) {
                    exception = e;
                }

                try {
                    tokenManager.deleteSecureIdToken();
                } catch (TokenStoreException e) {
                    exception = e;
                }

                String clientId = getClientId();

                if (contactServer && idToken != null && clientId != null) {
                    try {
                        new OAuthClient(this).logout(idToken, clientId, getClientSecret(), true);
                    } catch (Exception e) {
                        throw new MssoException("Server logout failed:" + e.getMessage(), e);
                    }
                }
            }

            try {
                privateTokens.clear();
            } catch (DataSourceException e) {
                if (exception != null) {
                    exception = e;
                }
            }

            try {
                tokenManager.deleteUserProfile();
            } catch (TokenStoreException e) {
                if (exception != null) {
                    exception = e;
                }
            }

            if (exception != null) {
                throw new MssoException(exception);
            }

        } finally {
            setCredentials(null);
            resetHttpClient();
        }
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
    public void removeDeviceRegistration() throws MssoException {
        EventDispatcher.DE_REGISTER.notifyObservers();
        if (tokenManager == null)
            throw new IllegalStateException("MssoContext not initialized, no token manager");
        try {
            if (isDeviceRegistered()) {
                new RegistrationClient(this).removeDeviceRegistration();
            }
        } catch (Exception e) {
            Log.w(TAG, "Error in removing Device registration details from the server " + e);
        } finally {
            resetHttpClient();
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
    public void destroyAllPersistentTokens() throws MssoException {
        if (tokenManager == null)
            throw new IllegalStateException("MssoContext not initialized, no token manager");
        setCredentials(null);
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
    public void destroyPersistentTokens() throws MssoException {
        if (tokenManager == null)
            throw new IllegalStateException("MssoContext not initialized, no token manager");
        setCredentials(null);
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
     * Check the App has been logon.
     *
     * @return true if the access token has been acquired. False is access Token is not available.
     */
    public boolean isAppLogon() {
        return getAccessToken() != null;
    }

    /**
     * Check if the user has already been logon.
     *
     * @return true if the id token has been acquired and stored in the the device. false if the id token is not available.
     * For SSO disabled, id token is not issued by the server, check access token and refresh token instead.
     */
    public boolean isLogin() {
        return getIdToken() != null ||
                (!isSsoEnabled() && getAccessToken() != null && getRefreshToken() != null);
    }

    public String getUserProfile() {
        return tokenManager != null
                ? tokenManager.getUserProfile()
                : null;
    }

    /**
     * Logoff the App by clear the access token.
     *
     * @throws MssoException if there is an error while accessing the storage .
     */
    public void logoffApp() throws MssoException {
        try {
            clearAccessToken();
        } catch (DataSourceException e) {
            throw new MssoException(e);
        }
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

    /**
     * Generate device-id using ANDROID_ID, sharedUserId,container description if present and app signature.
     *
     * @return device-id
     */
    private String generateDeviceId() {
        return (new DeviceIdentifier(context)).toString();
    }

}

