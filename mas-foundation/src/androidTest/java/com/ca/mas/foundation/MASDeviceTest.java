/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.util.Base64;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

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

    @Test
    public void testDeviceIdentifierAfterReset() throws InterruptedException, ExecutionException {
        MASDevice deviceInstance = MASDevice.getCurrentDevice();
        String identifier1 = deviceInstance.getIdentifier();
        deviceInstance.resetLocally();

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);
        Assert.assertNotNull(callback.get());

        String identifier2 = deviceInstance.getIdentifier();
        Assert.assertFalse(identifier1.equals(identifier2));
    }

    @Test
    public void testDeviceIdentifierAfterDeregister() throws InterruptedException, ExecutionException {
        MASDevice deviceInstance = MASDevice.getCurrentDevice();
        String identifier1 = deviceInstance.getIdentifier();
        deviceInstance.deregister(null);

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);
        Assert.assertNotNull(callback.get());

        String identifier2 = deviceInstance.getIdentifier();
        Assert.assertFalse(identifier1.equals(identifier2));
    }

    @Test
    public void testAddAttribute() {
        MASDevice deviceInstance = MASDevice.getCurrentDevice();
        deviceInstance.addAttribute("attr", "valueAttr", new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                assertTrue(true);
            }

            @Override
            public void onError(Throwable e) {
                fail();
            }
        });
    }

    @Test
    public void testRemoveAttribute() {
        MASDevice device = MASDevice.getCurrentDevice();

        device.removeAttribute("attr", new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                assertTrue(true);
            }

            @Override
            public void onError(Throwable e) {
                fail();
            }
        });
    }

    @Test
    public void testGetAttribute() {
        MASDevice devi = MASDevice.getCurrentDevice();

        devi.getAttribute("attr", new MASCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                assertTrue(true);
            }

            @Override
            public void onError(Throwable e) {
                fail();
            }
        });
    }

    @Test
    public void testGetAttributes() {
        MASDevice device = MASDevice.getCurrentDevice();

        device.getAttributes(new MASCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                if(result != null && result.length() >= 0) {
                    assertTrue(true);
                }
            }

            @Override
            public void onError(Throwable e) {
                fail();
            }
        });
    }

    @Test
    public void testRemoveAttributes() {
        MASDevice device = MASDevice.getCurrentDevice();

        device.removeAllAttributes(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                assertTrue(true);
            }

            @Override
            public void onError(Throwable e) {
                fail();
            }
        });
    }


    @Test(expected = IllegalArgumentException.class)
    public void testNullCallback(){
        MASDevice device = MASDevice.getCurrentDevice();
        device.addAttribute("attr", "value", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKey(){
        MASDevice device = MASDevice.getCurrentDevice();
        device.addAttribute(null, "value", null);
    }
}
