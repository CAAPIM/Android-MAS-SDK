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

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.io.IoUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Credentials for Username & Password Grant Type
 */
public class PasswordCredentials implements Credentials {
    private final String username;
    private volatile char[] password;

    /**
     * Create a UsernamePassword holding the specified username and password.
     *
     * @param username the username.  Required.
     * @param password the password.
     */
    public PasswordCredentials(String username, char[] password) {
        this.username = username;
        this.password = password;
    }

    /**
     * Clear the password from memory.  Call this when the password is no longer needed.
     */
    private void clearPassword() {
        char[] p = password;
        this.password = null;
        if (p != null)
            Arrays.fill(p, 'X');
    }

    @Override
    public void clear() {
        clearPassword();
    }

    @Override
    public boolean isValid() {
        return !(username == null || password == null);
    }

    @Override
    public Map<String, List<String>> getHeaders(MssoContext context) {
        Map<String, List<String>> headers = new HashMap<>();
        List<String> authorizationValue = new ArrayList<>();
        authorizationValue.add("Basic " + IoUtils.base64(username + ":" + new String(password), Charset.defaultCharset()));
        headers.put("authorization", authorizationValue);
        return headers;
    }

    @Override
    public List<Pair<String, String>> getParams(MssoContext config) {
        ArrayList<Pair<String, String>> params = new ArrayList<>();
        params.add(new Pair<String, String>("username", username));
        params.add(new Pair<String, String>("password", new String(password)));
        return params;
    }

    @Override
    public String getGrantType() {
        return "password";
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isReuseable() {
        return true;
    }

    @Override
    public String toString() {
        return username;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(username);
        dest.writeString(new String(password));
    }

    /**
     * Factory for creating instances of the Parcelable class.
     */
    public static final Creator<PasswordCredentials> CREATOR = new Creator<PasswordCredentials>() {

        /**
         * This method will be called to instantiate a MyParcelableMessage
         * when a Parcel is received.
         * All data fields which where written during the writeToParcel
         * method should be read in the correct sequence during this method.
         */
        @Override
        public PasswordCredentials createFromParcel(Parcel in) {
            String username = in.readString();
            String password = in.readString();
            return new PasswordCredentials(username, password.toCharArray());
        }

        /**
         * Creates an array of our Parcelable object.
         */
        @Override
        public PasswordCredentials[] newArray(int size) {
            return new PasswordCredentials[size];
        }
    };
}
