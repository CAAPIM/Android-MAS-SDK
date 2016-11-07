/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.context.MssoException;
import com.ca.mas.core.creds.Credentials;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.policy.exceptions.CredentialRequiredException;
import com.ca.mas.core.policy.exceptions.RetryRequestException;
import com.ca.mas.core.policy.exceptions.TokenStoreUnavailableException;
import com.ca.mas.core.registration.DeviceRegistrationAwaitingActivationException;
import com.ca.mas.core.registration.RegistrationClient;
import com.ca.mas.core.registration.RegistrationException;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.core.store.TokenStoreException;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.util.KeyUtils;

import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

/**
 * Ensures that the device is registered.
 * <p/>
 * This policy does nothing if the device is already registered.
 * <p/>
 * If device registration is required, this policy will generate a keypair and register the device.
 * CredentialRequiredException will be thrown if the user needs to be prompted for credentials.
 * TokenStoreUnavailableException will be thrown if the device needs to be unlocked.
 */
public class DeviceRegistrationAssertion implements MssoAssertion {
    private TokenManager tokenManager;

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) throws MssoException {
        this.tokenManager = mssoContext.getTokenManager();
        if (tokenManager == null)
            throw new NullPointerException("mssoContext.tokenManager");
        if (mssoContext.getConfigurationProvider() == null)
            throw new NullPointerException("mssoContext.configurationProvider");
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) throws MAGException, MAGServerException {
        if (mssoContext.isDeviceRegistered()) {
            if (tokenManager != null) {
                // Check if client certificate is expired
                Calendar cal = Calendar.getInstance();
                //cal.add(Calendar.YEAR, 5);
                Date date = cal.getTime();
                X509Certificate[] clientCerts = tokenManager.getClientCertificateChain();
                if (clientCerts != null && clientCerts.length > 0) {
                    X509Certificate certificate = clientCerts[0];
                    try {
                        certificate.checkValidity(date);
                    } catch (CertificateExpiredException | CertificateNotYetValidException e) {
                        if (e instanceof CertificateExpiredException) {
                            // Client certificate expired, try to renew
                            try {
                                renewDevice(mssoContext);
                            } catch (Exception e1){
                                if (e1 instanceof RegistrationException) {
                                    // Network error
                                    throw e1;
                                } else {
                                    // Other error, try re-registration
                                    mssoContext.destroyPersistentTokens();
                                    throw new RetryRequestException(e1);
                                }
                            }
                        }
                    }
                }
            }
            return;
        }

        boolean success = false;
        try {
            registerDevice(mssoContext, request);
            success = true;
        } finally {
            // If registration fails, clear any cached credentials so the user will be prompted again.
            if (!success || (mssoContext.getCredentials() != null && !mssoContext.getCredentials().isReuseable())) {
                mssoContext.setCredentials(null);
            }
        }
    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MAGResponse response) throws MAGStateException {
        // Nothing to do here
    }

    private void renewDevice(MssoContext mssoContext) throws MAGException, MAGServerException {
        try {
            X509Certificate[] certificates = new RegistrationClient(mssoContext).renewDevice();
            tokenManager.saveClientCertificateChain(certificates);
        } catch (TokenStoreException e){
            throw new TokenStoreUnavailableException(e);
        }

        mssoContext.resetHttpClient();
    }

    private void registerDevice(MssoContext mssoContext, RequestInfo request) throws MAGException, MAGServerException {
        // Ensure token store is available
        if (!tokenManager.isTokenStoreReady())
            throw new TokenStoreUnavailableException();

        // Ensure credentials are available
        Credentials creds = request.getRequest().getGrantProvider().getCredentials(mssoContext);
        if (creds == null)
            throw new CredentialRequiredException();
        if (!creds.isValid())
            throw new CredentialRequiredException();

        // Perform device registration
        KeyPair keyPair = tokenManager.getClientKeyPair();
        if (keyPair == null) {
            Integer keyBits = mssoContext.getConfigurationProvider().getProperty(ConfigurationProvider.PROP_CLIENT_CERT_RSA_KEYBITS);
            if (keyBits == null)
                keyBits = 1024;
            keyPair = KeyUtils.generateRsaKeyPair(keyBits);
            try {
                tokenManager.saveClientKeyPair(keyPair);
            } catch (TokenStoreException e) {
                throw new TokenStoreUnavailableException(e);
            }
        }

        final String deviceId = mssoContext.getDeviceId();
        final String deviceName = mssoContext.getDeviceName();
        byte[] csrBytes;
        try {
            String organization = mssoContext.getConfigurationProvider().getProperty(ConfigurationProvider.PROP_ORGANIZATION);
            csrBytes = CertUtils.generateCertificateSigningRequest(creds.getUsername(), deviceId, deviceName, organization, keyPair);
        } catch (CertificateException e) {
            throw new RegistrationException(MAGErrorCode.DEVICE_NOT_REGISTERED, e);
        }

        // Ensure fresh HTTP client is used for registration
        mssoContext.resetHttpClient();

        Boolean ssoEnabled = mssoContext.getConfigurationProvider().getProperty(ConfigurationProvider.PROP_SSO_ENABLED);
        boolean createSession = ssoEnabled != null && ssoEnabled;
        final String clientId = mssoContext.getClientId();
        final String clientSecret = mssoContext.getClientSecret();
        RegistrationClient.DeviceRegistrationResult result = new RegistrationClient(mssoContext).registerDevice(csrBytes, request.getRequest(), clientId, clientSecret, deviceId, deviceName, createSession);

        final IdToken idToken = result.getIdToken();

        try {
            tokenManager.saveClientCertificateChain(result.getClientCertificateChain());
            tokenManager.saveMagIdentifier(result.getMagIdentifier());
            // MssoContext will take care of persisting the ID token if SSO is enabled
        } catch (TokenStoreException e) {
            throw new TokenStoreUnavailableException(e);
        }

        mssoContext.onDeviceRegistrationCompleted();
        if (idToken != null)
            mssoContext.onIdTokenAvailable(idToken);

        if (RegistrationClient.DeviceStatus.REGISTERED.equals(result.getDeviceStatus())) {
            throw new DeviceRegistrationAwaitingActivationException();
        }
    }

    @Override
    public void close() {
    }
}
