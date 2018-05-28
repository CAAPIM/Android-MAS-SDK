/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.content.AsyncTaskLoader;
import android.os.Parcel;
import android.os.Parcelable;

import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.service.Provider;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;

import java.util.ArrayList;
import java.util.List;

/**
 * The MASAuthenticationProviders class is a representation of all available {@link MASAuthenticationProvider}
 */
public class MASAuthenticationProviders implements Parcelable {

    private List<MASAuthenticationProvider> providers;
    private String idp;

    public MASAuthenticationProviders(AuthenticationProvider provider) {
        providers = new ArrayList<>();
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

    /**
     *
     * Send an authorization request to MAG and retrieves the MASAuthenticationProviders from the server.
     * <em>Important</em> Authentication providers will not be retrieved if the user is already authenticated.
     *
     * @param callback Notify caller for the result.
     */
    public static void getAuthenticationProviders(final MASCallback<MASAuthenticationProviders> callback) {
        if (MASUser.getCurrentUser() == null || !MASUser.getCurrentUser().isAuthenticated()) {
            new AsyncTaskLoader<Object>(MAS.getContext()) {
                @Override
                protected void onStartLoading() {
                    super.onStartLoading();
                    forceLoad();
                }

                @Override
                public Object loadInBackground() {
                    try {
                        AuthenticationProvider ap = MobileSsoFactory.getInstance().getAuthenticationProvider();
                        Callback.onSuccess(callback, new MASAuthenticationProviders(ap));
                    } catch (Exception e) {
                        Callback.onError(callback, e);
                    }
                    return null;
                }
            }.startLoading();
        } else {
            Callback.onSuccess(callback, null);
        }
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

    public static final Creator<MASAuthenticationProviders> CREATOR = new Creator<MASAuthenticationProviders>() {
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
