/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.client;

import android.content.Context;

import com.ca.mas.connecta.util.ConnectaUtil;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.io.ssl.MAGSocketFactory;
import com.ca.mas.core.request.internal.StateRequest;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASResultReceiver;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

/**
 * <i>ConnectOptions</i> is the interface to the  <a href="https://www.eclipse.org/paho/files/javadoc/org/eclipse/paho/client/mqttv3/MqttConnectOptions.html">MqttConnectOptions</a>
 * class. This class uses the MAG SSLSocketFactory to create a mutually authenticated connection with the Mqtt broker.
 */
public class ConnectOptions extends MqttConnectOptions {

    private static String TAG = ConnectOptions.class.getSimpleName();

    private String hostname;
    private Integer portNumber;

    /*
    public void initConnectNonSecure(final Context context, final long timeOutInMillis, final MASResultReceiver<MqttConnectOptions> result) {
        final MqttConnectOptions connectOptions = ConnectaUtil.createConnectionOptions(ConnectaUtil.getBrokerUrl(context, hostname, portNumber), timeOutInMillis);

        result.onSuccess(new MAGResponse<MqttConnectOptions>() {
            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }

            @Override
            public int getResponseCode() {
                return 0;
            }

            @Override
            public String getResponseMessage() {
                return null;
            }

            @Override
            public MAGResponseBody<MqttConnectOptions> getBody() {
                return new MAGResponseBody<MqttConnectOptions>() {
                    @Override
                    public MqttConnectOptions getContent() {
                        return connectOptions;
                    }
                };
            }
        });
    }*/

    public void initConnectOptions(final Context context, final long timeOutInMillis, final MASCallback<Map<String, Object>> callback) {

        final MASResultReceiver<JSONObject> receiver = new MASResultReceiver<JSONObject>(Callback.getHandler(callback)) {
            @Override
            public void onSuccess(final MAGResponse<JSONObject> response) {
                JSONObject jobj = response.getBody().getContent();
                String oauthToken = jobj.optString(StateRequest.ACCESS_TOKEN);
                final MqttConnectOptions connectOptions = ConnectaUtil.createConnectionOptions(ConnectaUtil.getBrokerUrl(context), timeOutInMillis);
                SSLSocketFactory sslSocketFactory = new MAGSocketFactory(context).createSSLSocketFactory();
                connectOptions.setSocketFactory(sslSocketFactory);
                String uname = MASUser.getCurrentUser().getUserName();
                connectOptions.setUserName(uname);
                connectOptions.setPassword(oauthToken.toCharArray());

                Map<String, Object> info = new HashMap<>();
                info.put(StateRequest.DEVICE_ID, jobj.optString(StateRequest.DEVICE_ID));
                info.put(ConnectOptions.class.getName(), connectOptions);

                Callback.onSuccess(callback, info);
            }

            @Override
            public void onError(MAGError magError) {
                Callback.onError(callback, magError);
            }
        };

        if (MASUser.getCurrentUser() != null && MASUser.getCurrentUser().getUserName() != null) {
            retrieveOauthToken(receiver);
        } else {
            MASUser.login(new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser result) {
                    retrieveOauthToken(receiver);
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });
        }
    }

    private void retrieveOauthToken(final MASResultReceiver<JSONObject> result) {
        MobileSsoFactory.getInstance().processRequest(new StateRequest(), result);
    }

    public String getHostname() {
        return hostname;
    }

    public Integer getPortNumber() {
        return portNumber;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setPortNumber(Integer portNumber) {
        this.portNumber = portNumber;
    }
}
