/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.util.FoundationUtil;
import com.ca.mas.messaging.MASMessage;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p><b>ConnectaUtil</b> consists of static helper methods that are used by different components of the MAS SDK. ConnectaUtil
 * is the only class besides {@link com.ca.mas.connecta.serviceprovider.ConnectaService} that references the MQTT protocol.
 * The focus of this class is the transformation of MqttMessage objects into {@link MASMessage} objects and
 * {@link <a href="http://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttConnectOptions.html">MqttConnectOptions</a>}
 * into MASConnectOptions objects.</p>
 */
public class ConnectaUtil extends FoundationUtil {

    /**
     * <mag_identifier>::<client_id>::<SCIM userID>
     *
     * @return the formatted clientId
     */
    public static String getMqttClientId(String clientId, String magIdentifier, boolean isGateway) {
        String mqttClientId = "";
        if( isGateway ){
            mqttClientId += magIdentifier + ConnectaConsts.CLIENT_ID_SEP;
        }

        mqttClientId += clientId;

        if( MASUser.getCurrentUser() != null ){
            // If user is logged in, get username to put in clientId
            mqttClientId += ConnectaConsts.CLIENT_ID_SEP + MASUser.getCurrentUser().getUserName();
        }

        return mqttClientId;
    }

    /**
     * <b>Pre-Conditions: </b> The client must has subscribed to a topic and received a valid message on the
     * <i>messageArrived</i> callback.<br>
     * <b>Description: </b> This is essentially an adapter utility method that maps an MqttMessage
     * onto a proprietary MASMessage.
     *
     * @param mqttMessage {@link <a href="http://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttMessage.html">MqttMessage</a>}
     * @return {@link MASMessage}
     * @throws JSONException
     */
    public static MASMessage createMASMessageFromMqtt(@NonNull MqttMessage mqttMessage) throws MASException {
        MASMessage masMessage = initMessageFromPayload(mqttMessage.getPayload());
        masMessage.setDuplicate(mqttMessage.isDuplicate());
        masMessage.setRetained(mqttMessage.isRetained());

        return masMessage;
    }

    /**
     * <b>Pre-Conditions: </b> A valid connection must exist.<br>
     * <b>Description:</b> This helper method sets the MqttConnection options for a <i>publish</i>
     * invocation. The MqttConnectOptions is used to set parameters, such as the timeOutInMillis paramter,
     * whether the session should be cleared, and the SSLSocketFactory created by the MAG server handshake.
     *
     * @return {@link <a href="http://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttConnectOptions.html">MqttConnectOptions</a>}
     */
    public static MqttConnectOptions createConnectionOptions(String brokerUrl, long timeOutInMillis) {
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        // unless the timeout has been set to be at least the default, we will let MQTT decide how long the timeout is to be.
        if (timeOutInMillis >= ConnectaConsts.TIMEOUT_VAL) {
            // timeout converted to seconds. The default for MQTT is 30 seconds. 0 means no timeout
            int toSeconds = (int) (timeOutInMillis / ConnectaConsts.SEC_MILLIS);
            mqttConnectOptions.setConnectionTimeout(toSeconds);
        }

        String[] servers = {brokerUrl};
        mqttConnectOptions.setServerURIs(servers);
        return mqttConnectOptions;
    }

    /**
     * <b>Pre-Conditions: </b> Same as {@link com.ca.mas.connecta.util.ConnectaUtil#createMASMessageFromMqtt}.<br>
     * <b>Description:</b> {@link com.ca.mas.connecta.util.ConnectaUtil#createMASMessageFromMqtt} - This internal
     * utility method populates a MASMessage with the payload bytes received from an MQTT message.
     *
     * @param payload
     * @throws MASException
     */
    private static MASMessage initMessageFromPayload(byte[] payload) throws MASException {
        String totalPayload = new String(payload);

        MASMessage m = MASMessage.newInstance();
        if (totalPayload.startsWith("{")) {
            m.createMASMessageFromJSONString(totalPayload);
        } else {
            m.setPayload(payload);
        }
        return m;
    }

}
