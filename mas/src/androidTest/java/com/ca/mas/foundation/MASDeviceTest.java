/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.util.Base64;
import android.util.Log;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.http.MAGRequest;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class MASDeviceTest extends MASLoginTestBase {

    @Test
    public void testDeviceIdChange() throws URISyntaxException, ExecutionException, InterruptedException, UnsupportedEncodingException {

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_REGISTER);

        String deviceIdHeaderValue = rr.getHeader("device-id");
        assertNotNull(deviceIdHeaderValue);
        String deviceIdValue = new String(Base64.decode(deviceIdHeaderValue.getBytes("US-ASCII"), Base64.NO_WRAP));
        assertEquals(true, deviceIdValue.length() <= 100);
    }

    @Test
    public void testDeviceRegistered() {
        MASDevice masDevice = MASDevice.getCurrentDevice();
        assertTrue(masDevice.isRegistered());
    }


}
