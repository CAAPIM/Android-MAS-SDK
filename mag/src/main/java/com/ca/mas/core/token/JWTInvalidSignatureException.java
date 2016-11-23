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
 * This exception is thrown when the received JWT has invalid Signature.
 */
public class JWTInvalidSignatureException extends JWTValidationException {

    public JWTInvalidSignatureException() {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_SIGNATURE);
    }

    public JWTInvalidSignatureException(String detailMessage) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_SIGNATURE, detailMessage);
    }

    public JWTInvalidSignatureException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_SIGNATURE, detailMessage, throwable);
    }

    public JWTInvalidSignatureException(Throwable throwable) {
        super(MAGErrorCode.TOKEN_ID_TOKEN_INVALID_SIGNATURE, throwable);
    }
}
