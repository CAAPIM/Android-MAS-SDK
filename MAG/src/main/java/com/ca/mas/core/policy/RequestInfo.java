/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.request.MAGInternalRequest;

/**
 * Holds a request being processed by policy along with other metadata.
 */
public class RequestInfo {
    private final MAGInternalRequest request;
    private int numAttempts = 0;

    public RequestInfo(MssoContext context, MAGRequest request) {
        if (request == null)
            throw new NullPointerException("request");
        this.request = new MAGInternalRequest(context, request);
    }

    /**
     * @return the request object.  Never null.
     */
    public MAGInternalRequest getRequest() {
        return request;
    }

    /**
     * @return the number of attempts that have been made to send this request, not including the current attempt.
     */
    public int getNumAttempts() {
        return numAttempts;
    }

    /**
     * Record that another attempt has been made to send this request.
     */
    public void incrementNumAttempts() {
        numAttempts++;
    }

}
