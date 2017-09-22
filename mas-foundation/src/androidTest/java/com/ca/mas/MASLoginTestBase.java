/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import com.ca.mas.foundation.MASDevice;
import com.ca.mas.foundation.MASUser;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;

import java.util.concurrent.ExecutionException;

public abstract class MASLoginTestBase extends MASStartTestBase {

    @Before
    public void login() throws InterruptedException, ExecutionException {
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);
        Assert.assertNotNull(callback.get());
    }

    @After
    public void deregister() throws InterruptedException, ExecutionException {
        if (isSkipped) return;

        MASCallbackFuture<Void> logoutCallback = new MASCallbackFuture<Void>();
        MASUser currentUser = MASUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.logout(logoutCallback);
            Assert.assertNull(logoutCallback.get());
        }

        MASDevice currentDevice = MASDevice.getCurrentDevice();
        if (currentDevice.isRegistered()) {
            MASCallbackFuture<Void> deregisterCallback = new MASCallbackFuture<Void>();
            currentDevice.deregister(deregisterCallback);
            Assert.assertNull(deregisterCallback.get());
        }
    }

}
