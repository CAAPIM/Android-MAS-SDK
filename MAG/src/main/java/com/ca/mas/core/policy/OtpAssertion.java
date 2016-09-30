/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.auth.otp.OtpUtil;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.error.MAGStateException;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.oauth.OAuthException;
import com.ca.mas.core.policy.exceptions.MobileNumberInvalidException;
import com.ca.mas.core.policy.exceptions.MobileNumberRequiredException;
import com.ca.mas.core.policy.exceptions.OtpException;
import com.ca.mas.core.service.MssoState;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A policy that checks for OTP flow related error codes and status in the response.
 * Throws OtpException if found.
 */
class OtpAssertion implements MssoAssertion {
    private static final String TAG = OtpAssertion.class.getName();

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) throws MAGException, MAGServerException {

        String otpAuthUrl = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.AUTHENTICATE_OTP_PATH);
        String requestPath = getRequestPath(request);
        if (!TextUtils.isEmpty(otpAuthUrl) &&
                !TextUtils.isEmpty(requestPath) &&
                requestPath.endsWith(otpAuthUrl)
                ) {
            List<String> selectedOtpChannels = (request.getRequest().getHeaders().get(OtpConstants.X_OTP_CHANNEL));
            String selectedOtpChannelsStr = OtpUtil.convertListToCommaSeparatedString(selectedOtpChannels);
            mssoContext.setOtpSelectedDeliveryChannels(selectedOtpChannelsStr);
        }

        String otp = mssoContext.getOtp();
        if (otp != null && !"".equals(otp)) {
            request.getRequest().addHeader(OtpConstants.X_OTP, otp);
            mssoContext.setOtp(null);
            /*MAPI-1033 : Add support caching of user selected OTP channels*/
            if (mssoContext.getOtpSelectedDeliveryChannels() != null
                    && !"".equals(mssoContext.getOtpSelectedDeliveryChannels() )) {
                request.getRequest().addHeader(OtpConstants.X_OTP_CHANNEL, mssoContext.getOtpSelectedDeliveryChannels() );
            }
        }

    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MAGResponse response) throws MAGServerException{

        //Check if status code is 400, 401 or 403
        int statusCode = response.getResponseCode();
        if (statusCode == java.net.HttpURLConnection.HTTP_BAD_REQUEST
                || statusCode == java.net.HttpURLConnection.HTTP_UNAUTHORIZED
                || statusCode == java.net.HttpURLConnection.HTTP_FORBIDDEN) {
            OtpResponseHeaders otpResponseHeaders = OtpUtil.getXotpValueFromHeaders(response.getHeaders());
            otpResponseHeaders.setHttpStatusCode(statusCode);

            if (OtpResponseHeaders.X_OTP_VALUE.REQUIRED == otpResponseHeaders.getxOtpValue()
                    || OtpResponseHeaders.X_CA_ERROR.OTP_INVALID == otpResponseHeaders.getErrorCode()){
                int errorCode = 0;
                try {
                    errorCode = ServerClient.findErrorCode(response);
                } catch (IOException e){
                }
                OtpResponseBody body = OtpUtil.parseOtpResponseBody(response.getBody().getContent().toString());
                throw new OtpException(errorCode, statusCode, response.getBody().getContentType(), body.getErrorDescription(),
                        otpResponseHeaders);
            }
        }
    }

    @Override
    public void close() {
    }

    private String getRequestPath(RequestInfo request) {
        if (request != null && request.getRequest() != null && request.getRequest().getURL() != null &&
                request.getRequest().getURL().getPath() != null
                ) {
            return request.getRequest().getURL().getPath();
        }
        return null;
    }
}
