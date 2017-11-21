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
import android.util.Log;

import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.auth.otp.OtpUtil;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.policy.MssoAssertion;
import com.ca.mas.core.policy.RequestInfo;
import com.ca.mas.core.policy.exceptions.OtpException;
import com.ca.mas.core.policy.exceptions.RiskAnalysisException;

import java.net.HttpURLConnection;
import java.util.logging.Logger;

/**
 * A policy that checks for risk analysis related error codes.
 * Throws OtpException if found.
 */
class RiskAnalysisAssertion implements MssoAssertion {

    private static String TAG = "RiskAnalysisAssertion";

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) throws MAGException, MAGServerException {

        //TODO check if request contains
        /*//When receive OTP
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
        }*/
        Log.d(TAG, "processRequest");
    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MAGResponse response) throws MAGServerException {

        Log.d(TAG, "processResponse");
        //Check if status code is 409
        int statusCode = response.getResponseCode();
        if (statusCode == HttpURLConnection.HTTP_CONFLICT
                ) {
            //Analyse response here
            Log.d(TAG, "error code is 409 ");
            throw new RiskAnalysisException(ServerClient.findErrorCode(response),
                    response.getResponseCode(),
                    response.getBody().getContentType(),
                    "Risk data is missing"
            );
        }
    }

    @Override
    public void close() {
    }

}
