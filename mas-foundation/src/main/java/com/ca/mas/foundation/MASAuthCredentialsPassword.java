/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.os.Parcel;
import android.util.Log;
import android.util.Pair;

import com.ca.mas.core.io.IoUtils;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ca.mas.foundation.MAS.TAG;

/**
 * MASAuthCredentials for Username &amp; Password Grant Type
 */
public class MASAuthCredentialsPassword implements MASAuthCredentials {
    private final String username;
    private volatile char[] password;

    /**
     * Create a UsernamePassword holding the specified username and password.
     *
     * @param username the username.  Required.
     * @param password the password.
     */
    public MASAuthCredentialsPassword(String username, char[] password) {
        if (username == null || username.trim().length() == 0) {
            throw new IllegalArgumentException("Empty Username.");
        }

        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Empty Password.");
        }

        this.username = username;
        this.password = password;
    }

    private MASAuthCredentialsPassword(Parcel in) {
        this.username = in.readString();
        this.password = new char[in.readInt()];
        in.readCharArray(this.password);
    }

    /**
     * Clear the password from memory.  Call this when the password is no longer needed.
     */
    private void clearPassword() {
        Log.d(TAG,"Escalation MASAuthCredentialsPassword clearPassword");
        char[] p = password;
        this.password = null;
        if (p != null)
            Arrays.fill(p, 'X');
    }

    @Override
    public void clear() {
        Log.d(TAG,"Escalation MASAuthCredentialsPassword clear");
        clearPassword();
    }

    @Override
    public boolean isValid() {
        return !(username == null || password == null);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> headers = new HashMap<>();
        List<String> authorizationValue = new ArrayList<>();
        authorizationValue.add("Basic " + IoUtils.base64(username + ":" + new String(password), Charset.defaultCharset()));
        headers.put("authorization", authorizationValue);
        return headers;
    }

    @Override
    public List<Pair<String, String>> getParams() {
        ArrayList<Pair<String, String>> params = new ArrayList<>();
        params.add(new Pair<>("username", username));
        params.add(new Pair<>("password", new String(password)));
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
    public boolean isReusable() {
        return true;
    }

    @Override
    public boolean canRegisterDevice() {
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
        dest.writeInt(password.length);
        dest.writeCharArray(password);
    }

    /**
     * Factory for creating instances of the Parcelable class.
     */
    public static final Creator<MASAuthCredentialsPassword> CREATOR = new Creator<MASAuthCredentialsPassword>() {

        /**
         * This method will be called to instantiate a MyParcelableMessage
         * when a Parcel is received.
         * All data fields which where written during the writeToParcel
         * method should be read in the correct sequence during this method.
         */
        @Override
        public MASAuthCredentialsPassword createFromParcel(Parcel in) {
            return new MASAuthCredentialsPassword(in);
        }

        /**
         * Creates an array of our Parcelable object.
         */
        @Override
        public MASAuthCredentialsPassword[] newArray(int size) {
            return new MASAuthCredentialsPassword[size];
        }
    };
}
