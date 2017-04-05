/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.error;

import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.policy.exceptions.LocationInvalidException;
import com.ca.mas.core.policy.exceptions.LocationRequiredException;
import com.ca.mas.core.test.BaseTest;
import com.ca.mas.core.test.DefaultDispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URISyntaxException;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;

@Deprecated
@RunWith(AndroidJUnit4.class)
public class GeoFencingTest extends BaseTest {

    @Deprecated
    @Test
    public void missingGeoLocationHeader() throws InterruptedException, URISyntaxException {

       assumeMockServer();

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                return new MockResponse().setResponseCode(449).setBody("{\"error\":\"invalid_request\", \"error_description\":\"Missing location data\"}");
            }
        });

        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();
        processRequest(request);
        assertTrue(error.getCause() instanceof LocationRequiredException);

    }

    @Deprecated
    @Test
    public void InvalidGeoLocationHeader() throws InterruptedException, URISyntaxException {

        assumeMockServer();

        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                return new MockResponse().setResponseCode(448).setBody("{\"error\":\"access_denied\", \"error_description\":\"The given location is not supported\"}");
            }
        });

        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).build();
        processRequest(request);
        assertTrue(error.getCause() instanceof LocationInvalidException);

    }
}
