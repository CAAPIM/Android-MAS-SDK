/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.policy;

import android.content.Context;
import androidx.annotation.NonNull;

import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.error.MAGException;
import com.ca.mas.core.policy.exceptions.CertificateExpiredException;
import com.ca.mas.foundation.MASResponse;

/**
 * An Assertion that handle common error return from server and determine if
 * the SDK can recover the error and retry the request
 */
class ResponseRecoveryAssertion implements MssoAssertion {


    @Override
    public void init(@NonNull MssoContext mssoContext, @NonNull Context sysContext) {
        //do nothing
    }

    @Override
    public void processRequest(MssoContext mssoContext, RequestInfo request) {
        //do nothing
    }

    @Override
    public void processResponse(MssoContext mssoContext, RequestInfo request, MASResponse response) throws MAGException {
        int errorCode = ServerClient.findErrorCode(response);
        if (errorCode == -1) {
            return;
        }
        String s = Integer.toString(errorCode);

        if (s.endsWith(CertificateExpiredException.CERTIFICATE_EXPIRED_SUFFIX)) {
            throw new CertificateExpiredException();
        }
    }

    @Override
    public void close() {
        //No resources to close
    }

}
