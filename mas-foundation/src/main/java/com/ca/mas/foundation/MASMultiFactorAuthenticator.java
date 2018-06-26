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
import com.ca.mas.core.service.MssoIntents;

import java.net.HttpURLConnection;
import java.util.Map;

/**
 * Represents a multi-factor authenticator
 *
 * @param <T>
 */
public abstract class MASMultiFactorAuthenticator<T extends MASMultiFactorHandler> implements ResponseInterceptor {

    /**
     * Determines whether the custom MFA object can or will handle flow based on the response of the original request.
     * + The method will only be triggered when the original request failed, and the original request is not one of MAG/OTK's system endpoints.
     *
     * @param requestId The request ID which identify the request ID which is pending for Multi-factor authentication
     * @param request   The original request that may trigger the multi-factor authentication.
     * @param response  The API Response
     * @return If the custom MFA class can or will handle the MFA flow based on the error codes, or response of the request,
     * the method should return {@link MASMultiFactorHandler} class.
     * If null is returned from the method, the original request will deliver the result as it was..
     */
    public abstract T getMultiFactorHandler(long requestId, MASRequest request, MASResponse<?> response);

    /**
     * The method will be triggered when MASFoundation detects that the custom MFA class is responsible to handle MFA flow.
     * {@link MASMultiFactorHandler}, step up authentication is required for this API request.
     *
     * @param context         The current Activity Context
     * @param originalRequest The original request that may trigger the multi-factor authentication.
     * @param handler         To handle and provide more information required for step up authentication.
     */
    protected abstract void onMultiFactorAuthenticationRequest(Context context, MASRequest originalRequest, MASResponse<?> response, T handler);


    @Override
    public boolean intercept(long requestId, MASRequest originalRequest, Bundle requestExtra, MASResponse<?> response) {
        //Multi factor authenticator only cares about failed response
        if (response.getResponseCode() < HttpURLConnection.HTTP_OK || response.getResponseCode() >= HttpURLConnection.HTTP_MULT_CHOICE) {
            T handler = getMultiFactorHandler(requestId, originalRequest, response);
            if (handler != null) {
                //Persist the previous multifactor additional headers
                handler.setPreviousAdditionalHeaders((Map<String, String>) requestExtra.get(MssoIntents.EXTRA_ADDITIONAL_HEADERS));
                onMultiFactorAuthenticationRequest(MAS.getCurrentActivity(), originalRequest, response, handler);
                //return true to keep the request in the queue
                return true;
            }
            return false;
        } else {
            //remove the request from the queue
            return false;
        }
    }
}
