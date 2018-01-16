/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.auth.otp.OtpUtil;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.policy.exceptions.OtpException;
import com.ca.mas.foundation.MASResponse;

/**
 * A policy that checks for OTP flow related error codes and status in the response.
 * Throws OtpException if found.
 */
class OtpAssertion implements MssoAssertion {

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        //do Nothing
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) throws MAGException, MAGServerException {

        //When receive OTP
        Bundle extra = request.getExtra();
        if (extra != null) {
            String otp = extra.getString(OtpConstants.X_OTP);
            if (otp != null ) {
                request.getRequest().addHeader(OtpConstants.X_OTP, otp);
                extra.remove(OtpConstants.X_OTP);
            }
            String selectedChannels = extra.getString(OtpConstants.X_OTP_CHANNEL);
            if (selectedChannels != null) {
                request.getRequest().addHeader(OtpConstants.X_OTP_CHANNEL, selectedChannels);
            }
        }
    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MASResponse response) throws MAGServerException {

        //Check if status code is 400, 401 or 403
        int statusCode = response.getResponseCode();
        if (statusCode == java.net.HttpURLConnection.HTTP_BAD_REQUEST
                || statusCode == java.net.HttpURLConnection.HTTP_UNAUTHORIZED
                || statusCode == java.net.HttpURLConnection.HTTP_FORBIDDEN) {
            OtpResponseHeaders otpResponseHeaders = OtpUtil.getXotpValueFromHeaders(response.getHeaders());
            otpResponseHeaders.setHttpStatusCode(statusCode);

            if (OtpResponseHeaders.X_OTP_VALUE.REQUIRED == otpResponseHeaders.getxOtpValue()
                    || OtpResponseHeaders.X_CA_ERROR.OTP_INVALID == otpResponseHeaders.getErrorCode()) {
                int errorCode = ServerClient.findErrorCode(response);
                OtpResponseBody body = OtpUtil.parseOtpResponseBody(response.getBody().getContent().toString());
                throw new OtpException(errorCode, statusCode, response.getBody().getContentType(), body.getErrorDescription(),
                        otpResponseHeaders);
            }
        }
    }

    @Override
    public void close() {
        //do Nothing
    }

}
