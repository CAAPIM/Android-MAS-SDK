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
import com.ca.mas.foundation.MASGrantFlow;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.notify.Callback;
import com.google.firebase.iid.FirebaseInstanceId;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

public class MASPush {

    private static final int NON_REGISTERED = 0;
    private static final int REGISTERED = 1;
    public static final String PUSH_REGISTRATION = "com.ca.mas.push.REGISTRATION";
    public static final String STATUS = "status";

    private static MASPush masPush = new MASPush();
    private boolean isTokenRefresh = false;

    private MASPush() {
    }

    public static MASPush getInstance() {
        return masPush;
    }

    /**
     * Token is refreshing
     */
    void setTokenRefresh() {
        isTokenRefresh = true;

    }

    /**
     * @return true when {@link MASFirebaseInstanceIdService} getting a token refresh event and process
     * the token even update
     */
    boolean isTokenRefresh() {
        return isTokenRefresh;
    }

    public void register(final MASCallback<Void> callback) {
        this.register(MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS, callback);
    }

    /**
     * Register the Instance ID to MAG
     *
     * @param callback Callback to receive the registration result. For implicit push registration (triggered by {@link MASFirebaseInstanceIdService},
     *                 or {@link MASPushContentProvider}, and local broadcast with action ${@link MASPush#PUSH_REGISTRATION} and bundle extra {@link MASPush#STATUS}
     *                 will be sent. The {@link MASPush#STATUS} will contains either {@link MASPush#REGISTERED} or {@link MASPush#NON_REGISTERED}
     */
    public void register(@MASGrantFlow int grantFlow, final MASCallback<Void> callback) {

        try {
            if (FirebaseInstanceId.getInstance().getToken() == null) {
                Callback.onError(callback, new IllegalStateException("Firebase Registration Token not ready."));
                return;
            }
        } catch (Exception e) {
            Callback.onError(callback, e);
            return;
        }

        //TODO Endpoint needs to be updated
        Uri.Builder builder = new Uri.Builder();
        builder.appendPath("notification").appendPath("register");

        JSONObject body = new JSONObject();
        try {
            body.put("registration_token", FirebaseInstanceId.getInstance().getToken());
            body.put("environment", "android");
        } catch (JSONException e) {
            //ignore
        }
        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(builder.build())
                .post(MASRequestBody.jsonBody(body));
        if (grantFlow == MASConstants.MAS_GRANT_FLOW_CLIENT_CREDENTIALS) {
            requestBuilder.clientCredential();
        } else {
            requestBuilder.password();
        }

        MAS.invoke(requestBuilder.build(), new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                if (DEBUG) Log.d(TAG, "Push Notification Successful Registered");
                isTokenRefresh = false;
                updateStatus(REGISTERED);
                if (callback == null) { //Only send broadcast for implicit push registration
                    Intent intent = new Intent(PUSH_REGISTRATION);
                    intent.putExtra(STATUS, REGISTERED);
                    LocalBroadcastManager.getInstance(MAS.getContext()).sendBroadcast(intent);
                } else {
                    Callback.onSuccess(callback, null);
                }
            }

            @Override
            public void onError(Throwable e) {
                if (DEBUG) Log.e(TAG, "Push Notification registration failed.", e);
                isTokenRefresh = false;
                updateStatus(NON_REGISTERED);
                if (callback == null) { //Only send broadcast for implicit push registration
                    Intent intent = new Intent(PUSH_REGISTRATION);
                    intent.putExtra(STATUS, NON_REGISTERED);
                    LocalBroadcastManager.getInstance(MAS.getContext()).sendBroadcast(intent);
                } else {
                    Callback.onError(callback, e);
                }
            }
        });
    }

    /**
     * Check if the InstanceID has been successfully registered to MAG
     *
     * @return true if success, false for now success
     */
    public boolean isRegistered() {
        SharedPreferences sp = MAS.getContext().getSharedPreferences(MAS.getContext()
                        .getString(R.string.push_registration_status)
                , Context.MODE_PRIVATE);
        return REGISTERED == sp.getInt(MASConfiguration.getCurrentConfiguration().getGatewayHostName(), NON_REGISTERED);
    }

    private void updateStatus(int status) {
        SharedPreferences sp = MAS.getContext().getSharedPreferences(MAS.getContext()
                        .getString(R.string.push_registration_status)
                , Context.MODE_PRIVATE);
        sp.edit().putInt(MASConfiguration.getCurrentConfiguration().getGatewayHostName(), status)
                .apply();
    }

}
