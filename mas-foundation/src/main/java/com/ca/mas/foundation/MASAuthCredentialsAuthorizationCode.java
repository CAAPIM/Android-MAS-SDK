/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.oauth.CodeVerifierCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MASAuthCredentials for Authorization Code Grant Type
 */
public class MASAuthCredentialsAuthorizationCode implements MASAuthCredentials {

    private String code;
    private String state;
    private String codeVerifier;

    public MASAuthCredentialsAuthorizationCode(String code, String state) {
        this.code = code;
        this.state = state;
        if (ConfigurationManager.getInstance().isPKCEEnabled()) {
            if (state != null && !state.isEmpty()) {
                this.codeVerifier = CodeVerifierCache.getInstance().take(state);
            } else {
                this.codeVerifier = CodeVerifierCache.getInstance().take();
            }
        }
    }

    @Override
    public void clear() {
    }

    @Override
    public boolean isValid() {
        return code != null && code.length() > 0;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> headers = new HashMap<>();
        List<String> authorizationValue = new ArrayList<>();
        authorizationValue.add("Bearer " + code);
        headers.put("authorization", authorizationValue);
        String redirectUrl = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
        if (redirectUrl != null) {
            List<String> redirectValue = new ArrayList<>();
            redirectValue.add(redirectUrl);
            headers.put("redirect-uri", redirectValue);
        }
        if (codeVerifier != null) {
            headers.put("code-verifier", Collections.singletonList(codeVerifier));
        }

        return headers;
    }

    @Override
    public List<Pair<String,String>> getParams() {
        ArrayList<Pair<String, String>> params = new ArrayList<>();
        params.add(new Pair<String, String>("code", code));
        if (codeVerifier != null) {
            params.add(new Pair<String, String>("code_verifier", codeVerifier));
        }
        String redirectUrl = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
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
     */
    @Override
    public boolean isReusable() {
        return false;
    }

    @Override
    public boolean canRegisterDevice() {
        return true;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.code);
        dest.writeString(this.state);
        dest.writeString(this.codeVerifier);
    }

    protected MASAuthCredentialsAuthorizationCode(Parcel in) {
        this.code = in.readString();
        this.state = in.readString();
        this.codeVerifier = in.readString();
    }

    public static final Parcelable.Creator<MASAuthCredentialsAuthorizationCode> CREATOR = new Parcelable.Creator<MASAuthCredentialsAuthorizationCode>() {
        @Override
        public MASAuthCredentialsAuthorizationCode createFromParcel(Parcel source) {
            return new MASAuthCredentialsAuthorizationCode(source);
        }

        @Override
        public MASAuthCredentialsAuthorizationCode[] newArray(int size) {
            return new MASAuthCredentialsAuthorizationCode[size];
        }
    };
}
