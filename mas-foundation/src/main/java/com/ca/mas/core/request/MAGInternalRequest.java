/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.request;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.request.internal.LocalRequest;
import com.ca.mas.core.request.internal.MAGRequestProxy;
import com.ca.mas.foundation.MASProgressListner;
import com.ca.mas.foundation.MASRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A request message from a MAG SDK to MAG Server.
 */
public class MAGInternalRequest extends MAGRequestProxy {

    private Map<String, List<String>> magHeaders = new HashMap<>();
    private MssoContext context;

    public MAGInternalRequest(MssoContext context, MASRequest request) {
        this.context = context;
        this.request = request;
    }

    public void addHeader(String name, String value) {
        List<String> values = magHeaders.get(name);
        if (values == null) {
            values = new ArrayList<>();
            magHeaders.put(name, values);
        }
        values.add(value);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> result = new HashMap<>(super.getHeaders());
        for (String key : magHeaders.keySet()) {
            List<String> value = magHeaders.get(key);
            result.put(key, value);
        }
        return result;
    }

    @Override
    public MASProgressListner getProgressListener() {
        return request.getProgressListener();
    }

    @Override
    public String getScope() {
        String scope = super.getScope();
        if (scope == null) {
            return context.getConfigurationProvider().getClientScope();
        } else {
            return scope;
        }
    }

    @Override
    public String getDownloadFilePath() {
        return request.getDownloadFilePath();
    }

    public boolean isLocalRequest() {
        return request instanceof LocalRequest;
    }

    public MASRequest getRequest(){
        return request;
    }
}
