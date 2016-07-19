/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.client;

import android.support.annotation.NonNull;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;

/**
 * <p>The <b>MASConnectaClient</b> interface enforces the Mobile App Services messaging operations.</p>
 * <p>Besides defining the connection/pub/sub operations this interface contains message type and status data representing
 * the Mqtt operations and interactions.
 * This contract for this interface mirrors the MQTT life cycle methods and includes;</p>
 * <ul>
 * <li>Connecting to the message broker on the MAG server.</li>
 * <li>Subscribing to a topic.</li>
 * <li>Publishing to a topic.</li>
 * <li>Unsubscribing from a topic.</li>
 * <li>Disconnecting from a message broker.</li>
 * </ul>
 */
public interface MASConnectaClient {

    int AT_MOST_ONCE = 0;
    int AT_LEAST_ONCE = 1;
    int EXACTLY_ONCE = 2;

    /**
     * <b>Pre-Conditions:</b> The MAG SDK must be initialized and the SSLSocketFactory created.<br>
     * <b>Description:</b> Connect to the gateway using the MAG SDK.
     */
    void connect(MASCallback<Void> callback);

    /**
     * <b>Pre-Conditions:</b> none.<br>
     * <b>Description:</b> Disconnect from the message broker.
     */
    void disconnect(MASCallback<Void> callback);

    /**
     * <b>Pre-Condition:</b> Must have a valid connection.<br>
     * <b>Description:</b> Subscribe to the topic specified in the topic string with the default qos value.
     *
     * @param masTopic - the topic that the broker registers for message transport.
     */
    void subscribe(@NonNull MASTopic masTopic, MASCallback<Void> callback);

    /**
     * <b>Pre-Condition:</b> Must have a valid connection.<br>
     * <b>Description:</b> This method is idempotent so a caller may unsubscribed from the same topic more than once.
     *
     * @param masTopic - the topic previously registered. If the topic does not exist, then this call falls through.
     */
    void unsubscribe(@NonNull MASTopic masTopic, MASCallback<Void> callback);


    /**
     * <b>Pre-Condition:</b> Must have a valid connection.
     * <b>Description:</b> Publish can be accomplished without first subscribing to a topic. If a publish
     * occurs on a topic the caller is subscribed, to, the callbacks for both the sender and receiver will fire.
     *
     * @param masTopic
     * @param message
     */
    void publish(@NonNull MASTopic masTopic, @NonNull MASMessage message, MASCallback<Void> callback);

    /**
     * <b>Pre-Condition:</b> Must have a valid connection.
     * <b>Description:</b> Publish can be accomplished without first subscribing to a topic. If a publish
     * occurs on a topic the caller is subscribed, to, the callbacks for both the sender and receiver will fire.
     *
     * @param masTopic
     * @param message
     */

    void publish(@NonNull final MASTopic masTopic, @NonNull final byte[] message, final MASCallback<Void> callback);

    /**
     * <b>Pre-Condition:</b> None.<br>
     * <b>Description:</b> In order to be called the object cannot be null. Use of this message
     * might occur like;
     * <pre>
     *     if(client != null && client.isConnected()) {
     *         // do something...
     *     }
     * </pre>
     *
     * @return boolean
     */
    boolean isConnected();

    /**
     * <b>Pre-Condition:</b> None.<br>
     * <b>Description:</b> The timeout set here may or may not define the length of time that a connection is
     * attempted. If some other component has a shorter timeout period then the connection attempt will be
     * nullified based on that timeout. A value of 0 means <i>inifinite</i> and the timeout is determined by
     * a lower layer in the stack.
     *
     * @param timeOutInMillis
     */
    void setTimeOutInMillis(long timeOutInMillis);

    /**
     * <b>Pre-Condition:</b> None.<br>
     * <b>Description:</b> This will be the value currently set on the connection and it can be 0.
     *
     * @return long - the timeout in milliseconds
     */
    long getTimeOutInMillis();

    /**
     * <b>Pre-Condition:</b> None.<br>
     * <b>Description:</b> Set the connect options for this client connection. The <i>ConnectOptions</i> class
     * is an extension of MqttConnectionOptions. The purpose of this is to retain the flexibility to change
     * Mqtt providers in the future. Any implementation of connect options from any provider should be
     * extended using {@link ConnectOptions}.
     *
     * @param connectOptions
     */
    void setConnectOptions(ConnectOptions connectOptions);
}
