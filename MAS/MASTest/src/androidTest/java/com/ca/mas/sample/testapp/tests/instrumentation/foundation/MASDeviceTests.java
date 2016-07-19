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

import com.ca.mas.foundation.MASDevice;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MASDeviceTests extends MASIntegrationBaseTest {

    @Test
    public void testDeviceRegistered() {
        MASDevice masDevice = MASDevice.getCurrentDevice();
        assertTrue(masDevice.isRegistered());
    }

}
