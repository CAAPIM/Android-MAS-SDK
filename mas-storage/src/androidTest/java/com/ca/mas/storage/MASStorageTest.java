/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.storage;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.foundation.MASException;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public abstract class MASStorageTest extends MASLoginTestBase {
    abstract
    @MASStorageSegment
    int getMode();

    abstract MASStorage getMASStorage();

    @Override
    protected void setupDispatcher(GatewayDefaultDispatcher gatewayDefaultDispatcher) {
        gatewayDefaultDispatcher.addDispatcher(new StorageDispatcher());
    }

    @After
    public void after() throws Exception {
        MASStorage mgr = getMASStorage();
        MASCallbackFuture<Set<String>> keySetCallbackFuture = new MASCallbackFuture<>();
        mgr.keySet(getMode(), keySetCallbackFuture);

        for (final String keyItem : keySetCallbackFuture.get()) {
            MASCallbackFuture<Void> deleteCallbackFuture = new MASCallbackFuture<>();
            mgr.delete(keyItem, getMode(), deleteCallbackFuture);
            deleteCallbackFuture.get();
        }
    }

    @Test(expected = NullPointerException.class)
    public void testSaveNull() throws Throwable {
        String key = "key";
        String value = null;
        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        getMASStorage().save(key, value, getMode(), callbackFuture);

        try {
            callbackFuture.get();
        } catch (Exception e) {
            throw ((MASException) e.getCause()).getRootCause();
        }
    }

    @Test(expected = TypeNotPresentException.class)
    public void testSaveUnsupportedData() throws Throwable {
        String key = "key";
        Object value = new Object();
        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        getMASStorage().save(key, value, getMode(), callbackFuture);
        try {
            callbackFuture.get();
        } catch (Exception e) {
            throw ((MASException) e.getCause()).getRootCause();
        }
    }

    @Test
    public void testSaveString() throws Exception {

        String key = "key";
        final String expectedValue = "VALUE";

        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        getMASStorage().save(key, expectedValue, getMode(), callbackFuture);
        callbackFuture.get();

        MASCallbackFuture<String> findByKeyCallbackFuture = new MASCallbackFuture<>();
        getMASStorage().findByKey(key, getMode(), findByKeyCallbackFuture);
        assertEquals(expectedValue, findByKeyCallbackFuture.get());
    }

    @Test
    public void testSaveJSONObject() throws Exception {

        final String expectedKey = "KEY";
        final String expectedValue = "VALUE";

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(expectedKey, expectedValue);

        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        getMASStorage().save(expectedKey, jsonObject, getMode(), callbackFuture);
        callbackFuture.get();

        MASCallbackFuture<JSONObject> findByKeyCallbackFuture = new MASCallbackFuture<>();
        getMASStorage().findByKey(expectedKey, getMode(), findByKeyCallbackFuture);
        assertEquals(jsonObject.toString(), findByKeyCallbackFuture.get().toString());
    }

    @Test
    public void testSaveByteArray() throws Exception {

        final byte[] expectedValue = {0, 1, 2};

        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        getMASStorage().save("key", expectedValue, getMode(), callbackFuture);
        callbackFuture.get();

        MASCallbackFuture<byte[]> findByKeyCallbackFuture = new MASCallbackFuture<>();
        getMASStorage().findByKey("key", getMode(), findByKeyCallbackFuture);
        assertTrue(Arrays.equals(expectedValue, findByKeyCallbackFuture.get()));
    }

    @Test
    public void testGetAllKeys() throws Exception {
        testSaveString();
        MASCallbackFuture<Set<String>> keySetCallbackFuture = new MASCallbackFuture<>();
        getMASStorage().keySet(getMode(), keySetCallbackFuture);
        assertTrue(keySetCallbackFuture.get().contains("key"));
    }

    @Test
    public void returnNullIfDataNotFound() throws Exception {

        MASCallbackFuture<byte[]> findByKeyCallbackFuture = new MASCallbackFuture<>();
        getMASStorage().findByKey("doesNotExist", getMode(), findByKeyCallbackFuture);
        assertNull(findByKeyCallbackFuture.get());
    }

    @Test
    public void ignoreWhenDataNotFound() throws Exception {

        MASCallbackFuture<Void> callbackFuture = new MASCallbackFuture<>();
        getMASStorage().delete("doesNotExist", getMode(), callbackFuture);
        callbackFuture.get();
    }
}
