/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import android.os.Bundle;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.request.MAGInternalRequest;
import com.ca.mas.foundation.MASRequest;

/**
 * Holds a request being processed by policy along with other metadata.
 */
public class RequestInfo {
    private final MAGInternalRequest request;
    private int numAttempts = 0;
    private final Bundle extra;

    public RequestInfo(MssoContext context, MASRequest request, Bundle extra) {
        if (request == null)
            throw new NullPointerException("request");
        this.request = new MAGInternalRequest(context, request);
        this.extra = extra;
    }

    public Bundle getExtra() {
        return extra;
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
