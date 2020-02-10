/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import androidx.annotation.Keep;

import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.foundation.MASResponse;

/**
 * This exception is thrown when an authentication error occurs while accessing the MAG Server register or token endpoint.
 */
@Keep
public class AuthenticationException extends MAGServerException {

    public static final String INVALID_RESOURCE_OWNER_SUFFIX = "202";

    public AuthenticationException(MASResponse response, int errorCode, int status, String contentType, String detailMessage) {
        super(response, errorCode, status, contentType, detailMessage);
    }

    public AuthenticationException(MASResponse response, int errorCode, int status, String contentType, String detailMessage, Throwable throwable) {
        super(response, errorCode, status, contentType, detailMessage, throwable);
    }

    public AuthenticationException(MAGServerException e) {
        super(e);
    }
}
