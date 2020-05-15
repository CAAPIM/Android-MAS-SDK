/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.client;

import android.os.Handler;
import androidx.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.util.Functions;
import com.ca.mas.core.EventDispatcher;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Observable;
import java.util.Observer;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * <p>The <b>MASConnectaManager</b> is the implementation of the {@link com.ca.mas.connecta.client.MASConnectaClient}.
 * The MASConnectaManager implementation used to interface between the messaging transport and the proprietary Android service that leverages the Mqtt library.</p>
 */
public class MASConnectaManager implements MASConnectaClient, Observer {

    private static MASConnectaManager instance = new MASConnectaManager();
    private MASConnectOptions connectOptions;
    private MASConnectaListener connectaListener;
    private String clientId;
    private MqttAndroidClient mqttAndroidClient;

    private MASConnectaManager() {
        EventDispatcher.STOP.addObserver(this);
        EventDispatcher.LOGOUT.addObserver(this);
        EventDispatcher.AFTER_DEREGISTER.addObserver(this);
        EventDispatcher.RESET_LOCALLY.addObserver(this);
        EventDispatcher.BEFORE_GATEWAY_SWITCH.addObserver(this);
    }

    public static MASConnectaManager getInstance() {
        return instance;
    }

    public void setConnectaListener(MASConnectaListener listener) {
        connectaListener = listener;
    }

    private MqttConnecta getMqttConnecta() {
        //If no connect Options is provided, default to the gateway
        if ((connectOptions == null) || (connectOptions.getServerURIs() == null)) {
            return new GatewayMqttConnecta();
        } else {
            return new PublicMqttConnecta((clientId));
        }
    }

    @Override
    public synchronized void connect(final MASCallback<Void> callback) {

        if (isConnected()) {
            Callback.onSuccess(callback, null);
            return;
        }
        final MqttConnecta mqttConnecta = getMqttConnecta();

        if (connectOptions == null) {
            connectOptions = new MASConnectOptions();
        }

        //Initialize the credentials and connection Factory for the connect option.
        mqttConnecta.init(connectOptions, new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mqttAndroidClient = new MqttAndroidClient(MAS.getContext(), mqttConnecta.getServerUri(), mqttConnecta.getClientId(), new MemoryPersistence());
                registerCallback();

                try {
                    mqttAndroidClient.connect(connectOptions, null, new IMqttActionListener() {
                        @Override
                        public void onSuccess(IMqttToken asyncActionToken) {
                            if (DEBUG) Log.d(TAG, "Success connect to mqtt broker");
                            Callback.onSuccess(callback, null);
                        }

                        @Override
                        public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                            if (DEBUG) Log.e(TAG, "Failed to connect to mqtt broker", exception);
                            Callback.onError(callback, exception);
                        }
                    });
                } catch (MqttException e) {
                    Callback.onError(callback, e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    @Override
    public synchronized void disconnect(MASCallback<Void> callback) {
        if (DEBUG) Log.d(TAG, "Disconnecting mqtt broker...");
        try {
            if (mqttAndroidClient != null) {
                mqttAndroidClient.disconnect();
            }
            Callback.onSuccess(callback, null);
        } catch (MqttException e) {
            Callback.onError(callback, e);
        } finally {
            mqttAndroidClient = null;
        }
    }

    @Override
    public void subscribe(@NonNull final MASTopic masTopic, final MASCallback<Void> callback) {
        connectAndExecute(new Functions.NullaryVoid() {
            @Override
            public void call() {
                subscribeTopic(masTopic, callback);
            }
        }, callback);

    }

    private void subscribeTopic(@NonNull final MASTopic topic, final MASCallback<Void> callback) {
        try {
            mqttAndroidClient.subscribe(topic.toString(), topic.getQos(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Callback.onSuccess(callback, null);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Callback.onError(callback, throwable);
                }
            });
        } catch (Exception e) {
            Callback.onError(callback, e);
        }
    }

    private void connectAndExecute(@NonNull final Functions.NullaryVoid function, final MASCallback<Void> callback) {
        if (!isConnected()) {
            connect(new MASCallback<Void>() {
                @Override
                public Handler getHandler() {
                    return Callback.getHandler(callback);
                }

                @Override
                public void onSuccess(Void result) {
                    function.call();
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });
        } else {
            function.call();
        }
    }

    @Override
    public void unsubscribe(@NonNull final MASTopic masTopic, final MASCallback<Void> callback) {

        connectAndExecute(new Functions.NullaryVoid() {
            @Override
            public void call() {
                unsubscribeTopic(masTopic, callback);
            }
        }, callback);
    }

    private void unsubscribeTopic(@NonNull final MASTopic topic, final MASCallback<Void> callback) {
        try {
            mqttAndroidClient.unsubscribe(topic.toString(), null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Callback.onSuccess(callback, null);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Callback.onError(callback, throwable);
                }
            });
        } catch (Exception e) {
            Callback.onError(callback, e);
        }
    }

    public void publish(@NonNull final MASTopic masTopic, @NonNull final String message, final MASCallback<Void> callback) {
        publish(masTopic, message.getBytes(), callback);
    }

    @Override
    public void publish(@NonNull final MASTopic topic, @NonNull final byte[] message, final MASCallback<Void> callback) {

        connectAndExecute(new Functions.NullaryVoid() {
            @Override
            public void call() {
                MqttMessage mqttMessage = new MqttMessage();
                mqttMessage.setPayload(message);
                mqttMessage.setQos(topic.getQos());
                publish(topic, mqttMessage, callback);

            }
        }, callback);
    }

    @Override
    public void publish(@NonNull final MASTopic masTopic, @NonNull final MASMessage message, final MASCallback<Void> callback) {

        connectAndExecute(new Functions.NullaryVoid() {
            @Override
            public void call() {
                MqttMessage mqttMessage = new MqttMessage();
                byte[] bytes = message.createJSONStringFromMASMessage(null).getBytes();
                mqttMessage.setPayload(bytes);
                mqttMessage.setQos(message.getQos());
                mqttMessage.setRetained(message.isRetained());
                publish(masTopic, mqttMessage, callback);
            }
        }, callback);

    }

    private void publish(@NonNull MASTopic topic, @NonNull MqttMessage mqttMessage, final MASCallback<Void> callback) {
        try {
            mqttAndroidClient.publish(topic.toString(), mqttMessage, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    Callback.onSuccess(callback, null);
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    Callback.onError(callback, throwable);
                }
            });
        } catch (Exception e) {
            Callback.onError(callback, e);
        }
    }

    private void registerCallback() {

        final MessageBroadcaster broadcaster = new MessageBroadcaster(MAS.getContext());
        mqttAndroidClient.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable throwable) {
                if (DEBUG)
                    Log.d(TAG, "Connection lost", throwable);
                if (connectaListener != null) {
                    connectaListener.onConnectionLost();
                }
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                if (DEBUG)
                    Log.d(TAG, "Message Arrived: QOS: " + mqttMessage.getQos() + ", duplicate?" + mqttMessage.isDuplicate() + ", retained? " + mqttMessage.isRetained());
                try {
                    MASMessage masMessage = ConnectaUtil.createMASMessageFromMqtt(mqttMessage);
                    masMessage.setTopic(topic);
                    broadcaster.broadcastMessage(masMessage);
                } catch (Exception je) {
                    if (connectaListener != null) {
                        connectaListener.onInvalidMessageFormat();
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                try {
                    if (DEBUG)
                        Log.d(TAG, "Delivery Complete: token message: " + iMqttDeliveryToken.getMessage());
                    if (connectaListener != null) {
                        connectaListener.onDeliveryCompletedSuccess();
                    }
                } catch (MqttException me) {
                    if (connectaListener != null) {
                        connectaListener.onDeliveryCompletedFailed(me);
                    }
                }
            }
        });
    }


    @Override
    public boolean isConnected() {
        return mqttAndroidClient != null && mqttAndroidClient.isConnected();
    }

    @Override
    public void setConnectOptions(MASConnectOptions connectOptions) {
        this.connectOptions = connectOptions;
    }

    @Override
    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public void update(Observable o, Object arg) {
        try {
            disconnect(null);
        } catch (Exception ignore) {
            //Ignore
        }
    }
}
