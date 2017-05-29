/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import android.os.Parcel;
import android.os.Parcelable;

public class Provider implements Parcelable {

    private String id;
    private String url;
    private String pollUrl;
    private Integer iconId;

    public Provider(String id, String url, String pollUrl,  Integer iconId) {
        this.id = id;
        this.url = url;
        this.pollUrl = pollUrl;
        this.iconId = iconId;
    }

    public String getId() {
        return id;
    }

    public String getPollUrl() {
        return pollUrl;
    }

    public String getUrl() {
        return url;
    }

    public Integer getIconId() {
        return iconId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.url);
        dest.writeString(this.pollUrl);
        dest.writeValue(this.iconId);
    }

    protected Provider(Parcel in) {
        this.id = in.readString();
        this.url = in.readString();
        this.pollUrl = in.readString();
        this.iconId = (Integer) in.readValue(Integer.class.getClassLoader());
    }

    public static final Creator<Provider> CREATOR = new Creator<Provider>() {
        @Override
        public Provider createFromParcel(Parcel source) {
            return new Provider(source);
        }

        @Override
        public Provider[] newArray(int size) {
            return new Provider[size];
        }
    };
}
