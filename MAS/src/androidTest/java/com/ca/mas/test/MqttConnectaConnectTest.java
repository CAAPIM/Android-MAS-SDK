/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.test;

import android.app.Application;
import android.test.ApplicationTestCase;

import com.ca.mas.connecta.client.MASConnectaManager;

/**
 * 2016-03-11
 *
 * @author allro12
 */
public class MqttConnectaConnectTest extends ApplicationTestCase<Application> {

    public MqttConnectaConnectTest(Class<Application> applicationClass) {
        super(applicationClass);
    }

    public MqttConnectaConnectTest() {
        super(Application.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MASConnectaManager.getInstance().start(getContext());
    }

    public void testConnectaConnect() {
        /*
        MASConnectaManager.getInstance().connect(new MASResultReceiver<Status>() {
            @Override
            public void onError(MAGError error) {
                assert(false);
            }

            @Override
            public void onSuccess(MAGResponse<Status> response) {
                assertNotNull(response);
            }
        });
        */
    }
}
