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

import java.util.List;

public class AuthenticationProvider implements Parcelable {


    public static final String ALL = "all";
    public static final String ENTERPRISE = "enterprise";

    String idp;
    List<Provider> providers;

    public AuthenticationProvider(String idp, List<Provider> providers) {
        this.idp = idp;
        this.providers = providers;
    }

    public boolean isEnterpriseEnabled() {
        return ENTERPRISE.equals(idp) || ALL.equals(idp);
    }

    public String getIdp() {
        return idp;
    }

    public List<Provider> getProviders() {
        return providers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.idp);
        dest.writeTypedList(this.providers);
    }

    protected AuthenticationProvider(Parcel in) {
        this.idp = in.readString();
        this.providers = in.createTypedArrayList(Provider.CREATOR);
    }

    public static final Parcelable.Creator<AuthenticationProvider> CREATOR = new Parcelable.Creator<AuthenticationProvider>() {
        @Override
        public AuthenticationProvider createFromParcel(Parcel source) {
            return new AuthenticationProvider(source);
        }

        @Override
        public AuthenticationProvider[] newArray(int size) {
            return new AuthenticationProvider[size];
        }
    };
}
