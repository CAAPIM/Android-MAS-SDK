/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.app;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

public class App implements Parcelable {

    private String id;
    private String name;
    private String iconUrl;
    private String authUrl;
    private String nativeUri;
    private String custom;

    public App(JSONObject app) {
        try {
            this.id = app.getString("id");
            this.name = app.optString("name");
            this.iconUrl = app.optString("icon_url");
            this.authUrl = app.optString("auth_url");
            this.nativeUri = app.optString("native_url");
            this.custom = app.optString("custom");
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid Enterprise App Configure.", e);
        }
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getAuthUrl() {
        return authUrl;
    }

    public String getNativeUri() {
        return nativeUri;
    }

    public JSONObject getCustom() {
        if (custom != null) {
            try {
                return new JSONObject(custom);
            } catch (JSONException e) {
                if (DEBUG) Log.e(TAG, e.getMessage(), e);
            }
        }
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.name);
        dest.writeString(this.iconUrl);
        dest.writeString(this.authUrl);
        dest.writeString(this.nativeUri);
        dest.writeString(this.custom);
    }

    protected App(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.iconUrl = in.readString();
        this.authUrl = in.readString();
        this.nativeUri = in.readString();
        this.custom = in.readString();
    }

    public static final Parcelable.Creator<App> CREATOR = new Parcelable.Creator<App>() {
        @Override
        public App createFromParcel(Parcel source) {
            return new App(source);
        }

        @Override
        public App[] newArray(int size) {
            return new App[size];
        }
    };
}
