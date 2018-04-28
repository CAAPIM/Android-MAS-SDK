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
import com.ca.mas.foundation.MASResponse;

/**
 * This exception is thrown when the target application API return x-ca-err code which belongs to otp flow
 */
public class OtpException extends MAGServerException {
    private OtpResponseHeaders otpResponseHeaders;

    public OtpException(MASResponse response, int errorCode, int status, String contentType, String detailMessage, Throwable throwable,
                        OtpResponseHeaders otpResponseHeaders) {
        super(response, errorCode, status, contentType, detailMessage, throwable);
        this.otpResponseHeaders = otpResponseHeaders;
    }
    public OtpException(MASResponse response, int errorCode, int status, String contentType, String detailMessage,
                        OtpResponseHeaders otpResponseHeaders) {
        super(response, errorCode, status, contentType, detailMessage);
        this.otpResponseHeaders = otpResponseHeaders;
    }

    public OtpResponseHeaders getOtpResponseHeaders() {
        return otpResponseHeaders;
    }

}
