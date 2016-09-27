/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.foundation;


import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import org.json.JSONObject;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;

public class MASTest extends MASIntegrationBaseTest {

    @Test
    public void testInvoke() throws Exception {

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts"))
                .build();
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                if (HttpURLConnection.HTTP_OK == response.getResponseCode()) {
                    JSONObject j = response.getBody().getContent();
                    result[0] = true;
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);

        assertTrue(result[0]);
    }

    @Test
    public void testAccessUserInfo() throws Exception {

        String userinfoEndPoint = MASConfiguration.getCurrentConfiguration().getEndpointPath("mas.url.user_info");
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(userinfoEndPoint))
                .build();
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                if (HttpURLConnection.HTTP_OK == response.getResponseCode()) {
                    JSONObject j = response.getBody().getContent();
                    result[0] = true;
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);

        assertTrue(result[0]);
    }



    @Test
    public void testInvokeWithPostJSON() throws Exception {
        JSONObject requestData = new JSONObject();
        requestData.put("jsonName", "jsonValue");
        requestData.put("jsonName2", 1234);

        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts"))
                .post(MASRequestBody.jsonBody(requestData))
                .build();
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                if (HttpURLConnection.HTTP_OK == response.getResponseCode()) {
                    JSONObject j = response.getBody().getContent();
                    result[0] = true;
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);

        assertTrue(result[0]);

    }

    @Test
    public void testGatewayIsReachable() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] assertResult = {false};

        MAS.gatewayIsReachable(new MASCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean result) {
                assertResult[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(assertResult[0]);
    }
}
