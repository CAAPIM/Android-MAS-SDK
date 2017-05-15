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
import android.telephony.TelephonyManager;
import android.util.Log;

import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.policy.exceptions.MobileNumberInvalidException;
import com.ca.mas.core.policy.exceptions.MobileNumberRequiredException;
import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * A policy that adds telephone information to outbound requests.
 */
class TelephoneAssertion implements MssoAssertion {

    private TelephonyManager telephonyManager;

    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        ConfigurationProvider conf = mssoContext.getConfigurationProvider();
        if (conf == null)
            throw new NullPointerException("mssoContext.configurationProvider");
        Boolean enabled = conf.getProperty(ConfigurationProvider.PROP_MSISDN_ENABLED);
        if (enabled == null || !enabled) {
            close();
            return;
        }

        try {
            initTelephonyManager(sysContext);
        } catch (Exception e) {
            if (DEBUG) Log.d(TAG, "Unable to access telephone manager: " + e.getMessage(), e);
        }
    }

    private void initTelephonyManager(Context sysContext) {
        telephonyManager = (TelephonyManager) sysContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MAGResponse response) throws MAGException {
        int statusCode = response.getResponseCode();
        if (statusCode >= 400 && statusCode < 500) {
            String responseContent = new String(response.getBody().getRawContent());
            if (responseContent.toLowerCase().contains("msisdn")) {
                if (statusCode == 449) {
                    throw new MobileNumberRequiredException("MSISDN is required by the application to function properly. Enable MSISDN permission.");
                } else if (statusCode == 448) {
                    throw new MobileNumberInvalidException("MSISDN is not authorized to access protected resource.");
                }
            }
        }
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) {
        if (telephonyManager != null) {
            try {
                request.getRequest().addHeader("MSISDN", telephonyManager.getLine1Number());
            } catch (SecurityException e) {
                if (DEBUG) Log.d(TAG, "No permission to access phone state: " + e.getMessage());
            }
        }
    }

    @Override
    public void close() {
        if (telephonyManager != null) {
            telephonyManager = null;
        }
    }
}
