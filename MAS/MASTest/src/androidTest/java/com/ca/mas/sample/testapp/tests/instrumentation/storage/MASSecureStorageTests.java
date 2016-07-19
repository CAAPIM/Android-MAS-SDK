/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.storage;

import android.os.Handler;
import android.os.Looper;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;
import com.ca.mas.storage.MASSecureStorage;
import com.ca.mas.storage.MASStorageSegment;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class MASSecureStorageTests extends MASIntegrationBaseTest {

    private static final String TAG = MASSecureStorageTests.class.getSimpleName();

    ArrayList<String> keys = new ArrayList<String>() {{
        add("test_key1");
        add("test_key2");
        add("test_key3");
    }};

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        MASSecureStorage mgr = new MASSecureStorage();
        final CountDownLatch countDownLatch = new CountDownLatch(keys.size());
        for (final String keyItem : keys) {
            mgr.delete(keyItem, getMode(), new MASCallback() {
                @Override
                public void onSuccess(Object result) {
                    countDownLatch.countDown();
                }

                @Override
                public void onError(Throwable exception) {
                    countDownLatch.countDown();
                }
            });
        }
        await(countDownLatch);
    }

    @Test(expected = NullPointerException.class)
    public void testSaveNull() throws Throwable {

        MASSecureStorage mgr = new MASSecureStorage();
        String key = keys.get(1);
        String value = null;
        final Throwable[] expected = new Throwable[1];
        mgr.save(key, value, getMode(), new MASCallback() {
            @Override
            public void onSuccess(Object value) {
            }

            @Override
            public void onError(Throwable exception) {
                expected[0] = exception;

            }
        });
        throw expected[0];
    }

    @Test(expected = TypeNotPresentException.class)
    public void testSaveUnsupportedData() throws Throwable {
        MASSecureStorage mgr = new MASSecureStorage();
        String key = keys.get(1);
        Object value = new Object();
        final Throwable[] expected = new Throwable[1];
        mgr.save(key, value, getMode(), new MASCallback() {
            @Override
            public void onSuccess(Object value) {
            }

            @Override
            public void onError(Throwable exception) {
                expected[0] = exception;
            }
        });
        throw expected[0];
    }

    @Test
    public void testSaveString() throws Exception {

        final String expectedValue = "VALUE";

        final CountDownLatch saveLatch = new CountDownLatch(1);
        MASSecureStorage mgr = new MASSecureStorage();
        String key = keys.get(1);
        mgr.save(key, expectedValue, getMode(), new MASCallback() {

            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(Object value) {
                saveLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                saveLatch.countDown();
            }
        });
        await(saveLatch);

        final CountDownLatch getLatch = new CountDownLatch(1);
        final boolean[] result = {false};
        mgr.findByKey(keys.get(1), getMode(), new MASCallback<String>() {

            @Override
            public void onSuccess(String value) {
                if (value.equals(expectedValue)) {
                    result[0] = true;
                }
                getLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                result[0] = false;
                getLatch.countDown();
            }
        });

        await(getLatch);
        assertTrue(result[0]);

    }

    @Test
    public void testSaveJSONObject() throws Exception {

        final String expectedKey = "KEY";
        final String expectedValue = "VALUE";

        final CountDownLatch saveLatch = new CountDownLatch(1);
        MASSecureStorage mgr = new MASSecureStorage();
        String key = keys.get(1);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(expectedKey, expectedValue);
        mgr.save(key, jsonObject, getMode(), new MASCallback() {
            @Override
            public void onSuccess(Object value) {
                saveLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                saveLatch.countDown();
            }
        });
        saveLatch.await(TIMEOUT, TimeUnit.SECONDS);

        final CountDownLatch getLatch = new CountDownLatch(1);
        final boolean[] result = {false};
        mgr.findByKey(keys.get(1), getMode(), new MASCallback() {

            @Override
            public void onSuccess(Object value) {
                if (value instanceof JSONObject) {
                    try {
                        if (((JSONObject) value).getString(expectedKey).equals(expectedValue)) {
                            result[0] = true;
                        }
                    } catch (JSONException e) {
                    }
                }
                getLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                result[0] = false;
                getLatch.countDown();
            }
        });

        await(getLatch);
        assertTrue(result[0]);

    }

    @Test
    public void testSaveByteArray() throws Exception {

        final byte[] expectedValue = {0, 1, 2};

        final CountDownLatch saveLatch = new CountDownLatch(1);
        MASSecureStorage mgr = new MASSecureStorage();
        String key = keys.get(1);
        mgr.save(key, expectedValue, getMode(), new MASCallback() {
            @Override
            public void onSuccess(Object value) {
                saveLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                saveLatch.countDown();
            }
        });
        saveLatch.await(TIMEOUT, TimeUnit.SECONDS);

        final CountDownLatch getLatch = new CountDownLatch(1);
        final boolean[] result = {false};
        mgr.findByKey(keys.get(1), getMode(), new MASCallback() {

            @Override
            public void onSuccess(Object value) {
                if (value instanceof byte[]) {
                    if (Arrays.equals((byte[]) value, expectedValue)) {
                        result[0] = true;
                    }
                }
                getLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                result[0] = false;
                getLatch.countDown();
            }
        });

        await(getLatch);
        assertTrue(result[0]);

    }

    @Test
    public void testGetAllKeys() throws Exception {
        testSaveString();
        final boolean[] result = {false};
        final CountDownLatch signal = new CountDownLatch(1);
        MASSecureStorage mgr = new MASSecureStorage();
        mgr.keySet(getMode(), new MASCallback<Set<String>>() {

            @Override
            public void onSuccess(Set<String> value) {
                if (!value.isEmpty() && value.contains(keys.get(1))) {
                    result[0] = true;
                }
                signal.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                result[0] = false;
                signal.countDown();
            }
        });
        await(signal);
    }

    @Test
    public void returnNullIfDataNotFound() throws Exception {

        MASSecureStorage mgr = new MASSecureStorage();

        final CountDownLatch getLatch = new CountDownLatch(1);
        final boolean[] result = {false};
        mgr.findByKey(keys.get(1), getMode(), new MASCallback() {

            @Override
            public void onSuccess(Object value) {
                if (value == null) {
                    result[0] = true;
                }
                getLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                result[0] = false;
                getLatch.countDown();
            }
        });

        await(getLatch);
        assertTrue(result[0]);

    }

    @Test
    public void ignoreWhenDataNotFound() throws Exception {

        MASSecureStorage mgr = new MASSecureStorage();

        final CountDownLatch getLatch = new CountDownLatch(1);
        final boolean[] result = {false};
        mgr.delete(keys.get(1), getMode(), new MASCallback() {

            @Override
            public void onSuccess(Object value) {
                result[0] = true;
                getLatch.countDown();
            }

            @Override
            public void onError(Throwable exception) {
                result[0] = false;
                getLatch.countDown();
            }
        });

        await(getLatch);
        assertTrue(result[0]);

    }


    protected
    @MASStorageSegment
    int getMode() {
        return MASConstants.MAS_USER;
    }

}
