/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import com.ca.mas.core.error.MAGServerException;

/**
 * This exception is thrown when an authentication error occurs while accessing the MAG Server register or token endpoint.
 */
public class AuthenticationException extends MAGServerException {

    public static final String INVALID_RESOURCE_OWNER_SUFFIX = "202";

    public AuthenticationException(int errorCode, int status, String contentType, String detailMessage) {
        super(errorCode, status, contentType, detailMessage);
    }

    public AuthenticationException(int errorCode, int status, String contentType, String detailMessage, Throwable throwable) {
        super(errorCode, status, contentType, detailMessage, throwable);
    }

    public AuthenticationException(MAGServerException e) {
        super(e);
    }
}
