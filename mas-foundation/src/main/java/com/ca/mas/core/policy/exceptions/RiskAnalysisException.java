/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.error.MAGServerException;

/**
 * This exception is thrown when the target application API return x-ca-err code which belongs to otp flow
 */
public class RiskAnalysisException extends MAGServerException {

    public RiskAnalysisException(int errorCode, int status, String contentType, String detailMessage, Throwable throwable) {
        super(errorCode, status, contentType, detailMessage, throwable);
    }
    public RiskAnalysisException(int errorCode, int status, String contentType, String detailMessage) {
        super(errorCode, status, contentType, detailMessage);
    }


}
