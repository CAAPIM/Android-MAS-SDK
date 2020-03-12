/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.common;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASTransformable;
import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * <p><b>Attributes</b> is the base class used to hold specific attributes that are defined by the implementing, or concrete class. These
 * attributes are the returned values from a SCIM SCHEMA call and contain the attributes supported by this SCIM impelentation on the server.</p>
 */
public abstract class Attributes implements MASTransformable {

    protected List<String> mAttributes;

    /**
     * No args constructor.
     */
    public Attributes() {
        mAttributes = new ArrayList<>();
    }

    /**
     * Convenience constructor.
     *
     * @param attributes
     */
    public Attributes(List<String> attributes) {
        mAttributes = attributes;
    }

    @Override
    public abstract void populate(@NonNull JSONObject jobj) throws JSONException;

    @Override
    public JSONObject getAsJSONObject() throws JSONException {
        return null;
    }

    /**
     *
     * <b>Description:</b> This method takes the current attributes, contained in the attributes list, and saves them to the Android
     * shared preferences. This is done as an optimization so that every request for the attributes does not result in a network operation.
     *
     * @param context
     */
    public void save(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getKey(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        StringBuilder sb = new StringBuilder();
        for (String s : mAttributes) {
            sb.append(s);
            sb.append(IdentityConsts.COLON);
        }
        editor.putString(getKey(), sb.toString());
        editor.apply();
    }

    /**
     *
     * <b>Description:</b> Calling this method will clear the shared prefences containing the attributes that had been previously written in a
     * call to 'saveAttributes'. This method can be used to force a network operation on a subsequent to getAttributes.
     *
     * @param context
     */
    public void clear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(getKey(), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        if (mAttributes != null) {
            mAttributes.clear();
        }
    }

    /**
     * <b>Pre-Conditions: </b> None.<br>
     * <b>Description: </b> Return the list of attributes that exist on the SCIM endpoint. If the attributes are currently loaded into
     * the data structure then simply return the data structure. If not, then attempt to read them from the shared preferences. This value
     * could be null, and if it is, then a network operation must be performed to retrieve the attributes required.
     *
     * @return List<String> The list of SCIM endpoints
     */
    public List<String> getAttributes() {
        // just return them if we already have them
        if (mAttributes != null && !mAttributes.isEmpty()) {
            return mAttributes;
        }
        // load them, if they are in the shared preferences
        SharedPreferences prefs = MAS.getContext().getSharedPreferences(getKey(), Context.MODE_PRIVATE);
        String attrs = prefs.getString(getKey(), null);
        if (attrs != null) {
            StringTokenizer st = new StringTokenizer(attrs, IdentityConsts.EMPTY + IdentityConsts.COLON);
            while (st.hasMoreTokens()) {
                String tok = st.nextToken();
                if (!TextUtils.isEmpty(tok)) {
                    mAttributes.add(tok);
                }
            }
        }
        return mAttributes;
    }

    protected abstract String getKey();

}
