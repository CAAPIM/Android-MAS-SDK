/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import com.ca.mas.core.error.MAGErrorCode;

/**
 * This exception is thrown when the received JWT is expired.
 */
public class JWTExpiredException extends JWTValidationException {

    public JWTExpiredException() {
        super(MAGErrorCode.TOKEN_ID_TOKEN_EXPIRED);
    }

    public JWTExpiredException(String detailMessage) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_EXPIRED, detailMessage);
    }

    public JWTExpiredException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_EXPIRED, detailMessage, throwable);
    }

    public JWTExpiredException(Throwable throwable) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_EXPIRED, throwable);
    }
}
