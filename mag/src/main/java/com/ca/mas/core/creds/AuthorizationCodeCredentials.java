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
import com.ca.mas.core.oauth.CodeVerifierCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Credentials for Authorization Code Grant Type
 */
public class AuthorizationCodeCredentials implements Credentials {

    private String code;
    private String state;
    private String codeVerifier;

    public AuthorizationCodeCredentials(String code, String state) {
        this.code = code;
        this.state = state;
        this.codeVerifier = CodeVerifierCache.getInstance().take(state);
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
        if (codeVerifier != null) {
            headers.put("code-verifier", Collections.singletonList(codeVerifier));
        }

        return headers;
    }

    @Override
    public List<Pair<String,String>> getParams(MssoContext context) {
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>("code", code));
        if (codeVerifier != null) {
            params.add(new Pair<String, String>("code_verifier", codeVerifier));
        }
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
        dest.writeString(this.code);
        dest.writeString(this.state);
        dest.writeString(this.codeVerifier);
    }

    protected AuthorizationCodeCredentials(Parcel in) {
        this.code = in.readString();
        this.state = in.readString();
        this.codeVerifier = in.readString();
    }

    public static final Creator<AuthorizationCodeCredentials> CREATOR = new Creator<AuthorizationCodeCredentials>() {
        @Override
        public AuthorizationCodeCredentials createFromParcel(Parcel source) {
            return new AuthorizationCodeCredentials(source);
        }

        @Override
        public AuthorizationCodeCredentials[] newArray(int size) {
            return new AuthorizationCodeCredentials[size];
        }
    };
}
