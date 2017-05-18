/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.request.internal;

import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGRequestBody;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.oauth.GrantProvider;

import java.net.URL;
import java.util.List;
import java.util.Map;

public abstract class MAGRequestProxy implements MAGRequest {

    protected MAGRequest request;

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
    public GrantProvider getGrantProvider() {
        return request.getGrantProvider();
    }

    @Override
    public MAGRequestBody getBody() {
        return request.getBody();
    }

    @Override
    public MAGResponseBody getResponseBody() {
        return request.getResponseBody();
    }

    @Override
    public MAGConnectionListener getConnectionListener() {
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

}
