/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.os.Bundle;

import com.ca.mas.core.ResponseInterceptor;

/**
 * Represents a multi-factor authenticator
 *
 * @param <T>
 */
public abstract class MASMultiFactorAuthenticator<T extends MASMultiFactorHandler> implements ResponseInterceptor {

    /**
     * Determine if the API Response required Multi-Factor Authentication, return a {@link MASMultiFactorHandler} if
     * step up authentication is required, otherwise return null;
     *
     * @param requestId    The request ID which identify the request ID which is pending for Multi-factor authentication
     * @param request      The original request that may trigger the multi-factor authentication.
     * @param requestExtra Previous state information for the request.
     * @param response     The API Response
     * @return A {@link MASMultiFactorHandler} If step up authentication is required, otherwise return null.
     */
    public abstract T getMultiFactorHandler(long requestId, MASRequest request, Bundle requestExtra, MASResponse response);

    /**
     * This method will be triggered after {@link #getMultiFactorHandler(long, MASRequest, Bundle, MASResponse)} return a
     * {@link MASMultiFactorHandler}, step up authentication is required for this API request.
     *
     * @param context         The current Activity Context
     * @param originalRequest The original request that may trigger the multi-factor authentication.
     * @param handler         To handle and provide more information required for step up authentication.
     */
    protected abstract void onMultiFactorAuthenticationRequest(Context context, MASRequest originalRequest, T handler);

    @Override
    public boolean intercept(long requestId, MASRequest originalRequest, Bundle requestExtra, MASResponse response) {
        T handler = getMultiFactorHandler(requestId, originalRequest, requestExtra, response);
        if (handler != null) {
            onMultiFactorAuthenticationRequest(MAS.getCurrentActivity(), originalRequest, handler);
            //return true to keep the request in the queue
            return true;
        }
        return false;
    }
}
