/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.foundation;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MASEnrollmentTest {

    @Test
    public void testEnrollment() throws Exception {

        URL url = new URL("<The enrollment url>");

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};

        MAS.start(InstrumentationRegistry.getInstrumentation().getTargetContext(), url, new MASCallback<Void>() {
            @Override
            public void onSuccess(Void r) {
                result[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                result[0] = false;
                latch.countDown();
            }
        });

        latch.await();
        assertTrue(result[0]);
        //Make sure MAS.start has been successfully invoked.
        MASConfiguration.getCurrentConfiguration();
    }

}
