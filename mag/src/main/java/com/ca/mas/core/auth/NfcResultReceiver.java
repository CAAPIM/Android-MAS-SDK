/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import com.ca.mas.core.MAGResultReceiver;

import org.json.JSONObject;

/**
 * The default NFC result receiver. To avoid polling the MAG, this receiver will notify
 * the session request device after it successfully authenticates the user session.
 */
public abstract class NfcResultReceiver extends MAGResultReceiver implements AuthResultReceiver {

   private JSONObject data;

    public JSONObject getData() {
        return data;
    }

    @Override
    public void setData(JSONObject data) {
        this.data = data;
    }
}
