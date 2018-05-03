/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core;

import android.os.Bundle;

import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;

/**
 * A interceptor is an object that intercepts the response of a resource.
 */
public interface ResponseInterceptor {

    /**
     * Intercept the response of a resource
     *
     * @param originalRequest The original API request
     * @param response        The response of the API request.
     * @return True to keep the request in the queue for later retry, False to remove the request from the queue
     */
    boolean intercept(long requestId, MASRequest originalRequest, Bundle requestExtra, MASResponse<?> response);

}
