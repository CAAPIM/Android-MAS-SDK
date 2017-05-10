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

import java.util.List;

/**
 * A model object to store the parsed OTP MAG Gateway response headers data
 */
public class OtpResponseHeaders implements Parcelable{

    public OtpResponseHeaders () {
        super();
    }


    public enum X_OTP_VALUE {
        REQUIRED, GENERATED, INVALID, EXPIRED, SUSPENDED, UNKNOWN, xOtpValue;
    }
    public enum X_CA_ERROR {
        REQUIRED, GENERATED, OTP_INVALID, OTP_MAX_RETRY_EXCEEDED, EXPIRED, SUSPENDED, UNKNOWN, INVALID_USER_INPUT, INTERNAL_SERVER_ERROR;
    }

    private X_OTP_VALUE xOtpValue;
    private List<String> channels;
    private int retry;
    private int retryInterval;
    private X_CA_ERROR errorCode;
    private int httpStatusCode;

    public X_OTP_VALUE getxOtpValue() {
        return xOtpValue;
    }

    public void setxOtpValue(X_OTP_VALUE xOtpValue) {
        this.xOtpValue = xOtpValue;
    }

    public int getRetry() {
        return retry;
    }

    public void setRetry(int retry) {
        this.retry = retry;
    }

    public X_CA_ERROR getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(X_CA_ERROR errorCode) {
        this.errorCode = errorCode;
    }

    public int getRetryInterval() {
        return retryInterval;
    }

    public void setRetryInterval(int retryInterval) {
        this.retryInterval = retryInterval;
    }

    public void setChannels(List<String> channels) {
        this.channels = channels;
    }

    public List<String> getChannels() {
        return channels;
    }

    protected OtpResponseHeaders(Parcel in) {
        channels = in.createStringArrayList();
        retry = in.readInt();
        retryInterval = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(channels);
        dest.writeInt(retry);
        dest.writeInt(retryInterval);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<OtpResponseHeaders> CREATOR = new Creator<OtpResponseHeaders>() {
        @Override
        public OtpResponseHeaders createFromParcel(Parcel in) {
            return new OtpResponseHeaders(in);
        }

        @Override
        public OtpResponseHeaders[] newArray(int size) {
            return new OtpResponseHeaders[size];
        }
    };

    public int getHttpStatusCode() {
        return httpStatusCode;
    }

    public void setHttpStatusCode(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }
}
