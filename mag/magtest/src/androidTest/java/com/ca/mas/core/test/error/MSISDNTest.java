/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.error;

import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.policy.exceptions.MobileNumberInvalidException;
import com.ca.mas.core.policy.exceptions.MobileNumberRequiredException;
import com.ca.mas.core.test.BaseTest;
import com.ca.mas.core.test.DefaultDispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URISyntaxException;

import static junit.framework.Assert.assertTrue;

@Deprecated
@RunWith(AndroidJUnit4.class)
public class MSISDNTest extends BaseTest {

    @Deprecated
    @Test
    public void missingMSISDNHeader() throws InterruptedException, URISyntaxException {

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                return new MockResponse().setResponseCode(449).setBody("{\"error\":\"invalid_request\", \"error_description\":\"Missing MSISDN\"}");
            }
        });

        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();

        processRequest(request);
        assertTrue(error.getCause() instanceof MobileNumberRequiredException);

    }

    @Deprecated
    @Test
    public void InvalidMSISDNHeader() throws InterruptedException, URISyntaxException {

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                return new MockResponse().setResponseCode(448).setBody("{\"error\":\"access_denied\", \"error_description\":\"The given MSISDN cannot be resolved\"}");
            }
        });

        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();

        processRequest(request);
        assertTrue(error.getCause() instanceof MobileNumberInvalidException);

    }
}
