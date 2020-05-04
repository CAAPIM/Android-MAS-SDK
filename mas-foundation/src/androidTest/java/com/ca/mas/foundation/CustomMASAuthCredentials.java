/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.os.Parcel;
import android.util.Log;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.ca.mas.foundation.MAS.TAG;

public class CustomMASAuthCredentials extends MASAuthCredentialsPassword {

    private String panCard;

    public CustomMASAuthCredentials(String username, char[] password, String panCard) {
        super(username, password);
        this.panCard = panCard;
    }

    private static char[] readPassword(Parcel in) {
        char[] password = new char[in.readInt()];
        in.readCharArray(password);
        return password;
    }

    private CustomMASAuthCredentials(Parcel in) {
        super(in.readString(), readPassword(in));
        panCard = in.readString();
    }

    @Override
    public void clear() {
        Log.d(TAG,"Escalation CustomMASAuthCredentials clear");
        super.clear();
    }

    @Override
    public boolean isValid() {
        return (super.isValid() && panCard != null);
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> headers = super.getHeaders();
        List<String> cards = new ArrayList<>();
        cards.add(panCard);
        headers.put("panCard", cards);
        return headers;
    }

    @Override
    public List<Pair<String, String>> getParams() {
        List<Pair<String, String>> pairs = super.getParams();
        pairs.add(new Pair<String, String>("panCard", panCard));
        return pairs;
    }

    @Override
    public String getGrantType() {
        return super.getGrantType();
    }

    @Override
    public boolean canRegisterDevice() {
        return super.canRegisterDevice();
    }

    @Override
    public String getUsername() {
        return super.getUsername();
    }

    @Override
    public boolean isReusable() {
        return super.isReusable();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(panCard);
    }

    /**
     * Factory for creating instances of the Parcelable class.
     */
    public static final Creator<CustomMASAuthCredentials> CREATOR = new Creator<CustomMASAuthCredentials>() {

        /**
         * This method will be called to instantiate a MyParcelableMessage
         * when a Parcel is received.
         * All data fields which where written during the writeToParcel
         * method should be read in the correct sequence during this method.
         */
        @Override
        public CustomMASAuthCredentials createFromParcel(Parcel in) {
            return new CustomMASAuthCredentials(in);
        }

        /**
         * Creates an array of our Parcelable object.
         */
        @Override
        public CustomMASAuthCredentials[] newArray(int size) {
            return new CustomMASAuthCredentials[size];
        }
    };
}
