/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.error.MAGStateException;

/**
 * Exception thrown if the user must be prompted for their username and password.
 */
public class CredentialRequiredException extends MAGStateException {
    public CredentialRequiredException() {
    }

    public CredentialRequiredException(String detailMessage) {
        super(detailMessage);
    }

    public CredentialRequiredException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public CredentialRequiredException(Throwable throwable) {
        super(throwable);
    }
}
