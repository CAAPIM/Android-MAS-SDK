/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.policy;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.foundation.MASResponse;

import java.util.Map;

/**
 * A policy that inject addtional headers to the request.
 */
class CustomHeaderAssertion implements MssoAssertion {

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        //do Nothing
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) {

        //When receive OTP
        Bundle extra = request.getExtra();
        if (extra != null) {
            Map<String, String> additionalHeaders = (Map<String, String>) extra.getSerializable(MssoIntents.EXTRA_ADDITIONAL_HEADERS);
            if (additionalHeaders != null) {
                for (Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
                    request.getRequest().addHeader(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MASResponse response) throws MAGServerException {
        //do Nothing
    }

    @Override
    public void close() {
        //do Nothing
    }

}
