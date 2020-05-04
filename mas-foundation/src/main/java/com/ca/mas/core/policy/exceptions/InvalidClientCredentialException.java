/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy.exceptions;

import android.util.Log;

import com.ca.mas.core.context.MssoContext;

import static com.ca.mas.foundation.MAS.TAG;

public class InvalidClientCredentialException extends RetryRequestException {

    public static final String INVALID_CLIENT_CREDENTIAL_SUFFIX = "201";

    public InvalidClientCredentialException() {
    }

    public InvalidClientCredentialException(String message) {
        super(message);
    }

    public InvalidClientCredentialException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public void recover(MssoContext context) {
        Log.d(TAG,"Escalation InvalidClientCredentialException recover");
        context.clearAccessToken();
        context.clearClientCredentials();
    }
}
