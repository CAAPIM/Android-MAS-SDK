/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;

import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.auth.otp.model.OtpResponseBody;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;

/**
 * Interface that will receive various notifications and requests for MAG Client.
 */
public interface MASAuthenticationListener {

    /**
     * Notify the host application that a request to authenticate is triggered by the authentication process.
     *
     * @param context   The current Activity context
     * @param requestId The request Id that trigger the authentication process
     * @param providers The available Authentication providers, providers can be used for Social Login or Proximity Login
     */
    void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers);

    /**
     * Notify the host application that a request to authenticate Otp is triggered by the authentication process.
     */
    void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler);

}
