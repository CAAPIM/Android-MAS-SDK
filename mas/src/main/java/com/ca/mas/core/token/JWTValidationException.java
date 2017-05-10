/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import com.ca.mas.core.error.MAGException;

/**
 * This exception is thrown when an error occur while validating the JWT
 */
public class JWTValidationException extends MAGException {
    public JWTValidationException(int errorCode) {
        super(errorCode);
    }

    public JWTValidationException(int errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    public JWTValidationException(int errorCode, String detailMessage, Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    public JWTValidationException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
