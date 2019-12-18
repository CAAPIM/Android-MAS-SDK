/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import android.content.Context;
import androidx.annotation.NonNull;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.foundation.MASResponse;

/**
 * Represents a policy that applies to outbound requests from the MSSO SDK.  Examples can include ensuring
 * that the device is registered, that TLS client auth is available, that location information is included,
 * that an access token is included.
 */
public interface MssoAssertion {

    /**
     * Initialize the policy.  This must be called, once, before the policy is applied to any messages.
     *
     * @param mssoContext the MSSO context.  Required.
     * @param sysContext Android context to use for initializaton.  Required.
     */
    void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext);

    /**
     * Apply the policy to the specified pending outbound request.
     * <p/>
     * This may trigger additional sub-requests, may modify the request or its headers,
     * and may fail if MSSO resources are required (such as a password from the user,
     * or the token store to be initialized or unlocked).
     *
     * @param mssoContext the MSSO context.  Required.
     * @param request the pending HTTP request.  Required.
     * @throws MAGStateException if the policy cannot be applied in the current MSSO engine state
     */
    void processRequest(MssoContext mssoContext, RequestInfo request) throws MAGStateException, MAGException, MAGServerException;

    /**
     * Apply the policy to the specified received response.
     * <p/>Mo
     * For a successful response, this should normally be quite fast.
     * <p/>
     * For an unsuccessful response, depending on the nature of the failure, this may take further action
     * and may even throw an MssoStateException.
     * <p/>
     * A successful response must never result in an exception from this method, because some application-level
     * behavior has presumably already occurred on the server as a result of the successful request.
     *
     * @param mssoContext the MSSO context.  Required.
     * @param request the original request.  Required.
     * @param response the HTTP response.  Required.
     */
    void processResponse(MssoContext mssoContext, RequestInfo request, MASResponse response) throws MAGStateException, MAGException, MAGServerException;

    /**
     * Shut down this policy, releasing any resources such as registered location callback etc.
     * <p/>
     * After this method is called the policy shall no longer be used to process messages.
     */
    void close();
}
