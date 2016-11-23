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
 * This exception is thrown when the received JWT has invalid AZP.
 */
public class JWTInvalidAZPException extends JWTValidationException {

    public JWTInvalidAZPException() {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_AZP);
    }

    public JWTInvalidAZPException(String detailMessage) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_AZP, detailMessage);
    }

    public JWTInvalidAZPException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_AZP, detailMessage, throwable);
    }

    public JWTInvalidAZPException(Throwable throwable) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_AZP, throwable);
    }
}
