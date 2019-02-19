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
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.connecta.client.MASConnectOptions;
import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
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
            intentFilter.addAction(MessagingConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED);
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
            masMessage.setContentType(MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE);
            masMessage.setPayload(EXPECT.getBytes());

            MASCallbackFuture<Void> sendCallbackFuture = new MASCallbackFuture<>();
            MASUser.getCurrentUser().sendMessage(masMessage, MASUser.getCurrentUser(), sendCallbackFuture);
            sendCallbackFuture.get();

            countDownLatch.await();
            Assert.assertEquals(EXPECT, messageReceived[0]);

            MASCallbackFuture<Void> stopCallbackFuture = new MASCallbackFuture<>();
            MASUser.getCurrentUser().stopListeningToMyMessages(stopCallbackFuture);
            stopCallbackFuture.get();


        } finally {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        }
    }

    @Test
    public void testSubscribePublishToPublicBroker() throws Exception {
        final String EXPECT = "Test1";
        BroadcastReceiver broadcastReceiver = null;
        try {
            final String[] messageReceived = new String[1];
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(MessagingConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED);
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
            MASConnectOptions connectOptions = setConnectOptions();

            MASCallbackFuture<Void> connectCallbackFuture = new MASCallbackFuture<>();
            MASConnectaManager.getInstance().setConnectOptions(connectOptions);
            MASConnectaManager.getInstance().setClientId(UUID.randomUUID().toString());
            MASConnectaManager.getInstance().connect(connectCallbackFuture);
            connectCallbackFuture.get();

            MASTopic customTopic = new MASTopicBuilder()
                    .setQos(2)
                    .setCustomTopic("test_topic")
                    .enforceTopicStructure(false)
                    .build();

            MASCallbackFuture<Void> subscribeCallbackFuture = new MASCallbackFuture<>();
            MASConnectaManager.getInstance().subscribe(customTopic,subscribeCallbackFuture);
            subscribeCallbackFuture.get();


            MASMessage masMessage = MASMessage.newInstance();
            masMessage.setContentType(MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE);
            masMessage.setPayload(EXPECT.getBytes());

            MASCallbackFuture<Void> sendCallbackFuture = new MASCallbackFuture<>();
            MASConnectaManager.getInstance().publish(customTopic, masMessage, sendCallbackFuture);
            sendCallbackFuture.get();

            countDownLatch.await();
            Assert.assertEquals(EXPECT, messageReceived[0]);

            MASCallbackFuture<Void> stopCallbackFuture = new MASCallbackFuture<>();
            MASConnectaManager.getInstance().disconnect(stopCallbackFuture);
            stopCallbackFuture.get();


        } finally {
            LocalBroadcastManager.getInstance(getContext()).unregisterReceiver(broadcastReceiver);
        }
    }

    public MASConnectOptions setConnectOptions() {

        final Uri.Builder uriBuilder = new Uri.Builder()
                .scheme("tcp")
                .encodedAuthority("mobile-autotest-services.lvn.broadcom.net:1883");
        final Uri uri = uriBuilder.build();
        final String username = "username";
        final String password = "password";

        MASConnectOptions connectOptions = new MASConnectOptions();
        connectOptions.setServerURIs(new String[]{uri.toString()});
        connectOptions.setKeepAliveInterval(10);
        connectOptions.setUserName(username);
        connectOptions.setPassword(password.toCharArray());
        return connectOptions;
    }


}
