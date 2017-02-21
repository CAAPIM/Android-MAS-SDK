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
import com.ca.mas.foundation.MASConstants;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.URL;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MASStateTest {

    @Ignore
    @Test
    public void testState() throws Exception {
        assertEquals(MASConstants.MAS_STATE_NOT_INITIALIZED, MAS.getState(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        MAS.start(InstrumentationRegistry.getInstrumentation().getTargetContext());
        assertEquals(MASConstants.MAS_STATE_STARTED, MAS.getState(InstrumentationRegistry.getInstrumentation().getTargetContext()));
        MAS.stop();
        assertEquals(MASConstants.MAS_STATE_STOPPED, MAS.getState(InstrumentationRegistry.getInstrumentation().getTargetContext()));
    }

    @Ignore
    @Test
    public void testStateNotConfigured() throws Exception {
        // Step to run the test.
        // 1. Remove msso_config.json
        // 2. adb shell
        // 3. run-as com.ca.mas.sample.testapp
        // 4. rm files/connected_gateway.json
        assertEquals(MASConstants.MAS_STATE_NOT_CONFIGURED, MAS.getState(InstrumentationRegistry.getInstrumentation().getTargetContext()));
    }


}
