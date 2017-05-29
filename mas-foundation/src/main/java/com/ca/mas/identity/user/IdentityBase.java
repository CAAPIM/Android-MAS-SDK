/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import android.support.annotation.NonNull;

import com.ca.mas.foundation.FoundationConsts;
import com.ca.mas.foundation.MASTransformable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * <p><b>IdentityBase</b> is a {@link MASTransformable} class that is used to represent SCIM attributes that are share a common
 * description set. In particular, those SCIM representations that contain a type and a value.</p>
 */
public class IdentityBase implements MASTransformable {

    private String mType;
    private String mValue;

    /**
     * <b>Description:</b> Getter to return the type for this SCIM representation.
     *
     * @return String - the type that is based on the implementing class' type and value context.
     */
    public String getType() {
        return mType;
    }

    /**
     * <b>Description:</b> Getter to return the value for this SCIM representation.
     *
     * @return String the value that is based on the implementing class' type and value context.
     */
    public String getValue() {
        return mValue;
    }

    @Override
    public void populate(@NonNull JSONObject jobj) throws JSONException {
        mType = jobj.optString(FoundationConsts.KEY_TYPE);
        mValue = jobj.optString(FoundationConsts.KEY_VALUE);
    }

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        JSONObject jobj = new JSONObject();
        jobj.put(FoundationConsts.KEY_TYPE, mType);
        jobj.put(FoundationConsts.KEY_VALUE, mValue);
        return jobj;
    }

    @Override
    public String toString() {
        return "\n\tType: " + getType() + "\n\tValue: " + getValue() + "\n";
    }
}
