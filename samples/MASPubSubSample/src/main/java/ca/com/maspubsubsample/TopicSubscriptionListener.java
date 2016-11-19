package ca.com.maspubsubsample;

import com.ca.mas.messaging.topic.MASTopic;

/**
 * Created by kalsa12 on 2016-11-17.
 */

public interface TopicSubscriptionListener {
    void onSubscribeToTopic(String topicName, MASTopic topic);
    void onUnsubscribeToTopic(String topicName, MASTopic topic);
    boolean isSubscribedToTopic(String topicName);
}
