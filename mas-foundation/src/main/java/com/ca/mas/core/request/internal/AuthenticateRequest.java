/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.request.internal;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.foundation.MASProgressListener;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASResponseBody;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class AuthenticateRequest extends MAGRequestProxy implements LocalRequest {

    public AuthenticateRequest() {
        request = new MASRequest.MASRequestBuilder((URI)null).password().build();
    }

    @Override
    public MASResponse send(MssoContext context) {
        context.clearCredentials();
        return new MASResponse() {

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }

            @Override
            public int getResponseCode() {
                return HttpURLConnection.HTTP_OK;
            }

            @Override
            public String getResponseMessage() {
                return null;
            }

            @Override
            public MASResponseBody getBody() {
                return null;
            }
        };
    }

    @Override
    public MASProgressListener getProgressListener() {
        return request.getProgressListener();
    }
}
