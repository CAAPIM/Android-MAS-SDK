/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;

import com.ca.mas.connecta.serviceprovider.ConnectaService;
import com.ca.mas.core.util.Functions;
import com.ca.mas.core.EventDispatcher;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;

import java.util.Observable;
import java.util.Observer;

/**
 * <p>The <b>MASConnectaManager</b> is the implementation of the {@link com.ca.mas.connecta.client.MASConnectaClient}.
 * The MASConnectaManager implementation used to interface between the messaging transport and the proprietary Android service that leverages the Mqtt library.</p>
 */
public class MASConnectaManager implements MASConnectaClient, Observer {

    private Context mContext;
    private static MASConnectaManager instance = new MASConnectaManager();
    private ConnectaService mMASTransportService;
    private long mTimeOutInMillis;
    private MASConnectOptions mConnectOptions;
    private MASConnectaListener connectaListener;
    private MASCallback<Void> connectCallback;
    private String clientId;

    /*
    This service connection uses IPC Binder to load the MQTT library specific service to perform Mqtt operations.
     */
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            ConnectaService.ServiceBinder masBinder = (ConnectaService.ServiceBinder) iBinder;
            mMASTransportService = masBinder.getService();

            if (mMASTransportService != null) {
                mMASTransportService.setClientId(clientId);
                mMASTransportService.setTimeOutInMillis(getTimeOutInMillis());
                mMASTransportService.setConnectOptions(mConnectOptions);
                mMASTransportService.connect(new MASCallback<Void>() {

                    @Override
                    public Handler getHandler() {
                        return Callback.getHandler(connectCallback);
                    }

                    @Override
                    public void onSuccess(Void object) {
                        if (connectaListener != null) {
                            mMASTransportService.setConnectaListener(connectaListener);
                        }
                        if (connectCallback != null) {
                            connectCallback.onSuccess(null);
                            connectCallback = null;
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (connectCallback != null) {
                            connectCallback.onError(e);
                            connectCallback = null;
                        }
                    }
                });
            } else {
                if (connectCallback != null) {
                    connectCallback.onError(new ConnectaException("Failed to bind Transport Service"));
                    connectCallback = null;
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mMASTransportService = null;
        }
    };

    private MASConnectaManager() {
        EventDispatcher.LOGOUT.addObserver(this);
        EventDispatcher.DE_REGISTER.addObserver(this);
        EventDispatcher.RESET_LOCALLY.addObserver(this);
        EventDispatcher.BEFORE_GATEWAY_SWITCH.addObserver(this);
    }

    public static MASConnectaManager getInstance() {
        return instance;
    }

    public void setConnectaListener(MASConnectaListener listener) {
        connectaListener = listener;
        if (mMASTransportService != null) {
            mMASTransportService.setConnectaListener(listener);
        }
    }

    public void start(@NonNull Context context) {
        mContext = context.getApplicationContext();
    }

    public void stop() {
        disconnect(null);
    }

    @Override
    public synchronized void connect(MASCallback<Void> callback) {
        if (isConnected()) {
            Callback.onSuccess(callback, null);
            return;
        }
        if (mMASTransportService == null) {
            connectCallback = callback;
            Intent intent = new Intent(mContext, ConnectaService.class);
            mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        } else {
            mMASTransportService.connect(callback);
        }
    }

    @Override
    public void disconnect(MASCallback<Void> callback) {
        if (isConnected()) {
            mConnectOptions = null;
            mMASTransportService.disconnect(callback);
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
        Handler h = new Handler(mContext.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                mMASTransportService.subscribe(topic, callback);
            }
        });
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
        Handler h = new Handler();
        h.post(new Runnable() {
            @Override
            public void run() {
                mMASTransportService.unsubscribe(topic, callback);
            }
        });
    }

    public void publish(@NonNull final MASTopic masTopic, @NonNull final String message, final MASCallback<Void> callback) {
        publish(masTopic, message.getBytes(), callback);
    }

    @Override
    public void publish(@NonNull final MASTopic masTopic, @NonNull final byte[] message, final MASCallback<Void> callback) {

        connectAndExecute(new Functions.NullaryVoid() {
            @Override
            public void call() {
                publishTopic(masTopic, message, callback);
            }
        }, callback);
    }

    @Override
    public void publish(@NonNull final MASTopic masTopic, @NonNull final MASMessage masMessage, final MASCallback<Void> callback) {

        connectAndExecute(new Functions.NullaryVoid() {
            @Override
            public void call() {
                mMASTransportService.publish(masTopic, masMessage, callback);
            }
        }, callback);

    }

    private void publishTopic(@NonNull final MASTopic masTopic, @NonNull final byte[] message, final MASCallback<Void> callback) {

        Handler h = new Handler(mContext.getMainLooper());
        h.post(new Runnable() {
            @Override
            public void run() {
                mMASTransportService.publish(masTopic, message, callback);
            }
        });
    }

    @Override
    public boolean isConnected() {
        return mMASTransportService != null && mMASTransportService.isConnected();
    }

    @Override
    public void setConnectOptions(MASConnectOptions connectOptions) {
        mConnectOptions = connectOptions;
        if (mMASTransportService != null) {
            mMASTransportService.setConnectOptions(connectOptions);
        }
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

    @Override
    public void update(Observable o, Object arg) {
        disconnect(null);
    }
}
