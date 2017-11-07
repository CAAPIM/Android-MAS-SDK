/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.push;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASDevice;
import com.ca.mas.foundation.MASGrantFlow;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.notify.Callback;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Observable;
import java.util.Observer;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class MASPush {

    private static final int NON_BOUND = 0;
    private static final int BOUND = 1;
    public static final String PUSH_BINDING = "com.ca.mas.push.BINDING";
    public static final String ERROR = "error";

    private static MASPush masPush = new MASPush();

    static final Observer PUSH_BINDING_OBSERVER = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            MASPush.getInstance().bind(null);
        }
    };

    static final Observer STARTED_OBSERVER = new Observer() {
        @Override
        public void update(Observable observable, Object o) {
            if (MASDevice.getCurrentDevice().isRegistered() && !MASPush.getInstance().isBound()) {
                MASPush.getInstance().bind(MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS, null);
            }
        }
    };

    private MASPush() {
    }

    public static MASPush getInstance() {
        return masPush;
    }

    /**
     * Bind the user or device to the Push registration token
     *
     * @param callback Callback to receive the registration result. For implicit push registration (triggered by {@link MASFirebaseInstanceIdService},
     *                 or {@link MASPushContentProvider}, an local broadcast with action ${@link MASPush#PUSH_BINDING} and if there is any error
     *                 bundle extra {@link MASPush#ERROR with the exception will be sent.
     */
    public void bind(final MASCallback<Void> callback) {
        bind(null, callback);
    }

    void bind(@MASGrantFlow Integer grantFlow, final MASCallback<Void> callback) {

        try {
            if (FirebaseInstanceId.getInstance().getToken() == null) {
                notifyResult(callback, new IllegalStateException("Firebase Registration Token not ready."));
                return;
            }
        } catch (Exception e) {
            //May flow IllegalStateException went Firebase is not initialized
            notifyResult(callback, e);
            return;
        }

        //TODO Endpoint needs to be updated
        Uri.Builder builder = new Uri.Builder();
        builder.appendPath("notification").appendPath("bind");

        JSONObject body = new JSONObject();
        try {
            body.put("registration_token", FirebaseInstanceId.getInstance().getToken());
            body.put("environment", "android");
        } catch (JSONException e) {
            //ignore
        }
        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(builder.build())
                .post(MASRequestBody.jsonBody(body));
        if (grantFlow != null) {
            if (grantFlow == MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS) {
                requestBuilder.clientCredential();
            } else if (grantFlow == MASConstants.MAS_GRANT_FLOW_PASSWORD) {
                requestBuilder.password();
            }
        }

        MAS.invoke(requestBuilder.build(), new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                if (DEBUG) Log.d(TAG, "Push Notification Successful Registered");
                updateStatus(BOUND);
                notifyResult(callback, null);
            }

            @Override
            public void onError(Throwable e) {
                if (DEBUG) Log.e(TAG, "Push Notification registration failed.", e);
                updateStatus(NON_BOUND);
                notifyResult(callback, e);
            }
        });
    }

    private void notifyResult(MASCallback<Void> callback, Throwable e) {
        if (callback == null) { //Only send broadcast for implicit push binding
            Intent intent = new Intent(PUSH_BINDING);
            if (e != null) {
                intent.putExtra(ERROR, e);
            }
            LocalBroadcastManager.getInstance(MAS.getContext()).sendBroadcast(intent);
        } else {
            if (e != null) {
                Callback.onError(callback, e);
            } else {
                Callback.onSuccess(callback, null);
            }
        }
    }

    void clear(Context context) {
        SharedPreferences sp = context.getSharedPreferences(context
                        .getString(R.string.push_binding_status)
                , Context.MODE_PRIVATE);
        sp.edit().clear().commit();
    }

    /**
     * Check if the InstanceID has been successfully registered to MAG
     *
     * @return true if success, false for now success
     */
    public boolean isBound() {
        SharedPreferences sp = MAS.getContext().getSharedPreferences(MAS.getContext()
                        .getString(R.string.push_binding_status)
                , Context.MODE_PRIVATE);
        return BOUND == sp.getInt(MASConfiguration.getCurrentConfiguration().getGatewayHostName(), NON_BOUND);
    }

    private void updateStatus(int status) {
        SharedPreferences sp = MAS.getContext().getSharedPreferences(MAS.getContext()
                        .getString(R.string.push_binding_status)
                , Context.MODE_PRIVATE);
        sp.edit().putInt(MASConfiguration.getCurrentConfiguration().getGatewayHostName(), status)
                .apply();
    }

}
