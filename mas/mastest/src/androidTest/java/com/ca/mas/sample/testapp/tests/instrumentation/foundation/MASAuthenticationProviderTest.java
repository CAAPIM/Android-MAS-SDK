/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.foundation;


import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class MASAuthenticationProviderTest extends MASIntegrationBaseTest {

    @Test
    public void testGetEmptyAuthenticationProvider() throws Exception {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] assertResult = {false};

        MASAuthenticationProviders.getAuthenticationProviders(new MASCallback<MASAuthenticationProviders>() {
            @Override
            public void onSuccess(MASAuthenticationProviders result) {
                if (result == null) {
                    assertResult[0] = true;
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(assertResult[0]);
    }

    @Test
    public void testGetAuthenticationProvider() throws Exception {
        final CountDownLatch[] latch = {new CountDownLatch(1)};
        final boolean[] assertResult = {false};

        MASUser.getCurrentUser().logout(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                assertResult[0] = true;
                latch[0].countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch[0].countDown();
            }
        });

        await(latch[0]);
        assertTrue(assertResult[0]);

        assertResult[0] = false;
        latch[0] = new CountDownLatch(1);
        MASAuthenticationProviders.getAuthenticationProviders(new MASCallback<MASAuthenticationProviders>() {
            @Override
            public void onSuccess(MASAuthenticationProviders result) {
                if (result != null) {
                    assertResult[0] = true;
                }
                latch[0].countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch[0].countDown();
            }
        });
        await(latch[0]);
        assertTrue(assertResult[0]);

        //Restore the state
        assertResult[0] = false;
        latch[0] = new CountDownLatch(1);
        MASUser.login(getUsername(), getPassword(), new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                assertResult[0] = true;
                latch[0].countDown();
            }

            @Override
            public void onError(Throwable e) {
                assertResult[0] = false;
                latch[0].countDown();
            }
        });
        await(latch[0]);
        assertTrue(assertResult[0]);
    }
}
