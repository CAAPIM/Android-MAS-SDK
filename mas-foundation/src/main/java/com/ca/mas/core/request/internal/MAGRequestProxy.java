/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.request.internal;

import com.ca.mas.foundation.MASGrantProvider;
import com.ca.mas.foundation.MASConnectionListener;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponseBody;

import java.net.URL;
import java.util.List;
import java.util.Map;

public abstract class MAGRequestProxy implements MASRequest {

    protected MASRequest request;

    @Override
    public URL getURL() {
        return request.getURL();
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return request.getHeaders();
    }

    @Override
    public MASGrantProvider getGrantProvider() {
        return request.getGrantProvider();
    }

    @Override
    public MASRequestBody getBody() {
        return request.getBody();
    }

    @Override
    public MASResponseBody getResponseBody() {
        return request.getResponseBody();
    }

    @Override
    public MASConnectionListener getConnectionListener() {
        return request.getConnectionListener();
    }

    @Override
    public String getScope() {
        return request.getScope();
    }

    @Override
    public boolean isPublic() {
        return request.isPublic();
    }

    @Override
    public boolean notifyOnCancel() {
        return false;
    }
}
