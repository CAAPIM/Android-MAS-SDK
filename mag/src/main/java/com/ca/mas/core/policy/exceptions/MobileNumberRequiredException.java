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
 * This exception is thrown when phone number is missing.
 */
public class MobileNumberRequiredException extends MAGException {

    public MobileNumberRequiredException() {
        super(MAGErrorCode.MSISDN_IS_MISSING);
    }

    public MobileNumberRequiredException(String detailMessage) {
        super(MAGErrorCode.MSISDN_IS_MISSING, detailMessage);
    }

    public MobileNumberRequiredException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.MSISDN_IS_MISSING, detailMessage, throwable);
    }

    public MobileNumberRequiredException(Throwable throwable) {
        super(MAGErrorCode.MSISDN_IS_MISSING, throwable);
    }
}
