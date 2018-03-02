/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import com.ca.mas.foundation.MASResponse;

/**
 * Holds information about a response to an MSSO request that is awaiting pickup.
 */
class MssoResponse {
    private final MssoRequest request;
    private final MASResponse response;
    private final long creationTime = System.currentTimeMillis();

    MssoResponse(MssoRequest request, MASResponse response) {
        this.request = request;
        this.response = response;
    }

    public long getId() {
        return request.getId();
    }

    public MssoRequest getRequest() {
        return request;
    }

    public MASResponse getHttpResponse() {
        return response;
    }

    public long getCreationTime() {
        return creationTime;
    }
}
