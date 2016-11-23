/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.auth.otp.OtpAuthenticationHandler;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.foundation.notify.Callback;

import java.util.List;

/**
 * Handler class for the OTP flow.
 * Wrapper around the OtpAuthenticationHandler class
 */
public class MASOtpAuthenticationHandler implements Parcelable{

    private OtpAuthenticationHandler handler;

    public MASOtpAuthenticationHandler(OtpAuthenticationHandler handler) {
        this.handler = handler;
    }


    /**
     * Proceed to invoke server to validate the OTP.
     * Wrapper for the OtpAuthenticationHandler.proceed method
     */
    public void proceed(Context context, String otp) {
        handler.proceed(context, otp);
    }

    /**
     * Proceed to invoke server to deliver the OTP to the given delivery channel.
     * Wrapper for the OtpAuthenticationHandler.deliver method
     */
    public void deliver(String channel, final MASCallback<Void> callback) {
        handler.deliver(channel, new MAGResultReceiver<Void>() {
            @Override
            public void onSuccess(MAGResponse<Void> response) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(MAGError error) {
                Callback.onError(callback, error);
            }

            @Override
            public void onRequestCancelled() {

            }
        });
    }

    public List<String> getChannels() {
        return handler.getChannels();
    }
    public boolean isInvalidOtp() {
        return handler.isInvalidOtp();
    }

    /**
     * Cancel the fetch otp protected data request.
     * Wrapper for the OtpAuthenticationHandler.cancel method
     */
    public void cancel() {
        handler.cancel();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.handler, flags);
    }

    protected MASOtpAuthenticationHandler(Parcel in) {
        this.handler = in.readParcelable(OtpAuthenticationHandler.class.getClassLoader());
    }

    public static final Creator<MASOtpAuthenticationHandler> CREATOR = new Creator<MASOtpAuthenticationHandler>() {
        @Override
        public MASOtpAuthenticationHandler createFromParcel(Parcel source) {
            return new MASOtpAuthenticationHandler(source);
        }

        @Override
        public MASOtpAuthenticationHandler[] newArray(int size) {
            return new MASOtpAuthenticationHandler[size];
        }
    };
}
