/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.error;

import com.ca.mas.core.http.MAGResponse;

/**
 * This exception is thrown when the target application API return http status code which is not within
 * the range 200 - 299.
 */
public class TargetApiException extends Exception {

    private MAGResponse response;

    public TargetApiException(MAGResponse response) {
        this.response = response;
    }

    /**
     * @return The Http Response of the target API
     */
    public MAGResponse getResponse() {
        return response;
    }
}
