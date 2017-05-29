/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.clientcredentials;

import com.ca.mas.core.error.MAGException;

/**
 * This exception is thrown when an error occurs while initializing the dynamic client id and client credentials.
 */
public class ClientCredentialsException extends MAGException {

    public ClientCredentialsException(int errorCode) {
        super(errorCode);
    }

    public ClientCredentialsException(int errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    public ClientCredentialsException(int errorCode, String detailMessage, Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    public ClientCredentialsException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
