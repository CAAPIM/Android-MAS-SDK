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
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.policy.exceptions.SecureLockAssertion;

import java.util.ArrayList;
import java.util.List;

/**
 * Keeps track of policies that can be applied to requests.
 */
public class PolicyManager {

    private final MssoContext mssoContext;
    private final List<MssoAssertion> policies = new ArrayList<MssoAssertion>();
    private final Object policySync = new Object();

    public PolicyManager(MssoContext mssoContext) {
        this.mssoContext = mssoContext;

        List<String> customPolicies = mssoContext.getConfigurationProvider().getProperty(MobileSsoConfig.PROP_ADD_CUSTOM_POLICIES);
        if (customPolicies != null && !customPolicies.isEmpty()) {
            for (String policy : customPolicies) {
                try {
                    policies.add((MssoAssertion) Class.forName(policy).newInstance());
                } catch (Exception e) {
                    throw new IllegalArgumentException("Unable to initialize policy: " + policy, e);
                }
            }
        }
        //Default Policy setting
        policies.add(new SecureLockAssertion());
        policies.add(new ClientCredentialAssertion());
        policies.add(new DeviceRegistrationAssertion());
        policies.add(new AccessTokenAssertion());
        policies.add(new LocationAssertion());
        policies.add(new TelephoneAssertion());
        policies.add(new OtpAssertion());
    }

    /**
     * Initialize policies.
     *
     * @param sysContext Android context.  Required.
     */
    public void init(Context sysContext) {
        for (MssoAssertion policy : policies) {
            policy.init(mssoContext, sysContext);
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
     * @throws MAGException Exception occur in MAG Engine
     */
    public void processRequest(RequestInfo request) throws MAGStateException, MAGException, MAGServerException{
        // For now, we will serialize all policies to prevent things like device registration and token acquisition
        // from being attempted in parallel.
        synchronized (policySync) {
            for (MssoAssertion policy : policies) {
                policy.processRequest(mssoContext, request);
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
     *                            the request cannot be processed in the current MSSO engine state (or should be retried).
     * @throws MAGException Exception occur in MAG Engine
     */
    public void processResponse(RequestInfo request, MAGResponse response) throws MAGException , MAGStateException, MAGServerException{
        // For now, we will serialize all policies
        synchronized (policySync) {
            for (MssoAssertion policy : policies) {
                policy.processResponse(mssoContext, request, response);
            }
        }
    }

    public void close() {
        for (MssoAssertion policy : policies) {
            policy.close();
        }
    }
}
