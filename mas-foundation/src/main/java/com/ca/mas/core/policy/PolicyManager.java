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
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Keeps track of policies that can be applied to requests.
 */
public class PolicyManager {

    private final MssoContext mssoContext;
    private final Object policySync = new Object();

    private final Map<String, List<MssoAssertion>> policys = new HashMap<>();
    private final StorageReadyAssertion storageReadyAssertion;
    private final SecureLockAssertion secureLockAssertion;
    private final ClientCredentialAssertion clientCredentialAssertion;
    private final DeviceRegistrationAssertion deviceRegistrationAssertion;
    private final AccessTokenAssertion accessTokenAssertion;
    private final LocationAssertion locationAssertion;
    private final TelephoneAssertion telephoneAssertion;
    private final CustomHeaderAssertion customHeaderAssertion;
    private final ResponseRecoveryAssertion responseRecoveryAssertion;
    private static final String DEF_KEY = "default";
    private final String endpointPath = MASConfiguration.getCurrentConfiguration().getEndpointPath(MobileSsoConfig.PROP_TOKEN_URL_SUFFIX_RESOURCE_OWNER_LOGOUT);

    public PolicyManager(MssoContext mssoContext) {
        List<MssoAssertion> defaultPolicy = new ArrayList<>();
        List<MssoAssertion> logoutPolicy = new ArrayList<>();

        this.mssoContext = mssoContext;
        storageReadyAssertion = new StorageReadyAssertion();
        secureLockAssertion = new SecureLockAssertion();
        clientCredentialAssertion = new ClientCredentialAssertion();
        deviceRegistrationAssertion = new DeviceRegistrationAssertion();
        accessTokenAssertion = new AccessTokenAssertion();
        locationAssertion = new LocationAssertion();
        telephoneAssertion = new TelephoneAssertion();
        customHeaderAssertion = new CustomHeaderAssertion();
        responseRecoveryAssertion = new ResponseRecoveryAssertion();

        defaultPolicy.add(storageReadyAssertion);
        defaultPolicy.add(secureLockAssertion);
        defaultPolicy.add(clientCredentialAssertion);
        defaultPolicy.add(deviceRegistrationAssertion);
        defaultPolicy.add(accessTokenAssertion);
        defaultPolicy.add(locationAssertion);
        defaultPolicy.add(telephoneAssertion);
        defaultPolicy.add(customHeaderAssertion);
        defaultPolicy.add(responseRecoveryAssertion);
        policys.put(DEF_KEY, defaultPolicy);

        logoutPolicy.add(storageReadyAssertion);
        logoutPolicy.add(secureLockAssertion);
        logoutPolicy.add(clientCredentialAssertion);
        logoutPolicy.add(deviceRegistrationAssertion);
        logoutPolicy.add(locationAssertion);
        logoutPolicy.add(responseRecoveryAssertion);
        policys.put(endpointPath, logoutPolicy);
    }

    /**
     * Initialize policies.
     *
     * @param sysContext Android context.  Required.
     */
    public void init(Context sysContext) {
        init(sysContext, policys.get(DEF_KEY));
    }

    private void init(Context sysContext, List<MssoAssertion> policy) {
        Context appContext = sysContext.getApplicationContext();
        for (MssoAssertion assertion : policy) {
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

        List<MssoAssertion> activePolicy = policys.get(DEF_KEY);

        String requestUrl = requestInfo.getRequest().getURL() == null ? "":requestInfo.getRequest().getURL().toString();

        if (requestUrl.contains(endpointPath)){
            activePolicy = policys.get(endpointPath);
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
        close(policys.get(DEF_KEY));
    }

    private void close(List<MssoAssertion> policy) {
        for (MssoAssertion assertion : policy) {
            assertion.close();
        }
    }
}
