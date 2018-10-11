/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import android.content.Context;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.foundation.MASResponse;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of policies that can be applied to requests.
 */
public class PolicyManager {

    private final MssoContext mssoContext;
    private final Object policySync = new Object();

    private final Map<String, List<MssoAssertion>> policies = new HashMap<>();
    private List<MssoAssertion> defaultPolicy;

    public PolicyManager(MssoContext mssoContext) {

        this.mssoContext = mssoContext;

        StorageReadyAssertion storageReadyAssertion = new StorageReadyAssertion();
        SecureLockAssertion secureLockAssertion = new SecureLockAssertion();
        ClientCredentialAssertion clientCredentialAssertion = new ClientCredentialAssertion();
        DeviceRegistrationAssertion deviceRegistrationAssertion = new DeviceRegistrationAssertion();
        AccessTokenAssertion accessTokenAssertion = new AccessTokenAssertion();
        LocationAssertion locationAssertion = new LocationAssertion();
        TelephoneAssertion telephoneAssertion = new TelephoneAssertion();
        CustomHeaderAssertion customHeaderAssertion = new CustomHeaderAssertion();
        ResponseRecoveryAssertion responseRecoveryAssertion = new ResponseRecoveryAssertion();

        defaultPolicy = Arrays.asList(
                storageReadyAssertion,
                secureLockAssertion,
                clientCredentialAssertion,
                deviceRegistrationAssertion,
                accessTokenAssertion,
                locationAssertion,
                telephoneAssertion,
                customHeaderAssertion,
                responseRecoveryAssertion);

        URI logout = ConfigurationManager
                .getInstance()
                .getConnectedGatewayConfigurationProvider()
                .getTokenUri(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_RESOURCE_OWNER_LOGOUT);

        URI revoke = ConfigurationManager
                .getInstance()
                .getConnectedGatewayConfigurationProvider()
                .getTokenUri(MobileSsoConfig.REVOKE_ENDPOINT);

        //Logout
        policies.put(logout.getPath(), Arrays.asList(
                storageReadyAssertion,
                secureLockAssertion,
                clientCredentialAssertion,
                locationAssertion,
                responseRecoveryAssertion
        ));

        //Revoke
        policies.put(revoke.getPath(), Arrays.asList(
                storageReadyAssertion,
                clientCredentialAssertion,
                locationAssertion,
                responseRecoveryAssertion));
    }

    /**
     * Initialize policies.
     *
     * @param sysContext Android context.  Required.
     */
    public void init(Context sysContext) {
        Context appContext = sysContext.getApplicationContext();
        for (MssoAssertion assertion : defaultPolicy) {
            assertion.init(mssoContext, appContext);
        }
    }

    /**
     * Process a request.  This will apply policies to the request, possibly calling the token server to
     * obtain additional information, possibly adding headers to the request.
     * <p/>
     * The request will not actually be sent on to the target system by this method.  The caller remains
     * responsible for doing that.
     *
     * @param request the request to process.  Required.
     * @throws MAGStateException if the request cannot be processed in the current MSSO engine state.
     * @throws MAGException      Exception occur in MAG Engine
     */
    private void processRequest(RequestInfo request, List<MssoAssertion> policy) throws MAGException, MAGServerException {
        // For now, we will serialize all policies to prevent things like device registration and token acquisition
        // from being attempted in parallel.
        synchronized (policySync) {
            for (MssoAssertion assertion : policy) {
                assertion.processRequest(mssoContext, request);
            }
        }
    }

    /**
     * Process a response.  This will apply policies to the response, possibly adjusting the MSSO state or
     * even calling the token server in the case of a failed response.
     *
     * @param request  the original request to which this is a response.  Required.
     * @param response the response to examine.  Required.
     * @throws MAGStateException only for a failed (non-200) response, if the nature of the response indicates that
     *                           the request cannot be processed in the current MSSO engine state (or should be retried).
     * @throws MAGException      Exception occur in MAG Engine
     */
    private void processResponse(RequestInfo request, MASResponse response, List<MssoAssertion> policy) throws MAGException, MAGServerException {
        // For now, we will serialize all policies
        synchronized (policySync) {
            for (MssoAssertion assertion : policy) {
                assertion.processResponse(mssoContext, request, response);
            }
        }
    }

    public MASResponse execute(RequestInfo requestInfo, Route<MASResponse> function) throws MAGException, MAGServerException, IOException {

        String path = requestInfo.getRequest().getURL() == null ? "" : requestInfo.getRequest().getURL().getPath();

        List<MssoAssertion> activePolicy = policies.get(path);
        if (activePolicy == null) {
            activePolicy = defaultPolicy;
        }

        processRequest(requestInfo, activePolicy);
        MASResponse response = function.invoke();
        processResponse(requestInfo, response, activePolicy);
        return response;
    }

    public interface Route<R> {
        R invoke() throws IOException;
    }


    public void close() {
        for (MssoAssertion assertion : defaultPolicy) {
            assertion.close();
        }
    }
}
