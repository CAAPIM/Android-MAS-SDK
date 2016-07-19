/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import android.os.Handler;
import android.os.ResultReceiver;

import org.json.JSONObject;

/**
 * Abstract interface for cross device result receiver.
 * Use the result receiver under {@link com.ca.mas.core.MobileSso#authorize}
 */
public abstract class AuthResultReceiver extends ResultReceiver {

    protected JSONObject json;
    /**
     * Create a new ResultReceive to receive results.  Your
     * {@link #onReceiveResult} method will be called from the thread running
     * <var>handler</var> if given, or from an arbitrary thread if null.
     *
     * @param handler
     */
    public AuthResultReceiver(Handler handler) {
        super(handler);
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }
}
