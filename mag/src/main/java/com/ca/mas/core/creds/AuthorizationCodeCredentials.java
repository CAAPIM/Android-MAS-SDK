/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.creds;

import android.os.Parcel;
import android.util.Pair;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.context.MssoContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Credentials for Authorization Code Grant Type
 */
public class AuthorizationCodeCredentials implements Credentials {

    private String code;

    public AuthorizationCodeCredentials(String code) {
        this.code = code;
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isValid() {
        if (code == null || code.length() < 1) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    public Map<String, List<String>> getHeaders(MssoContext context) {
        Map<String, List<String>> headers = new HashMap<>();
        List<String> authorizationValue = new ArrayList<>();
        authorizationValue.add("Bearer " + code);
        headers.put("authorization", authorizationValue);
        String redirectUrl = context.getConfigurationProvider().getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
        if (redirectUrl != null) {
            List<String> redirectValue = new ArrayList<>();
            redirectValue.add(redirectUrl);
            headers.put("redirect-uri", redirectValue);
        }
        return headers;
    }

    @Override
    public List<Pair<String,String>> getParams(MssoContext context) {
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("code", code));
        String redirectUrl = context.getConfigurationProvider().getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
        if (redirectUrl != null) {
            params.add(new Pair<String, String>("redirect_uri", redirectUrl));
        }
        return params;
    }

    @Override
    public String getGrantType() {
        return "authorization_code";
    }

    @Override
    public String getUsername() {
        return "socialLogin";
    }

    /**
     * The authorization code can only be use for one time only, it considers invalid after.
     * @return
     */
    @Override
    public boolean isReuseable() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(code);
    }

    /**
     * Factory for creating instances of the Parcelable class.
     */
    public static final Creator<AuthorizationCodeCredentials> CREATOR = new Creator<AuthorizationCodeCredentials>() {

        /**
         * This method will be called to instantiate a MyParcelableMessage
         * when a Parcel is received.
         * All data fields which where written during the writeToParcel
         * method should be read in the correct sequence during this method.
         */
        @Override
        public AuthorizationCodeCredentials createFromParcel(Parcel in) {
            String code = in.readString();
            return new AuthorizationCodeCredentials(code);
        }

        /**
         * Creates an array of our Parcelable object.
         */
        @Override
        public AuthorizationCodeCredentials[] newArray(int size) {
            return new AuthorizationCodeCredentials[size];
        }
    };
}
