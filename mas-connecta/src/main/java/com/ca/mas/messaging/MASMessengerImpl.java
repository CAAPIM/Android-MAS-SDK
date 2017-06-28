/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.messaging;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class MASMessengerImpl implements MASMessenger {

    @Override
    public void sendMessage(MASTopic topic, MASMessage message, MASCallback<Void> callback) {
        MASConnectaManager.getInstance().publish(topic, message, callback);
    }

    @Override
    public void sendMessage(MASMessage message, MASUser user, MASCallback<Void> callback) {
        String userId = user.getId();
        sendMessage(message, user, userId, callback);
    }

    @Override
    public void sendMessage(MASMessage message, MASUser user, String topic, MASCallback<Void> callback) {
        String userId = user.getId();
        MASTopic masTopic = new MASTopicBuilder()
                .setUserId(userId)
                .setCustomTopic(topic)
                .build();
        MASConnectaManager.getInstance().publish(masTopic, message, callback);
    }

    @Override
    public void sendMessage(MASMessage message, MASGroup group, final MASCallback<Void> callback) {
        sendMessage(message, group, null, callback);
    }

    @Override
    public void sendMessage(MASMessage message, MASGroup group, String topic, final MASCallback<Void> callback) {
        if (group == null) {
            throw new IllegalArgumentException("Group cannot be null");
        }
        final List<MASMember> members = group.getMembers();
        if (members.isEmpty()) {
            Callback.onError(callback, new MASException("Group has no members", null));
            return;
        }
        String userId = null;
        final int size = members.size();
        MASCallback<Void> temp = null;
        final AtomicInteger count = new AtomicInteger(1);
        final AtomicBoolean bool = new AtomicBoolean(false);
        for (final MASMember member : members) {
            if (member != null) {
                userId = member.getValue();
                temp = new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        if (!bool.getAndSet(true)) {
                            Callback.onSuccess(callback, null);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (!bool.get() && (count.getAndIncrement() == size)) {
                            Callback.onError(callback, e);
                        }
                    }
                };
                MASTopic masTopic = new MASTopicBuilder().setUserId(userId).setCustomTopic(topic != null ? topic : userId).build();
                MASConnectaManager.getInstance().publish(masTopic, message, temp);
            }
        }
    }

    @Override
    public void startListeningToMyMessages(MASCallback<Void> callback) {
        MASTopic masTopic = new MASTopicBuilder()
                .setUserId(MASUser.getCurrentUser().getId())
                .setCustomTopic("#")
                .build();

        MASConnectaManager.getInstance().subscribe(masTopic, callback);
    }

    @Override
    public void stopListeningToMyMessages(MASCallback<Void> callback) {
        MASTopic masTopic = new MASTopicBuilder()
                .setUserId(MASUser.getCurrentUser().getId())
                .setCustomTopic("#")
                .build();
        MASConnectaManager.getInstance().unsubscribe(masTopic, callback);
    }
}
