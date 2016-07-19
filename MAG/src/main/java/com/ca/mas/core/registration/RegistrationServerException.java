/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.registration;

import com.ca.mas.core.error.MAGServerException;

/**
 * This exception is thrown when an error occurs while accessing register endpoint.
 */
public class RegistrationServerException extends MAGServerException {

    public RegistrationServerException(int errorCode, int status, String contentType, String detailMessage) {
        super(errorCode, status, contentType, detailMessage);
    }

    public RegistrationServerException(int errorCode, int status, String contentType, String detailMessage, Throwable throwable) {
        super(errorCode, status, contentType, detailMessage, throwable);
    }

    public RegistrationServerException(MAGServerException e) {
        super(e);
    }
}
