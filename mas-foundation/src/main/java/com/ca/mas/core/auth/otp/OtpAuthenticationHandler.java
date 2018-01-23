/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.otp;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.service.MssoService;
import com.ca.mas.foundation.MASRequest;

import java.net.URI;
import java.util.List;

/**
 * Handler class for the OTP flow.
 */
public class OtpAuthenticationHandler implements Parcelable {
    private long requestId;
    private List<String> channels;
    private boolean isInvalidOtp;
    private String selectedChannels;

    public OtpAuthenticationHandler(long requestId, List<String> channels, boolean isInvalidOtp, String selectedChannels) {

        this.requestId = requestId;
        this.channels = channels;
        this.isInvalidOtp = isInvalidOtp;
        this.selectedChannels = selectedChannels;

    }

    /**
     * Proceed to invoke server to validate the OTP.
     *
     * @param context Application context
     * @param otp     the OTP to be validated
     */
    public void proceed(Context context, String otp) {
        Intent intent = new Intent(MssoIntents.ACTION_VALIDATE_OTP, null, context, MssoService.class);

        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
        intent.putExtra(MssoIntents.EXTRA_OTP_VALUE, otp);
        intent.putExtra(MssoIntents.EXTRA_OTP_SELECTED_CHANNELS, selectedChannels);
        context.startService(intent);
    }

    /**
     * Proceed to invoke server to deliver the OTP to the given delivery channel.
     *
     * @param channels  the name of the delivery channel
     * @param callback the callback for delivering a success or error
     */
    public void deliver(String channels, MAGResultReceiver<Void> callback) {
        this.selectedChannels = channels;
        MobileSso mobileSso = MobileSsoFactory.getInstance();

        //MAPI-1032 : Android SDK : Fix for prefixed server otp protected resource
        String otpAuthUrl = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.AUTHENTICATE_OTP_PATH);
        URI otpDeliveryUrl = mobileSso.getURI(otpAuthUrl);
        MASRequest request = new MASRequest.MASRequestBuilder(otpDeliveryUrl)
                .header(OtpConstants.X_OTP_CHANNEL, channels)
                .build();
        mobileSso.processRequest(request, callback);

    }

    public List<String> getChannels() {
        return channels;
    }

    public boolean isInvalidOtp() {
        return isInvalidOtp;
    }

    /**
     * Cancel the fetch otp protected data request.
     */
    public void cancel() {
        MobileSsoFactory.getInstance().cancelRequest(requestId, null);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.requestId);
        dest.writeStringList(this.channels);
        dest.writeByte(this.isInvalidOtp ? (byte) 1 : (byte) 0);
        dest.writeString(this.selectedChannels);
    }

    protected OtpAuthenticationHandler(Parcel in) {
        this.requestId = in.readLong();
        this.channels = in.createStringArrayList();
        this.isInvalidOtp = in.readByte() != 0;
        this.selectedChannels = in.readString();
    }

    public static final Creator<OtpAuthenticationHandler> CREATOR = new Creator<OtpAuthenticationHandler>() {
        @Override
        public OtpAuthenticationHandler createFromParcel(Parcel source) {
            return new OtpAuthenticationHandler(source);
        }

        @Override
        public OtpAuthenticationHandler[] newArray(int size) {
            return new OtpAuthenticationHandler[size];
        }
    };
}
