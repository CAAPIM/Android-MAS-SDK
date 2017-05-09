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
 * Represents completed responses awaiting pickup.
 */
class MssoResponseQueue {
    private static final MssoResponseQueue INSTANCE = new MssoResponseQueue();

    // Output queue
    private final Map<Long, MssoResponse> outboundResponses = new LinkedHashMap<Long, MssoResponse>();

    private MssoResponseQueue() {
    }

    public static MssoResponseQueue getInstance() {
        return INSTANCE;
    }

    synchronized  void addResponse(MssoResponse response) {
        outboundResponses.put(response.getId(), response);
    }

    synchronized MssoResponse takeResponse(long responseId) {
        return outboundResponses.remove(responseId);
    }

    /**
     * Atomically remove all pending responses that match the specified predicate.
     *
     * @param predicate a predicate to check whether a given response should be removed.  Required.
     */
    synchronized void removeMatching(Functions.Unary<Boolean, MssoResponse> predicate, Bundle data) {
        Iterator<MssoResponse> it = outboundResponses.values().iterator();
        while (it.hasNext()) {
            MssoResponse mssoResponse = it.next();
            if (predicate.call(mssoResponse))
                if (mssoResponse.getRequest().getResultReceiver() != null) {
                    mssoResponse.getRequest().getResultReceiver().send(MssoIntents.RESULT_CODE_ERR_CANCELED, data);
                }

            it.remove();
        }
    }
}
