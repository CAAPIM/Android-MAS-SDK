/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import com.ca.mas.core.util.Functions;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents pending active requests.
 */
class MssoActiveQueue {

    private static final MssoActiveQueue INSTANCE = new MssoActiveQueue();

    // Input queue
    private final Map<Long, MssoRequest> activeRequests = Collections.synchronizedMap(new LinkedHashMap<Long, MssoRequest>());

    private MssoActiveQueue() {
    }

    public static MssoActiveQueue getInstance() {
        return INSTANCE;
    }

    Collection<MssoRequest> getAllRequest() {
        return activeRequests.values();
    }

    void addRequest(MssoRequest request) {
        activeRequests.put(request.getId(), request);
    }

    MssoRequest getRequest(long requestId) {
        return activeRequests.get(requestId);
    }

    MssoRequest takeRequest(long requestId) {
        return activeRequests.remove(requestId);
    }

    /**
     * Atomically remove all pending requests that match the specified predicate.
     *
     * @param predicate a predicate to check whether a given request should be removed.  Required.
     */
    synchronized void removeMatching(Functions.Unary<Boolean, MssoRequest> predicate) {
        Iterator<MssoRequest> it = activeRequests.values().iterator();
        while (it.hasNext()) {
            MssoRequest mssoRequest = it.next();
            if (predicate.call(mssoRequest)) {
                if (mssoRequest.getResultReceiver() != null) {
                    mssoRequest.getResultReceiver().send(MssoIntents.RESULT_CODE_ERR_CANCELED, null);
                }
                it.remove();
            }
        }
    }
}
