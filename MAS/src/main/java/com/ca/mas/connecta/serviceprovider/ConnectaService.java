/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.serviceprovider;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.R;
import com.ca.mas.connecta.client.MASConnectOptions;
import com.ca.mas.connecta.client.ConnectaException;
import com.ca.mas.connecta.client.MASConnectaClient;
import com.ca.mas.connecta.client.MASConnectaListener;
import com.ca.mas.connecta.util.ConnectaUtil;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.request.internal.StateRequest;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.foundation.util.FoundationUtil;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * <b>ConnectaService</b> is the bound service container used to
 * control the life cycle of the MAS messaging features. This service is
 * MQTT specific and leverages the PAHO MQTT client package. To use MAS
 * with another messaging client replace this service with the client specific
 * service that implements the MAS interface.
 * </p>
 * References:
 * <ol>
 * <li> <a href="http://mqtt.org/">MQTT Standard</a> </li>
 * <li> <a href="http://www.eclipse.org/paho/">PAHO Mqtt Client Package</a>
 * </ol>
 * {@link com.ca.mas.connecta.client.MASConnectaClient} and {@link android.app.Service}
 * <p>Every call in this class is asynchronous and the calling of the next stage of the service is dependant on the success
 * of the previous methods successful completion. If there is a failure anywhere in the calling process, such as SSLSocketCreation,
 * connecting, etc. then the error callback is invoked ending the service's life.</p>
 */
public class ConnectaService extends Service implements MASConnectaClient {

    private static String TAG = ConnectaService.class.getSimpleName();

    /**
     * <p><b>mMqttClient</b> is the only instance variable the references the Mqtt implementation library.</p>
     */
    private MqttClient mMqttClient;
    private long mTimeOutInMillis;
    private MASConnectOptions mConnectOptions;
    private MessageBroadcaster mMessageBroadcaster;
    private MASConnectaListener connectaListener;

    private final IBinder mBinder = new ServiceBinder();
    private String clientId;

    private List<MASTopic> subscribedTopic = Collections.synchronizedList(new ArrayList<MASTopic>());

    public void setConnectaListener(MASConnectaListener connectaListener) {
        this.connectaListener = connectaListener;
    }

    /**
     * <p>This class is the handle that users of this service manage the service life-cycle with. The method</p>
     * <pre>
     *     public Transport getService() {
     *
     *     }
     * </pre>
     * returns an instance of <i>this</i>.
     */
    public class ServiceBinder extends Binder {
        public ConnectaService getService() {
            return ConnectaService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void connect(final MASCallback<Void> callback) {
        try {
            mMessageBroadcaster = new MessageBroadcaster(this);
            Log.d(TAG, "CONNECTA: connect()");
            MASCallback<Map<String, Object>> masCallback = new MASCallback<Map<String, Object>>() {
                @Override
                public Handler getHandler() {
                    return Callback.getHandler(callback);
                }

                @Override
                public void onSuccess(Map<String, Object> result) {
                    try {
                        Log.d(TAG, "CONNECTA: onSuccess()");
                        initMqttClient((String) result.get(StateRequest.DEVICE_ID));
                        mMqttClient.connect((MqttConnectOptions) result.get(MASConnectOptions.class.getName()));
                        if (!mMqttClient.isConnected()) {
                            Callback.onError(callback, new ConnectaException("Not connected to message broker!"));
                            return;
                        } else {
                            //Once reconnected, re-subscribe the topic
                            for (MASTopic topic: subscribedTopic) {
                                subscribe(topic, null);
                            }
                        }
                        Callback.onSuccess(callback, null);
                    } catch (Exception e) {
                        Log.e(TAG, "" + e.getMessage());
                        Callback.onError(callback, e);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    Callback.onError(callback, error);
                }
            };

            if ((mConnectOptions == null) || (mConnectOptions.getServerURIs() == null)) {
                // For Gateway connection
                // If connect options have not been set
                mConnectOptions = new MASConnectOptions();
                mConnectOptions.initConnectOptions(this, mTimeOutInMillis, masCallback);
            } else {
                // For public broker connection
                // MASConnectOptions has been set
                mConnectOptions.setConnectionTimeout(ConnectaUtil.createConnectionOptions(ConnectaUtil.getBrokerUrl(this), mTimeOutInMillis).getConnectionTimeout());
                Log.d(TAG, "CONNECTA: onSuccess()");
                initMqttClient();
                mMqttClient.connect(mConnectOptions);
                if (!mMqttClient.isConnected()) {
                    Callback.onError(callback, new ConnectaException("Not connected to message broker!"));
                    return;
                }
                Callback.onSuccess(callback, null);
            }
        } catch (Exception e) {
            Log.e(TAG, "" + e.getMessage(), e);
            Callback.onError(callback, e);
        }
    }

    private void initMqttClient() throws MASException, MqttException {
        initMqttClient(null);
    }

    /*
    Called once the secure socket factory has been created to perform the Mqtt initialization.
     */
    private void initMqttClient(String deviceId) throws MASException, MqttException {
        if (mMqttClient != null) {
            return;
        }
        String brokerUrl = ConnectaUtil.getBrokerUrl(getApplicationContext(), mConnectOptions);
        Log.d(TAG, "CONNECTA: brokerUrl: " + brokerUrl);

        // we use a UUID instead of the device ID so there are no restrictions
        // on the number of unique connections that can be made.
        String brokerClientId;
        if (this.clientId == null || brokerUrl.contains(FoundationUtil.getHost())) {
            // Client ID was not set, or if connecting to the gateway, generate a client id
            this.clientId = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getClientId();
            brokerClientId = ConnectaUtil.getMqttClientId(clientId, deviceId);
        } else {
            // Client ID was set, use it
            brokerClientId = this.clientId;
        }

        Log.d(TAG, "CONNECTA: clientId: " + clientId);
        Log.d(TAG, "CONNECTA: brokerClientId: " + brokerClientId);

        final MemoryPersistence memoryPersistence = new MemoryPersistence();



        mMqttClient = new MqttClient(brokerUrl, brokerClientId, memoryPersistence);
        mMqttClient.setCallback(
                new MqttCallback() {

                    @Override
                    public void connectionLost(Throwable throwable) {
                        //Try to reconnect for gateway connect
                        if (mConnectOptions.isGateway()) {
                            mConnectOptions = null;
                            connect(null);
                        }
                        Log.d(TAG, "Connections was lost: " + throwable.getMessage() + ", " + throwable.getCause());
                        if (connectaListener != null) {
                            connectaListener.onConnectionLost();
                        }
                    }

                    @Override
                    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                        Log.d(TAG, "Message Arrived: QOS: " + mqttMessage.getQos() + ", Duplicate?" + mqttMessage.isDuplicate() + ", Retained? " + mqttMessage.isRetained());
                        try {
                            MASMessage masMessage = ConnectaUtil.createMASMessageFromMqtt(mqttMessage);
                            masMessage.setTopic(topic);
                            mMessageBroadcaster.broadcastMessage(masMessage);
                        } catch (Exception je) {
                            if (connectaListener != null) {
                                connectaListener.onInvalidMessageFormat();
                            }
                        }
                    }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        try {
                            Log.d(TAG, "Delivery Complete - token message: " + iMqttDeliveryToken.getMessage());
                            if (connectaListener != null) {
                                connectaListener.onDeliveryCompletedSuccess();
                            }
                        } catch (MqttException me) {
                            if (connectaListener != null) {
                                connectaListener.onDeliveryCompletedFailed(me);
                            }
                        }
                    }
                }
        );
    }

    @Override
    public void setConnectOptions(MASConnectOptions connectOptions) {
        mConnectOptions = connectOptions;
    }

    @Override
    public void disconnect(MASCallback<Void> callback) {
        subscribedTopic.clear();
        if (isConnected()) {
            Log.d(TAG, "Client Disconnected.");
            try {
                mMqttClient.disconnect();
                mConnectOptions = null;
                Callback.onSuccess(callback, null);
            } catch (Exception e) {
                Callback.onError(callback, e);
            }
        } else {
            Callback.onError(callback, new ConnectaException(getResources().getString(R.string.could_not_disconnect)));
        }
    }

    @Override
    public void subscribe(@NonNull MASTopic masTopic, MASCallback<Void> callback) {
        if (isConnected()) {
            try {
                mMqttClient.subscribe(masTopic.toString(), masTopic.getQos());
                if (!subscribedTopic.contains(masTopic)) {
                    subscribedTopic.add(masTopic);
                }
                Callback.onSuccess(callback, null);
            } catch (Exception e) {
                Callback.onError(callback, e);
            }
        } else {
            Callback.onError(callback, new ConnectaException(getResources().getString(R.string.could_not_subscribe)));
        }
    }

    @Override
    public void unsubscribe(@NonNull MASTopic topic, MASCallback<Void> callback) {
        if (isConnected()) {
            try {
                mMqttClient.unsubscribe(topic.toString());
                subscribedTopic.remove(topic);
                Callback.onSuccess(callback, null);
            } catch (Exception e) {
                Callback.onError(callback, e);
            }
        } else {
            Callback.onError(callback, new ConnectaException(getResources().getString(R.string.could_not_unsubscribe)));
        }
    }

    @Override
    public void publish(@NonNull MASTopic masTopic, @NonNull MASMessage message, MASCallback<Void> callback) {
        try {
            MqttMessage mqttMessage = new MqttMessage();
            byte[] bytes = message.createJSONStringFromMASMessage(getApplicationContext()).getBytes();
            mqttMessage.setPayload(bytes);
            mqttMessage.setQos(message.getQos());
            mqttMessage.setRetained(message.isRetained());
            publish(masTopic, mqttMessage, callback);
        } catch (MASException me) {
            Callback.onError(callback, me);
        }
    }

    public void publish(@NonNull MASTopic topic, @NonNull byte[] message, MASCallback<Void> callback) {
        MqttMessage mqttMessage = new MqttMessage();
        mqttMessage.setPayload(message);
        mqttMessage.setQos(topic.getQos());
        publish(topic, mqttMessage, callback);
    }

    private void publish(@NonNull MASTopic topic, @NonNull MqttMessage mqttMessage, MASCallback<Void> callback) {
        if (isConnected()) {
            try {
                mMqttClient.publish(topic.toString(), mqttMessage);
                Callback.onSuccess(callback, null);
            } catch (Exception e) {
                Callback.onError(callback, e);
            }
        } else {
            Callback.onError(callback, new ConnectaException(getResources().getString(R.string.could_not_publish)));
        }
    }

    @Override
    public boolean isConnected() {
        if (mMqttClient != null) {
            return mMqttClient.isConnected();
        }
        return false;
    }

    @Override
    public void setTimeOutInMillis(long timeOutInMillis) {
        mTimeOutInMillis = timeOutInMillis;
    }

    @Override
    public long getTimeOutInMillis() {
        return mTimeOutInMillis;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
