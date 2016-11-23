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

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.storage.MASSecureStorage;
import com.ca.mas.storage.MASStorage;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;

public class MASSecureStorageForApplication extends MASSecureStorageTests {

    @Override
    protected int getMode() {
        return MASConstants.MAS_APPLICATION;
    }

    @Test
    public void testSaveStringWithDifferentMode() throws Exception {

        final String expectedValue = "VALUE";

        final CountDownLatch saveLatch = new CountDownLatch(1);
        MASSecureStorage mgr = new MASSecureStorage();
        String key = keys.get(1);
        mgr.save(key, expectedValue, super.getMode(), new MASCallback() {

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

}
