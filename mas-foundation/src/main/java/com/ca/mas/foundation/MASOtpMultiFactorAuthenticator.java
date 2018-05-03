/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.auth.otp.OtpUtil;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.service.MssoIntents;

import java.io.Serializable;
import java.util.Map;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * One Time Password MultiFactorAuthenticator
 */
public class MASOtpMultiFactorAuthenticator extends MASMultiFactorAuthenticator<MASOtpAuthenticationHandler> {

    @Override
    public MASOtpAuthenticationHandler getMultiFactorHandler(long requestId, MASRequest request, MASResponse response) {
        //Check if status code is 400, 401 or 403
        int statusCode = response.getResponseCode();
        if (statusCode == java.net.HttpURLConnection.HTTP_BAD_REQUEST
                || statusCode == java.net.HttpURLConnection.HTTP_UNAUTHORIZED
                || statusCode == java.net.HttpURLConnection.HTTP_FORBIDDEN) {

            OtpResponseHeaders otpResponseHeaders = OtpUtil.getXotpValueFromHeaders(response.getHeaders());

            if (OtpResponseHeaders.X_OTP_VALUE.REQUIRED == otpResponseHeaders.getxOtpValue()) {
                return new MASOtpAuthenticationHandler(requestId, otpResponseHeaders.getChannels(), false);
            }

            if (OtpResponseHeaders.X_CA_ERROR.OTP_INVALID == otpResponseHeaders.getErrorCode()) {
                return new MASOtpAuthenticationHandler(requestId, otpResponseHeaders.getChannels(), true);
            }
        }
        return null;
    }

    @Override
    public void onMultiFactorAuthenticationRequest(Context context, MASRequest originalRequest, MASOtpAuthenticationHandler handler) {

        if (MAS.getAuthenticationListener() == null) {
            Class<Activity> otpActivity = getOtpActivity();
            if (otpActivity != null) {
                if (context != null) {
                    Intent intent = new Intent(context, otpActivity);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(MssoIntents.EXTRA_OTP_HANDLER, handler);
                    context.startActivity(intent);
                }
            } else {
                if (DEBUG)
                    Log.w(TAG, MASAuthenticationListener.class.getSimpleName() + " is required for otp authentication.");
            }
        } else {
            //Backward compability
            MAS.getAuthenticationListener().onOtpAuthenticateRequest(MAS.getCurrentActivity(), handler);
        }
    }

    /**
     * Return the MASOtpActivity from MASUI components if MASUI library is included in the classpath.
     *
     * @return A OtpActivity to capture the otp or null if error.
     */
    private Class<Activity> getOtpActivity() {
        try {
            return (Class<Activity>) Class.forName("com.ca.mas.ui.otp.MASOtpActivity");
        } catch (Exception e) {
            return null;
        }
    }
}
