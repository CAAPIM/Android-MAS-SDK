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
 * This exception is thrown when the received JWT has invalid AUD.
 */
public class JWTInvalidAUDException extends JWTValidationException {
    public JWTInvalidAUDException() {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_AUD);
    }

    public JWTInvalidAUDException(String detailMessage) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_AUD, detailMessage);
    }

    public JWTInvalidAUDException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_AUD, detailMessage, throwable);
    }

    public JWTInvalidAUDException(Throwable throwable) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_AUD, throwable);
    }
}
