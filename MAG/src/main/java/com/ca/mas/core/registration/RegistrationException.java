/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.registration;

import com.ca.mas.core.error.MAGException;

/**
 * This exception is thrown when an error occurs while processing the registration process.
 */
public class RegistrationException extends MAGException {

    public RegistrationException(int errorCode) {
        super(errorCode);
    }

    public RegistrationException(int errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    public RegistrationException(int errorCode, String detailMessage, Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    public RegistrationException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
