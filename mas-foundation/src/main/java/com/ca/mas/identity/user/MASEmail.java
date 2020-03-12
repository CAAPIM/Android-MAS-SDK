/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import androidx.annotation.NonNull;

import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONException;
import org.json.JSONObject;

public class MASEmail extends IdentityBase {

    private boolean mIsPrimary;

    public boolean isPrimary() {
        return mIsPrimary;
    }

    @Override
    public void populate(@NonNull JSONObject jobj) throws JSONException {
        super.populate(jobj);
        mIsPrimary = jobj.has(IdentityConsts.KEY_EMAIL_PRIMARY) && jobj.getBoolean(IdentityConsts.KEY_EMAIL_PRIMARY);
    }

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        JSONObject jobj = super.getAsJSONObject();
        jobj.put(IdentityConsts.KEY_EMAIL_PRIMARY, mIsPrimary);
        return jobj;
    }
}
