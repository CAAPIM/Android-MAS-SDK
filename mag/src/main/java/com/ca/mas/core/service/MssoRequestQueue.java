/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import android.os.Bundle;

import com.ca.mas.core.util.Functions;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents pending outbound requests.
 */
class MssoRequestQueue {

    private static final MssoRequestQueue INSTANCE = new MssoRequestQueue();

    // Input queue
    private final Map<Long, MssoRequest> inboundRequests = new LinkedHashMap<Long, MssoRequest>();

    private MssoRequestQueue() {
    }

    public static MssoRequestQueue getInstance() {
        return INSTANCE;
    }

    synchronized void addRequest(MssoRequest request) {
        inboundRequests.put(request.getId(), request);
    }

    synchronized MssoRequest getRequest(long requestId) {
        return inboundRequests.get(requestId);
    }

    synchronized MssoRequest takeRequest(long requestId) {
        return inboundRequests.remove(requestId);
    }

    /**
     * Atomically remove all pending requests that match the specified predicate.
     *
     * @param predicate a predicate to check whether a given request should be removed.  Required.
     */
    synchronized void removeMatching(Functions.Unary<Boolean, MssoRequest> predicate, Bundle data) {
        Iterator<MssoRequest> it = inboundRequests.values().iterator();
        while (it.hasNext()) {
            MssoRequest mssoRequest = it.next();
            if (predicate.call(mssoRequest)) {
                if (mssoRequest.getResultReceiver() != null) {
                    mssoRequest.getResultReceiver().send(MssoIntents.RESULT_CODE_ERR_CANCELED, data);
                }
                it.remove();
            }
        }
    }
}
