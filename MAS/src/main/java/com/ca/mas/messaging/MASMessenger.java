/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.messaging;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.topic.MASTopic;

/**
 * This interface enables messaging feature to the authenticated user
 */
public interface MASMessenger {

    /**
     * This method subscribes the current user to a specific topic.
     *
     * @param topic    The topic to subscribe to.
     * @param callback Callback with either the Success or the Error message
     */

    void startListeningToTopic(MASTopic topic, MASCallback<Void> callback);

    /**
     * This method unsubscribes the current user from a specific topic.
     *
     * @param topic    The topic to unsubscribe from.
     * @param callback Callback with either the Success or the Error message.
     */

    void stopListeningToTopic(MASTopic topic, MASCallback<Void> callback);

    // --------------- USER Messaging -----------------------------------------

    /**
     * This method sends a message from the current user to a topic.
     *
     * @param topic    The Topic to subscribe to.
     * @param message  The Message to be sent. Only MASMessage objects are supported.
     * @param callback The Callback with either the Success or Error message.
     */

    void sendMessage(MASTopic topic, MASMessage message, MASCallback<Void> callback);

    /**
     * This method sends a message from the current user to an existing user.
     *
     * @param message  The Message to be sent.
     * @param user     The User to send the message to.
     * @param callback The Callback with either the Success or Error message.
     */
    void sendMessage(MASMessage message, MASUser user, MASCallback<Void> callback);

    /**
     * This method sends a message from the current user to an existing user on a specified topic.
     *
     * @param message  The Message to be sent.
     * @param user     The User to send the message to.
     * @param callback The Callback with either the Success or the Error message.
     */
    void sendMessage(MASMessage message, MASUser user, String topic, MASCallback<Void> callback);

    /**
     * This method enables incoming messages to the currently authenticated user.
     *
     * @param callback The Callback with either the Success or Error message.
     */
    void startListeningToMyMessages(MASCallback<Void> callback);

    /**
     * This method disables incoming messages to the currently authenticated user.
     *
     * @param callback The Callback with either the Success or Error message.
     */
    void stopListeningToMyMessages(MASCallback<Void> callback);

}
