/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;

/**
 * This exception is thrown when an invalid phone number is passed to MAG Server
 */
public class MobileNumberInvalidException extends MAGException {

    public MobileNumberInvalidException() {
        super(MAGErrorCode.MSISDN_IS_INVALID);
    }

    public MobileNumberInvalidException(String detailMessage) {
        super(MAGErrorCode.MSISDN_IS_INVALID, detailMessage);
    }

    public MobileNumberInvalidException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.MSISDN_IS_INVALID, detailMessage, throwable);
    }

    public MobileNumberInvalidException(Throwable throwable) {
        super(MAGErrorCode.MSISDN_IS_INVALID, throwable);
    }
}
