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

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.foundation.notify.Callback;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

/**
 * A Handler object to handle One Time Password multi-factor authentication
 */
public class MASOtpAuthenticationHandler extends MASMultiFactorHandler {

    private List<String> channels;
    private boolean isInvalidOtp;
    private String selectedChannels;

    public MASOtpAuthenticationHandler(long requestId, List<String> channels, boolean isInvalidOtp, String selectedChannels) {
        super(requestId);
        this.channels = channels;
        this.isInvalidOtp = isInvalidOtp;
        this.selectedChannels = selectedChannels;
    }

    public void proceed(Context context, String otp) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put(OtpConstants.X_OTP, otp);
        headers.put(OtpConstants.X_OTP_CHANNEL, selectedChannels);
        proceed(context, headers);
    }

    /**
     * Proceed to invoke server to deliver the OTP to the given delivery channel.
     *
     * @param channels the name of the delivery channel
     * @param callback the callback for delivering a success or error
     */
    public void deliver(String channels, final MASCallback<Void> callback) {
        this.selectedChannels = channels;

        //MAPI-1032 : Android SDK : Fix for prefixed server otp protected resource
        String otpAuthUrl = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.AUTHENTICATE_OTP_PATH);
        URI otpDeliveryUrl = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUri(otpAuthUrl);
        MASRequest request = new MASRequest.MASRequestBuilder(otpDeliveryUrl)
                .header(OtpConstants.X_OTP_CHANNEL, channels)
                .build();
        MAS.invoke(request, new MASCallback<MASResponse<Void>>() {

            @Override
            public void onSuccess(MASResponse<Void> result) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });

    }

    public List<String> getChannels() {
        return channels;
    }

    public boolean isInvalidOtp() {
        return isInvalidOtp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeStringList(this.channels);
        dest.writeByte(this.isInvalidOtp ? (byte) 1 : (byte) 0);
        dest.writeString(this.selectedChannels);
    }

    protected MASOtpAuthenticationHandler(Parcel in) {
        super(in);
        this.channels = in.createStringArrayList();
        this.isInvalidOtp = in.readByte() != 0;
        this.selectedChannels = in.readString();
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
