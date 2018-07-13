/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import android.content.Context;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.foundation.MASResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of policies that can be applied to requests.
 */
public class PolicyManager {

    private final MssoContext mssoContext;
    private final List<MssoAssertion> defaultPolicy = new ArrayList<>();
    private final List<MssoAssertion> logoutPolicy = new ArrayList<>();
    private final Object policySync = new Object();

    public PolicyManager(MssoContext mssoContext) {
        this.mssoContext = mssoContext;

        //Default Policy setting
        defaultPolicy.add(new StorageReadyAssertion());
        defaultPolicy.add(new SecureLockAssertion());
        defaultPolicy.add(new ClientCredentialAssertion());
        defaultPolicy.add(new DeviceRegistrationAssertion());
        defaultPolicy.add(new AccessTokenAssertion());
        defaultPolicy.add(new LocationAssertion());
        defaultPolicy.add(new TelephoneAssertion());
        defaultPolicy.add(new CustomHeaderAssertion());
        defaultPolicy.add(new ResponseRecoveryAssertion());

        logoutPolicy.add(new StorageReadyAssertion());
        logoutPolicy.add(new SecureLockAssertion());
        logoutPolicy.add(new ClientCredentialAssertion());
        logoutPolicy.add(new LocationAssertion());
        logoutPolicy.add(new TelephoneAssertion());
        logoutPolicy.add(new CustomHeaderAssertion());
        logoutPolicy.add(new ResponseRecoveryAssertion());
    }

    /**
     * Initialize policies.
     *
     * @param sysContext Android context.  Required.
     */
    public void init(Context sysContext) {
        init(sysContext, defaultPolicy);
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

        MASResponse response = null;
        if (requestInfo.getExtra() != null && Boolean.parseBoolean((String) requestInfo.getExtra().get("logout"))) {
            processRequest(requestInfo, logoutPolicy);
            response = function.invoke();
            processResponse(requestInfo, response, logoutPolicy);
        } else {

            processRequest(requestInfo, defaultPolicy);
            response = function.invoke();
            processResponse(requestInfo, response, defaultPolicy);
        }

        return response;
    }

    public interface Route<R> {
        R invoke() throws IOException;
    }


    public void close() {
        close(defaultPolicy);
    }

    private void close(List<MssoAssertion> policy) {
        for (MssoAssertion assertion : policy) {
            assertion.close();
        }
    }
}
