/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

import android.os.Parcel;
import android.os.Parcelable;

public class IdToken implements Parcelable {
    public static final String JWT_DEFAULT = "urn:ietf:params:oauth:grant-type:jwt-bearer";

    private String value;
    private String type;

    public IdToken(String value, String type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type == null ? JWT_DEFAULT : type;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.value);
        dest.writeString(this.type);
    }

    protected IdToken(Parcel in) {
        this.value = in.readString();
        this.type = in.readString();
    }

    public static final Creator<IdToken> CREATOR = new Creator<IdToken>() {
        @Override
        public IdToken createFromParcel(Parcel source) {
            return new IdToken(source);
        }

        @Override
        public IdToken[] newArray(int size) {
            return new IdToken[size];
        }
    };
}
