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
import android.util.Log;

import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.clientcredentials.ClientCredentialsClient;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.policy.exceptions.InvalidClientCredentialException;
import com.ca.mas.core.token.ClientCredentials;

import java.util.UUID;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

class ClientCredentialAssertion implements MssoAssertion {

    public static final String INVALID_CLIENT_CREDENTIALS_ERROR_CODE_SUFFIX = "201";

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        if (mssoContext.getConfigurationProvider() == null)
            throw new NullPointerException("ConfigurationProvider is null");
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) throws MAGException, MAGServerException {

       String configuredClientSecret = mssoContext.getConfigurationProvider().getClientSecret();
        String configuredClientId = mssoContext.getConfigurationProvider().getClientId();

        if (configuredClientSecret != null && configuredClientSecret.trim().length() > 0) {
            //Configured Client ID cannot be null, it is mandatory in the configuration.
            if (!configuredClientId.equals(configuredClientSecret)) {
                //It is not a master key, do not hit the initialize endpoint
                if (DEBUG) Log.d(TAG, "Using static client id and client secret");
                return;
            }
        }

        //OR the client ID does not exist. Due to unset the the device pin, the key to decrypt the clientID may be empty.
        //May not necessary to check the client id, the client expiration check may be good enough
        if (mssoContext.isClientCredentialExpired(mssoContext.getClientExpiration()) ||
                mssoContext.getStoredClientId() == null) {
            try {
                if (DEBUG) Log.d(TAG, "Retrieve dynamic Client Credentials");
                String uuid = UUID.randomUUID().toString();
                ClientCredentials result = new ClientCredentialsClient(mssoContext).
                        getClientCredentials(configuredClientId, uuid, mssoContext.getDeviceId());
                if (DEBUG) Log.d(TAG, String.format("Client id: %s", result.getClientId()));
                mssoContext.setClientCredentials(result);
                return;
            } catch (NullPointerException e) {
                throw new IllegalArgumentException("Please check your configurations. One or more configuration is wrong or incomplete");
            }
        }

        if (DEBUG) Log.d(TAG, String.format("Client id: %s", mssoContext.getStoredClientId()));

    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MAGResponse response) throws MAGStateException {
        int errorCode = ServerClient.findErrorCode(response);
        if (errorCode == -1) {
            return;
        }
        String s = Integer.toString(errorCode);
        if (s.endsWith(INVALID_CLIENT_CREDENTIALS_ERROR_CODE_SUFFIX)) {
            throw new InvalidClientCredentialException("Client is rejected by server");
        }
    }

    @Override
    public void close() {
    }
}
