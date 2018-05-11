/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.service.MssoService;

import java.io.Serializable;
import java.util.Map;

/**
 * A Handler object to handle multi-factor authentication
 */
public class MASMultiFactorHandler implements Parcelable {

    protected long requestId;

    /**
     * Create a MASMultiFactorHandler with the request ID.
     *
     * @param requestId The request ID of the pending request
     */
    public MASMultiFactorHandler(long requestId) {
        this.requestId = requestId;
    }

    /**
     * Proceeds with additional headers that can be injected to the original request.
     *
     * @param context           The current Activity Context
     * @param additionalHeaders Additional headers that will inject to the original pending request.
     */
    public void proceed(Context context, Map<String, String> additionalHeaders) {
        Intent intent = new Intent(MssoIntents.ACTION_PROCESS_REQUEST, null, context, MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
        intent.putExtra(MssoIntents.EXTRA_ADDITIONAL_HEADERS, (Serializable) additionalHeaders);
        context.startService(intent);
    }

    /**
     * Cancel the multi-factor authentication, the pending request from the queue will be removed.
     * See {@link MAS#cancelRequest(long)} for detail
     */
    public void cancel() {
        MAS.cancelRequest(requestId);
    }

    /**
     * Cancels the original request with additional information defined in multi factor authentication process
     * See {@link MAS#cancelRequest(long, Bundle)} for detail
     *
     * @param data the additional information to the request.
     */
    public void cancel(Bundle data) {
        MAS.cancelRequest(requestId, data);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.requestId);
    }

    protected MASMultiFactorHandler(Parcel in) {
        this.requestId = in.readLong();
    }

    public static final Creator<MASMultiFactorHandler> CREATOR = new Creator<MASMultiFactorHandler>() {
        @Override
        public MASMultiFactorHandler createFromParcel(Parcel source) {
            return new MASMultiFactorHandler(source);
        }

        @Override
        public MASMultiFactorHandler[] newArray(int size) {
            return new MASMultiFactorHandler[size];
        }
    };
}
