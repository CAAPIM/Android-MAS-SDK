/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.connecta.client;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.util.Properties;
/**
 * <i>MASConnectOptions</i> is the interface to the  <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttConnectOptions.html">MqttConnectOptions</a>
 * class. This class uses the MAG SSLSocketFactory to create a mutually authenticated connection with the Mqtt broker.
 */
public class MASConnectOptions extends MqttConnectOptions {

    @Override
    public Properties getDebug() {
        Properties p = super.getDebug();
        String s = "";
        if (getServerURIs() != null && getServerURIs().length > 0) {
            s = getServerURIs()[0];
        }
        p.put("ServerURI", s);
        return p;
    }
}
