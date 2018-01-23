/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.connecta.client;

import android.support.annotation.NonNull;

import com.ca.mas.messaging.MASMessageException;
import com.ca.mas.messaging.MASMessage;

import org.eclipse.paho.client.mqttv3.MqttMessage;

/**
 * <p><b>ConnectaUtil</b> consists of static helper methods that are used by different components of the MAS SDK.
 * The focus of this class is the transformation of MqttMessage objects into {@link MASMessage} objects and
 * {@link <a href="http://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttConnectOptions.html">MqttConnectOptions</a>}
 * into MASConnectOptions objects.</p>
 */
class ConnectaUtil {

    private ConnectaUtil() {
    }

    /**
     * <b>Pre-Conditions: </b> The client must has subscribed to a topic and received a valid message on the
     * <i>messageArrived</i> callback.<br>
     * <b>Description: </b> This is essentially an adapter utility method that maps an MqttMessage
     * onto a proprietary MASMessage.
     *
     * @param mqttMessage {@link <a href="http://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html">MqttMessage</a>}
     * @return {@link MASMessage}
     */
    static MASMessage createMASMessageFromMqtt(@NonNull MqttMessage mqttMessage) {
        MASMessage masMessage = initMessageFromPayload(mqttMessage.getPayload());
        masMessage.setDuplicate(mqttMessage.isDuplicate());
        masMessage.setRetained(mqttMessage.isRetained());

        return masMessage;
    }

    /**
     * <b>Pre-Conditions: </b> Same as {@link ConnectaUtil#createMASMessageFromMqtt}.<br>
     * <b>Description:</b> {@link ConnectaUtil#createMASMessageFromMqtt} - This internal
     * utility method populates a MASMessage with the payload bytes received from an MQTT message.
     *
     * @param payload The payload of the message
     */
    private static MASMessage initMessageFromPayload(byte[] payload) {
        String totalPayload = new String(payload);

        MASMessage m = MASMessage.newInstance();

        try {
            m.createMASMessageFromJSONString(totalPayload);
        } catch (MASMessageException e) {
            m.setPayload(payload);
        }
        return m;
    }

}
