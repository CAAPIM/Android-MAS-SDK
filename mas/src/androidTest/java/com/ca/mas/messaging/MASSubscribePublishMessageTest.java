/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.util.MessagingConsts;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class MASSubscribePublishMessageTest extends MASLoginTestBase {

    @Override
    public void login() throws InterruptedException, ExecutionException {
        //Don't login
    }

    @Before
    public void setUp() throws Exception {
        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config_messaging.json"));
        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("admin", "7layer".toCharArray(), callback);
        Assert.assertNotNull(callback.get());

    }

    @Test
    public void testSubscribePublish() throws Exception {
        final String EXPECT = "Test1";
        BroadcastReceiver broadcastReceiver = null;
        try {
            final String[] messageReceived = new String[1];
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED);
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    MASMessage message = null;
                    message = MASMessage.newInstance(intent);
                    byte[] msg = message.getPayload();
                    messageReceived[0] = new String(msg);
                    countDownLatch.countDown();
                }
            };

            LocalBroadcastManager.getInstance(getContext()).registerReceiver(broadcastReceiver,
                    intentFilter);

            MASCallbackFuture<Void> listenCallbackFuture = new MASCallbackFuture<>();
            MASUser.getCurrentUser().startListeningToMyMessages(listenCallbackFuture);
            listenCallbackFuture.get();

            MASMessage masMessage = MASMessage.newInstance();
            masMessage.setContentType(MessagingConsts.MT_TEXT_PLAIN);
            masMessage.setPayload(EXPECT.getBytes());

            MASCallbackFuture<Void> sendCallbackFuture = new MASCallbackFuture<>();
            MASUser.getCurrentUser().sendMessage(masMessage, MASUser.getCurrentUser(), sendCallbackFuture);
            sendCallbackFuture.get();

            countDownLatch.await();
            Assert.assertEquals(EXPECT, messageReceived[0]);

            MASCallbackFuture<Void> stopCallbackFuture = new MASCallbackFuture<>();
            MASUser.getCurrentUser().stopListeningToMyMessages(stopCallbackFuture);
            sendCallbackFuture.get();


        } finally {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        }
    }

    @After
    public void tearDown() throws Exception {
    }
}
