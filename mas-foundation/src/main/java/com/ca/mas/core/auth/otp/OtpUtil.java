/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.otp;

import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class OtpUtil {

    private OtpUtil() {
    }

    public static OtpResponseHeaders getXotpValueFromHeaders(Map<String, List<String>> headers) {
        OtpResponseHeaders otpHeaders = new OtpResponseHeaders();
        if (headers != null) {
            List<String> otpStatusList = headers.get(OtpConstants.X_OTP);
            if (otpStatusList != null && !otpStatusList.isEmpty()) {
                String otpStatus = otpStatusList.get(0);
                OtpResponseHeaders.X_OTP_VALUE otpValue = convertOtpStatusToEnum(otpStatus);
                otpHeaders.setxOtpValue(otpValue);
            }

            List<String> otpChannelList = headers.get(OtpConstants.X_OTP_CHANNEL);
            if (otpChannelList != null && otpChannelList.get(0) != null) {
                String channels = otpChannelList.get(0);
                String[] channelsStringArray = channels.split(",");
                otpHeaders.setChannels(Arrays.asList(channelsStringArray));
            }

            List<String> otpRetryList = headers.get(OtpConstants.X_OTP_RETRY);
            if (otpRetryList != null && !otpRetryList.isEmpty()) {
                String otpRetry = otpRetryList.get(0);
                if (otpRetry != null && !"".equals(otpRetry))
                    otpHeaders.setRetry(Integer.decode(otpRetry));
            }
            List<String> otpRetryIntervalList = headers.get(OtpConstants.X_OTP_RETRY_INTERVAL);
            if (otpRetryIntervalList != null && !otpRetryIntervalList.isEmpty()) {
                String otpRetryInterval = otpRetryIntervalList.get(0);
                if (otpRetryInterval != null && !"".equals(otpRetryInterval))
                    otpHeaders.setRetryInterval(Integer.decode(otpRetryInterval));
            }

            List<String> errorCodeList = headers.get(OtpConstants.X_CA_ERR);
            if (errorCodeList != null && !errorCodeList.isEmpty()) {
                String xCaError = errorCodeList.get(0);
                OtpResponseHeaders.X_CA_ERROR error = convertOtpErrorCodeToEnum(xCaError);
                otpHeaders.setErrorCode(error);
            }
        }
        return otpHeaders;
    }

    /*In the otp flow the gateway returns a header with the name X-OTP
    The value may be generated, required, invalid, expired, suspended
     */
    private static OtpResponseHeaders.X_OTP_VALUE convertOtpStatusToEnum(String otpStatus) {
        switch (otpStatus) {
            //Request to a Otp protected API was made without a header named X-OTP
            case "required":
                return OtpResponseHeaders.X_OTP_VALUE.REQUIRED;

            //Request to generate an Otp was made. This would mean the Otp was successfully generated
            case "generated":
                return OtpResponseHeaders.X_OTP_VALUE.GENERATED;

            //Request to a Otp protected API was made with a header named X-OTP.
            //An otp is passed as a value to this header
            //The otp is incorrect
            case "invalid":
                return OtpResponseHeaders.X_OTP_VALUE.INVALID;

            //Request to a Otp protected API was made with a header named X-OTP.
            //An otp is passed as a value to this header
            //The otp has expired
            case "expired":
                return OtpResponseHeaders.X_OTP_VALUE.EXPIRED;

            //New OTP transaction is requested but the user in barred/suspended state.
            case "suspended":
                return OtpResponseHeaders.X_OTP_VALUE.SUSPENDED;

            default:
                return OtpResponseHeaders.X_OTP_VALUE.UNKNOWN;

        }
    }

    /*In the otp flow the gateway returns a header with the name x-ca-err
     */

    public static OtpResponseHeaders.X_CA_ERROR convertOtpErrorCodeToEnum(String errorCode) {
        switch (errorCode) {
            //Request to a Otp protected API was made without a header named X-OTP
            case "8000140":
                return OtpResponseHeaders.X_CA_ERROR.REQUIRED;

            //Request to a Otp protected API was made with a header named X-OTP.
            //An otp is passed as a value to this header
            //The otp is incorrect
            case "8000142":
                return OtpResponseHeaders.X_CA_ERROR.OTP_INVALID;

            //Request to a Otp protected API was made with a header named X-OTP.
            //An otp is passed as a value to this header
            //The otp has expired
            case "8000143":
                return OtpResponseHeaders.X_CA_ERROR.EXPIRED;


            //Request to a Otp protected API was made with a header named X-OTP.
            //An otp is passed as a value to this header
            //OTP provided is invalid/expired and max retries have exceeded.
            case "8000144":
                return OtpResponseHeaders.X_CA_ERROR.OTP_MAX_RETRY_EXCEEDED;

            //New OTP transaction is requested but the user in barred/suspended state.
            case "8000145":
                return OtpResponseHeaders.X_CA_ERROR.SUSPENDED;
            case "8000400":
                return OtpResponseHeaders.X_CA_ERROR.INVALID_USER_INPUT;
            case "8000500":
                return OtpResponseHeaders.X_CA_ERROR.INTERNAL_SERVER_ERROR;
            default:
                return OtpResponseHeaders.X_CA_ERROR.UNKNOWN;

        }
    }

    public static OtpResponseBody parseOtpResponseBody(String body) {
        OtpResponseBody otpResponseBody = new OtpResponseBody();
        try {
            JSONObject jsonObj = new JSONObject(body);
            otpResponseBody.setError(jsonObj.getString("error"));
            otpResponseBody.setErrorDescription(jsonObj.getString("error_description"));
        } catch (JSONException e) {
            //ignore
        }
        return otpResponseBody;
    }
}


