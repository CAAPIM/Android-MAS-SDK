/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.oauth;

import com.ca.mas.core.error.MAGException;

/**
 * This exception is thrown when an error occurs while processing the OAuth Request.
 */
public class OAuthException extends MAGException {

    public OAuthException(int errorCode) {
        super(errorCode);
    }

    public OAuthException(int errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    public OAuthException(int errorCode, String detailMessage, Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    public OAuthException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
