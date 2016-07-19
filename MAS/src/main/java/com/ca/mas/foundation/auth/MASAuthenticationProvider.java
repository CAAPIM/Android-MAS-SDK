/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.os.Parcel;
import android.os.Parcelable;

import com.ca.mas.core.service.Provider;

/**
 * The MASAuthenticationProvider class is a representation of a single provider.
 */
public class MASAuthenticationProvider implements Parcelable {

    private Provider provider;

    protected MASAuthenticationProvider(Provider provider) {
        this.provider = provider;
    }

    public String getIdentifier() {
        return provider.getId();
    }

    public String getAuthenticationUrl() {
        return provider.getUrl();
    }

    public String getPollUrl() {
        return provider.getPollUrl();
    }

    public boolean isEnterprise() {
        return provider.getId().equalsIgnoreCase("enterprise");
    }

    public boolean isFacebook() {
        return provider.getId().equalsIgnoreCase("facebook");
    }

    public boolean isGoogle() {
        return provider.getId().equalsIgnoreCase("google");
    }

    public boolean isLinkedIn() {
        return provider.getId().equalsIgnoreCase("linkedin");
    }

    public boolean isSalesForce() {
        return provider.getId().equalsIgnoreCase("salesforce");
    }

    public boolean isProximityLogin() {
        return provider.getId().equalsIgnoreCase("qrcode");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.provider, flags);
    }

    protected MASAuthenticationProvider(Parcel in) {
        this.provider = in.readParcelable(Provider.class.getClassLoader());
    }

    public static final Parcelable.Creator<MASAuthenticationProvider> CREATOR = new Parcelable.Creator<MASAuthenticationProvider>() {
        @Override
        public MASAuthenticationProvider createFromParcel(Parcel source) {
            return new MASAuthenticationProvider(source);
        }

        @Override
        public MASAuthenticationProvider[] newArray(int size) {
            return new MASAuthenticationProvider[size];
        }
    };
}
