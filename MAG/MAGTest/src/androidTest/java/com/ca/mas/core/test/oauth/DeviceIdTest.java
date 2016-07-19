/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.oauth;

import android.support.test.runner.AndroidJUnit4;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.test.BaseTest;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;

@RunWith(AndroidJUnit4.class)
public class DeviceIdTest extends BaseTest {

    private static final String TAG="DeviceIdTest";

    @Test
    public void testDeviceIdChange() throws InterruptedException, UnsupportedEncodingException {

        try {
            MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts"))
                    .password()
                    .build();
            processRequest(request);
            if(useMockServer()) {
                ssg.takeRequest();//Authorize Request
                ssg.takeRequest();//Client Credentials Request
                RecordedRequest rr = ssg.takeRequest();//Register device request
                String ss2 = rr.getPath();
                Log.i(TAG, "path value: " + ss2);
                String deviceIdHeaderValue = rr.getHeader("device-id");
                Log.i(TAG, "device-id value:" + deviceIdHeaderValue);
                String deviceIdValue = new String(Base64.decode(deviceIdHeaderValue.getBytes("US-ASCII"), Base64.NO_WRAP));
                Log.i(TAG, "the actual value:" + deviceIdValue);
                assertNotNull(deviceIdHeaderValue);
                assertEquals(true, deviceIdValue.length()<=100);
            }

        } catch (URISyntaxException e) {
            Log.w(TAG,"Error while getting device-id value",e);
        }

    }

}
