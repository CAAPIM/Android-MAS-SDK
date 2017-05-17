package com.ca.mas.messaging;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;

public class MASMessengerImpl implements MASMessenger {

    @Override
    public void sendMessage(MASTopic topic, MASMessage message, MASCallback<Void> callback) {
        MASConnectaManager.getInstance().publish(topic, message, callback);
    }

    @Override
    public void sendMessage(MASMessage message, MASUser user, MASCallback<Void> callback) {
        String userId = user.getId();
        sendMessage(message, user, userId, callback);}

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
