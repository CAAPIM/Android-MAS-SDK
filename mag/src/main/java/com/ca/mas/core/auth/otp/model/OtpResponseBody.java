/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.otp.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A model object to store the parsed OTP MAG Gateway response data
 */
public class OtpResponseBody implements Parcelable{

    public OtpResponseBody () {
        super();
    }

    private String error;
    private String errorDescription;

    public String getErrorDescription() {

        return errorDescription;
    }

    public void setErrorDescription(String errorDescription) {
        this.errorDescription = errorDescription;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(error);
        dest.writeString(errorDescription);
    }

    protected OtpResponseBody(Parcel in) {
        error = in.readString();
        errorDescription = in.readString();
    }

    public static final Creator<OtpResponseBody> CREATOR = new Creator<OtpResponseBody>() {
        @Override
        public OtpResponseBody createFromParcel(Parcel in) {
            return new OtpResponseBody(in);
        }

        @Override
        public OtpResponseBody[] newArray(int size) {
            return new OtpResponseBody[size];
        }
    };
}
