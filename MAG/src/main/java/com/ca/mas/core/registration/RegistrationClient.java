/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.registration;

import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.auth.AuthenticationException;
import com.ca.mas.core.cert.CertUtils;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGRequestBody;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.io.Charsets;
import com.ca.mas.core.io.IoUtils;
import com.ca.mas.core.token.IdToken;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * Utility class that encapsulates talking to the token server into Java method calls.
 * This handles just the network protocol for communicating with the MAG server to register the device.
 * It does not deal with state management, token persistence, looking up credentials in the context, or anything other
 * higher-level issue.
 */

public class RegistrationClient extends ServerClient {

    private static final int INVALID_CLIENT_CREDENTIALS = 1000201;
    private static final int INVALID_RESOURCE_OWNER_CREDENTIALS = 1000202;

    public RegistrationClient(MssoContext mssoContext) {
        super(mssoContext);
    }


    /**
     * Represents the type of registration that occurred.
     */
    public enum DeviceStatus {
        /**
         * Streamlined activation has succeeded.  The device can be used immediately.
         */
        ACTIVATED,

        /**
         * Manual activation is required.  The device has been registered and will be activated at a later time.
         */
        REGISTERED
    }

    /**
     * Represents the result of a successful device registration.
     */
    public interface DeviceRegistrationResult {
        /**
         * @return the activation status (streamlined or manual)
         */
        DeviceStatus getDeviceStatus();

        /**
         * @return the opaque device identifier we have been assigned by the server, as a Base-64 string.
         */
        String getMagIdentifier();

        /**
         * @return the ID token we should use for single-sign-on, or null if one was not returned.
         */
        IdToken getIdToken();

        /**
         * @return the signed client certificate chain we should use from now on for TLS mutual auth.
         */
        X509Certificate[] getClientCertificateChain();
    }

    /**
     * Register a device with the token server.
     * <p/>
     * This will use the current MSSO context's configuration provider to look up the register_device URL.
     * <p/>
     * The current MSSO context's current HTTP client will be used to communicate with the token server.
     * <p/>
     * A caller of this method must already have obtained the user's credentials, generated a key pair,
     * and created a certificate signing request.
     *
     * @param certificateSigningRequest a PKCS#10 certificate signing request in raw binary form.  Required.
     * @param request                   the OAuth Request
     * @param clientId                  the client identifier for the app triggering registration.  Required.
     * @param clientSecret              the client secret for the app triggering registration.  Required.
     * @param deviceId                  the device unique identifier string (eg the IMEI number).  Required.
     * @param deviceName                the device name (eg "Joe's Nexus S").  Required.
     * @param createSession             true to create an SSO session at the same time as device registration.  An ID token will be returned in the result.
     *                                  false to register the device but create no SSO session.  No ID token will be returned in the result.
     * @return the result of contacting the token server to register the device, as a {@link DeviceRegistrationResult}.
     * @throws RegistrationServerException if there is an error response from the token server
     */
    public DeviceRegistrationResult registerDevice(@NonNull byte[] certificateSigningRequest,
                                                   @NonNull MAGRequest request,
                                                   @NonNull String clientId,
                                                   @NonNull String clientSecret,
                                                   @NonNull String deviceId,
                                                   @NonNull String deviceName, boolean createSession) throws RegistrationException, RegistrationServerException, AuthenticationException {
        if (request.getGrantProvider().getCredentials(mssoContext) == null)
            throw new NullPointerException("credentials");

        final URI tokenUri = request.getGrantProvider().getRegistrationPath(mssoContext);
        if (tokenUri == null)
            throw new RegistrationException(MAGErrorCode.DEVICE_NOT_REGISTERED, "No device registration URL is configured");

        MAGRequest.MAGRequestBuilder builder = new MAGRequest.MAGRequestBuilder(tokenUri);

        Map<String, List<String>> headers = request.getGrantProvider().getCredentials(mssoContext).getHeaders(mssoContext);
        if (headers != null) {
            for (String key : headers.keySet()) {
                if (headers.get(key) != null) {
                    for (String value : headers.get(key)) {
                        builder.header(key, value);
                    }
                }
            }
        }

        builder.header(CLIENT_AUTHORIZATION, "Basic " + IoUtils.base64(clientId + ":" + clientSecret, Charsets.ASCII));
        builder.header(DEVICE_ID, IoUtils.base64(deviceId, Charsets.ASCII));
        builder.header(DEVICE_NAME, IoUtils.base64(deviceName, Charsets.ASCII));
        if (request.getGrantProvider().isSessionSupported()) {
            builder.header(CREATE_SESSION, Boolean.toString(createSession));
        }
        builder.header(CERT_FORMAT, PEM);

        builder.post(MAGRequestBody.byteArrayBody(Base64.encode(certificateSigningRequest, Base64.DEFAULT)));

        MAGHttpClient httpClient = mssoContext.getMAGHttpClient();
        final MAGResponse response;
        try {
            response = httpClient.execute(builder.build());
        } catch (IOException e) {
            throw new RegistrationException(MAGErrorCode.DEVICE_NOT_REGISTERED, "Unable to post to register_device: " + e.getMessage(), e);
        }

        if (DEBUG) Log.d(TAG,
                String.format("%s response with status: %d",
                        request.getURL(),
                        response.getResponseCode()) );

        if (response.getResponseCode() != HttpURLConnection.HTTP_OK) {

            RegistrationServerException e = ServerClient.createServerException(response, RegistrationServerException.class);

            if (e.getErrorCode() == INVALID_CLIENT_CREDENTIALS) {
                mssoContext.clearClientCredentials();
            } else if (e.getErrorCode() == INVALID_RESOURCE_OWNER_CREDENTIALS) {
                throw new AuthenticationException(e);
            }
            throw e;
        }

        final DeviceStatus deviceStatus = findDeviceStatus(response);
        final String magIdentifier = findMagIdentifier(response);
        final IdToken idToken = findIdToken(response, createSession && request.getGrantProvider().isSessionSupported());

        MAGResponseBody<byte[]> responseEntity = response.getBody();
        if (responseEntity == null)
            throw new RegistrationException(MAGErrorCode.DEVICE_RECORD_IS_NOT_VALID, "register_device response did not contain an entity");

        byte[] chainBytes = responseEntity.getRawContent();
        if (chainBytes.length < 1)
            throw new RegistrationException(MAGErrorCode.DEVICE_RECORD_IS_NOT_VALID, "register_device response was empty");
        final X509Certificate[] chain = CertUtils.decodeCertificateChain(chainBytes);
        if (chain.length < 1)
            throw new RegistrationException(MAGErrorCode.DEVICE_RECORD_IS_NOT_VALID, "register_device response did not include a certificate chain");

        return new DeviceRegistrationResult() {
            @Override
            public DeviceStatus getDeviceStatus() {
                return deviceStatus;
            }

            @Override
            public String getMagIdentifier() {
                return magIdentifier;
            }

            @Override
            public IdToken getIdToken() {
                return idToken;
            }

            @Override
            public X509Certificate[] getClientCertificateChain() {
                return chain;
            }
        };
    }



    private static DeviceStatus findDeviceStatus(MAGResponse response) throws RegistrationException {
        final DeviceStatus deviceStatus;
        List<String> headers = (List<String>) response.getHeaders().get(DEVICE_STATUS);
        if (headers != null && headers.size() == 1) {
            final String value = headers.get(0);
            if (ACTIVATED.equalsIgnoreCase(value)) {
                deviceStatus = DeviceStatus.ACTIVATED;
            } else if (REGISTERED.equalsIgnoreCase(value)) {
                deviceStatus = DeviceStatus.REGISTERED;
            } else
                throw new RegistrationException(MAGErrorCode.DEVICE_RECORD_IS_NOT_VALID, "register_device response did not include a recognized device status.  Status was: " + value);
        } else {
            throw new RegistrationException(MAGErrorCode.DEVICE_RECORD_IS_NOT_VALID, "register_device response did not include exactly one device status header.");
        }
        return deviceStatus;
    }

    private String findMagIdentifier(MAGResponse response) throws RegistrationException {
        final String magIdentifier;
        List<String> headers = (List<String>) response.getHeaders().get(MAG_IDENTIFIER);
        if (headers != null && headers.size() == 1) {
            final String value = headers.get(0);

            // Decode just to ensure the value exists and can be decoded -- we'll store and use just the Base64
            byte[] decoded = Base64.decode(value, Base64.DEFAULT);
            if (decoded == null || decoded.length < 1)
                throw new RegistrationException(MAGErrorCode.REGISTRATION_WITHOUT_REQUIRED_PARAMETERS, "register_device response did not include a valid mag identifier.");

            magIdentifier = value;
        } else {
            throw new RegistrationException(MAGErrorCode.REGISTRATION_WITHOUT_REQUIRED_PARAMETERS, "register_device response did not include exactly one mag identifier header.");
        }
        return magIdentifier;
    }

    private IdToken findIdToken(MAGResponse response, boolean require) throws RegistrationException {
        List<String> idTokens = (List<String>) response.getHeaders().get(ID_TOKEN);
        List<String> idTokenTypes = (List<String>) response.getHeaders().get(ID_TOKEN_TYPE);
        if (idTokens == null || idTokens.size() != 1 || idTokenTypes == null || idTokenTypes.size() != 1) {
            if (require)
                throw new RegistrationException(MAGErrorCode.DEVICE_RECORD_IS_NOT_VALID, "register_device response did not include exactly one ID token and ID Token type header.");
            return null;
        }
        final String idToken = idTokens.get(0);
        if (idToken.trim().length() < 1)
            throw new RegistrationException(MAGErrorCode.DEVICE_RECORD_IS_NOT_VALID, "register_device response did not include a valid ID token.");

        final String idTokenType = idTokenTypes.get(0);
        if (idTokenType.trim().length() < 1)
            throw new RegistrationException(MAGErrorCode.DEVICE_RECORD_IS_NOT_VALID, "register_device response did not include a valid ID token type.");

        return new IdToken(idToken, idTokenType);
    }

    /**
     * Remove the device registration for the current device, both authorizing the removal via
     * TLS mutual authentication with the device's TLS client certificate private key.
     * <p/>
     * The server will also revoke all active access tokens as well as any ID token for this device.
     *
     * @throws RegistrationServerException if there is an error response from the token server
     * @throws RegistrationException       if there is an error other than a valid error JSON response from the token server
     */
    public void removeDeviceRegistration() throws RegistrationException, RegistrationServerException {

        MAGRequest.MAGRequestBuilder builder = new MAGRequest.MAGRequestBuilder(
                conf.getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_REMOVE_DEVICE_X509))
                .delete(null);

        MAGHttpClient httpClient = mssoContext.getMAGHttpClient();

        final MAGResponse response;
        try {
            response = httpClient.execute(builder.build());
        } catch (IOException e) {
            throw new RegistrationException(MAGErrorCode.DEVICE_COULD_NOT_BE_DEREGISTERED, "Unable to de-register device: " + e.getMessage(), e);
        }
        if (HttpURLConnection.HTTP_OK != response.getResponseCode()) {
            throw ServerClient.createServerException(response, RegistrationServerException.class);
        }
    }


}
