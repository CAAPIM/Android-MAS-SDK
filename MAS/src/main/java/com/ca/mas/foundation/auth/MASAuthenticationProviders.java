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

import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.service.Provider;

import java.util.ArrayList;
import java.util.List;

/**
 * The MASAuthenticationProviders class is a representation of all available {@link MASAuthenticationProvider}
 */
public class MASAuthenticationProviders implements Parcelable {

    private List<MASAuthenticationProvider> providers;
    private String idp;

    public MASAuthenticationProviders(AuthenticationProvider provider) {
        providers = new ArrayList<MASAuthenticationProvider>();
        if (provider != null) {
            List<Provider> ps = provider.getProviders();
            if (ps != null && !ps.isEmpty()) {
                for (final Provider p : ps) {
                    providers.add(new MASAuthenticationProvider(p));
                }
            }
        }
        idp = provider == null ? null : provider.getIdp();

    }

    public List<MASAuthenticationProvider> getProviders() {
        return providers;
    }

    public String getIdp() {
        return idp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.providers);
        dest.writeString(this.idp);
    }

    protected MASAuthenticationProviders(Parcel in) {
        this.providers = in.createTypedArrayList(MASAuthenticationProvider.CREATOR);
        this.idp = in.readString();
    }

    public static final Parcelable.Creator<MASAuthenticationProviders> CREATOR = new Parcelable.Creator<MASAuthenticationProviders>() {
        @Override
        public MASAuthenticationProviders createFromParcel(Parcel source) {
            return new MASAuthenticationProviders(source);
        }

        @Override
        public MASAuthenticationProviders[] newArray(int size) {
            return new MASAuthenticationProviders[size];
        }
    };
}
