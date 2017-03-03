/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.policy.exceptions.LocationInvalidException;
import com.ca.mas.core.policy.exceptions.LocationRequiredException;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.json.JSONObject;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASGeoFencingTest extends MASLoginTestBase {

    @Test
    public void missingGeoLocationHeader() throws InterruptedException, URISyntaxException {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                return new MockResponse().setResponseCode(449).setBody("{\"error\":\"invalid_request\", \"error_description\":\"Missing location data\"}");
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof LocationRequiredException);
            assertTrue(((MASException)e.getCause()).getRootCause() instanceof LocationRequiredException);
        }
    }

    @Test
    public void InvalidGeoLocationHeader() throws InterruptedException, URISyntaxException {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse secureServiceResponse() {
                return new MockResponse().setResponseCode(448).setBody("{\"error\":\"access_denied\", \"error_description\":\"The given location is not supported\"}");
            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        try {
            assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof LocationInvalidException);
            assertTrue(((MASException)e.getCause()).getRootCause() instanceof LocationInvalidException);
        }
    }
}
