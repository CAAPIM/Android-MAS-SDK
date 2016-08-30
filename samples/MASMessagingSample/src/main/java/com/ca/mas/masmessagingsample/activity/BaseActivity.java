/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.mas.masmessagingsample.activity;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.text.TextUtils;

import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.masmessagingsample.R;
import com.ca.mas.masmessagingsample.mas.DataManager;
import com.ca.mas.messaging.MASMessage;

public class BaseActivity extends AppCompatActivity {
    private BroadcastReceiver mMessagingBroadcastReceiver;
    private IntentFilter mMessagingIntentFilter;

    @Override
    protected void onResume() {
        super.onResume();

        mMessagingBroadcastReceiver = getMessagingBroadcastReceiver();
        mMessagingIntentFilter = getMessagingIntentFilter();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(mMessagingBroadcastReceiver, mMessagingIntentFilter);

        MASUser currentUser = MASUser.getCurrentUser();
        if (currentUser != null && currentUser.isAuthenticated()) {
            currentUser.startListeningToMyMessages(new MASCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                }

                @Override
                public void onError(Throwable e) {
                }
            });
        }
    }
    @Override
    public void onPause() {
        LocalBroadcastManager
                .getInstance(this)
                .unregisterReceiver(mMessagingBroadcastReceiver);

        MASUser currentUser = MASUser.getCurrentUser();
        if (currentUser != null && currentUser.isAuthenticated()) {
            currentUser.stopListeningToMyMessages(new MASCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                }

                @Override
                public void onError(Throwable e) {
                }
            });
        }
        super.onPause();
    }

    private BroadcastReceiver getMessagingBroadcastReceiver() {
        if (mMessagingBroadcastReceiver == null) {
            mMessagingBroadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (TextUtils.equals(action, ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED)) {
                        NotificationCompat.Builder builder = getNotificationBuilder(context);
                        try {
                            MASMessage message = MASMessage.newInstance(intent);
                            final String senderName = message.getDisplayName();
                            byte[] messageData = message.getPayload();

                            DataManager.INSTANCE.saveMessage(message);
                            String messageText = new String(messageData);

                            builder.setContentTitle(senderName).setContentText(messageText);
                        } catch (MASException me) {
                            builder.setContentTitle(MASException.class.getSimpleName())
                                    .setContentText("Could not extract the message.");
                        }

                        // Displays the notification
                        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                        notificationManager.notify(0, builder.build());
                    }
                }
            };
        }

        return mMessagingBroadcastReceiver;
    }

    protected IntentFilter getMessagingIntentFilter() {
        if (mMessagingIntentFilter == null) {
            mMessagingIntentFilter = new IntentFilter(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED);
        }

        return mMessagingIntentFilter;
    }

    protected NotificationCompat.Builder getNotificationBuilder(Context context) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
        builder.setSmallIcon(R.drawable.ic_message_text_white_48dp)
                .setColor(getResources().getColor(R.color.colorPrimaryDark))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setVibrate(new long[0]);

        return builder;
    }
}
