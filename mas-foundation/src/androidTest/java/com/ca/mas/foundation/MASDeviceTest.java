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
import com.ca.mas.core.http.ContentType;
import com.squareup.okhttp.mockwebserver.MockResponse;
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

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
        MASCallbackFuture<Void> deRegistrationCallback = new MASCallbackFuture<>();
        deviceInstance.deregister(deRegistrationCallback);
        deRegistrationCallback.get();

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);
        Assert.assertNotNull(callback.get());

        String identifier2 = deviceInstance.getIdentifier();
        Assert.assertFalse(identifier1.equals(identifier2));
    }

    @Test
    public void testAddAttribute() throws ExecutionException, InterruptedException {
        MASDevice deviceInstance = MASDevice.getCurrentDevice();

        MASCallbackFuture<Void> callback = new MASCallbackFuture<>();
        deviceInstance.addAttribute("attr", "valueAttr", callback);
        assertNull(callback.get());
    }

    @Test
    public void testRemoveAttribute() throws ExecutionException, InterruptedException {

        MASDevice deviceInstance = MASDevice.getCurrentDevice();
        MASCallbackFuture<Void> callback = new MASCallbackFuture<>();

        deviceInstance.removeAttribute("attr", callback);
        assertNull(callback.get());
    }

    @Test
    public void testGetAttribute() throws ExecutionException, InterruptedException {
        MASDevice devi = MASDevice.getCurrentDevice();
        MASCallbackFuture<JSONObject> callback = new MASCallbackFuture<>();

        devi.getAttribute("attr", callback);
        assertNotNull(callback.get());

        assertNotNull(callback.get().toString().contains("name"));
        assertNotNull(callback.get().toString().contains("value"));

    }

    @Test
    public void testGetAttributes() throws ExecutionException, InterruptedException {
        MASDevice device = MASDevice.getCurrentDevice();

        MASCallbackFuture<JSONArray> callback = new MASCallbackFuture<>();

        device.getAttributes(callback);
        assertNotNull(callback.get());
    }

    @Test
    public void testRemoveAttributes() throws ExecutionException, InterruptedException {
        MASDevice device = MASDevice.getCurrentDevice();

        MASCallbackFuture<Void> callback = new MASCallbackFuture<>();
        device.removeAllAttributes(callback);

        assertNull(callback.get());
    }

    @Test
    public void getAttrNotFound() throws ExecutionException, InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse deviceMetadata() {
                return new MockResponse().setResponseCode(404)
                        .setHeader("Content-type", ContentType.APPLICATION_JSON)
                        .setHeader("x-ca-err","1016156");
            }
        });

        MASDevice devi = MASDevice.getCurrentDevice();
        MASCallbackFuture<JSONObject> callback = new MASCallbackFuture<>();

        devi.getAttribute("attr", callback);
        assertNotNull(callback.get());
        assertTrue(callback.get() != null);
    }

    @Test
    public void deleteAttrNotFound() throws ExecutionException, InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse deviceMetadata() {
                return new MockResponse().setResponseCode(404)
                        .setHeader("Content-type", ContentType.APPLICATION_JSON)
                        .setHeader("x-ca-err","1016156");
            }
        });

        MASDevice device = MASDevice.getCurrentDevice();

        MASCallbackFuture<Void> callback = new MASCallbackFuture<>();
        device.removeAttribute("NoWayAttr", callback);

        assertNull(callback.get());
    }

    @Test(expected = MASDeviceAttributeOverflowException.class)
    public void testOverflow() throws Throwable {

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse deviceMetadata() {
                 return new MockResponse().setResponseCode(400)
                        .setHeader("Content-type", ContentType.APPLICATION_JSON)
                        .setHeader("x-ca-err","1016155");
            }
        });

        MASDevice device = MASDevice.getCurrentDevice();

        MASCallbackFuture<Void> callback = new MASCallbackFuture<>();
        device.addAttribute("overflow", "overflow", callback);

        try {
            callback.get();
            fail();
        } catch (ExecutionException e) {
            throw ((MASException) e.getCause()).getRootCause();
        }

    }

    public void testNullCallback() {
        MASDevice device = MASDevice.getCurrentDevice();
        device.getAttribute("attr", null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNullKey() throws Throwable {
        MASDevice device = MASDevice.getCurrentDevice();
        MASCallbackFuture<Void> callback = new MASCallbackFuture<>();

        device.addAttribute(null, "value", callback);

        try {
            callback.get();
            fail();
        }catch (ExecutionException e){
            throw ((MASException) e.getCause()).getRootCause();
        }
    }
}
