/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.creds;

import android.os.Parcel;
import android.util.Pair;

import com.ca.mas.core.client.ServerClient;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.token.IdToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JWTCredentials implements Credentials {

    private volatile IdToken idToken;

    public JWTCredentials(IdToken idToken) {
        this.idToken = idToken;
    }

    @Override
    public void clear() {
        this.idToken = null;
    }

    @Override
    public boolean isValid() {
        return idToken != null;
    }

    @Override
    public Map<String, List<String>> getHeaders(MssoContext context) {
        Map<String, List<String>> headers = new HashMap<>();
        headers.put("authorization", Collections.singletonList("Bearer " + idToken.getValue()));
        headers.put("x-authorization-type", Collections.singletonList(idToken.getType()));
        return headers;
    }

    @Override
    public List<Pair<String, String>> getParams(MssoContext context) {
        ArrayList<Pair<String, String>> params = new ArrayList<Pair<String, String>>();
        params.add(new Pair<String, String>(ServerClient.ASSERTION, idToken.getValue()));
        return params;
    }

    @Override
    public String getGrantType() {
        return idToken.getType();
    }

    @Override
    public String getUsername() {
        return "socialLogin";
    }

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
        dest.writeParcelable(this.idToken, flags);
    }

    protected JWTCredentials(Parcel in) {
        this.idToken = in.readParcelable(IdToken.class.getClassLoader());
    }

    public static final Creator<JWTCredentials> CREATOR = new Creator<JWTCredentials>() {
        @Override
        public JWTCredentials createFromParcel(Parcel source) {
            return new JWTCredentials(source);
        }

        @Override
        public JWTCredentials[] newArray(int size) {
            return new JWTCredentials[size];
        }
    };
}
