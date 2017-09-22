/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.messaging;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.topic.MASTopic;

/**
 * <b>MASMessenger</b> is an interface which enables messaging feature for the authenticated user.
 */
public interface MASMessenger {

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
     * This method sends a message from the current user to an existing group.
     *
     * @param message  The Message to be sent.
     * @param group     The Group to send the message to.
     * @param callback The Callback with either the Success or Error message.
     */
    void sendMessage(MASMessage message, MASGroup group, MASCallback<Void> callback);

    /**
     * This method sends a message from the current user to an existing group on a specified topic.
     *
     * @param message  The Message to be sent.
     * @param group     The Group to send the message to.
     * @param topic     The Topic on which the message to send.
     * @param callback The Callback with either the Success or Error message.
     */
    void sendMessage(MASMessage message, MASGroup group,String topic, MASCallback<Void> callback);

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
