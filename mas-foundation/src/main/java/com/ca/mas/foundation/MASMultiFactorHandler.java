/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.service.MssoService;
import com.ca.mas.core.service.MssoServiceConnection;
import com.ca.mas.core.service.MssoServiceState;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * A Handler object to handle multi-factor authentication
 */
public class MASMultiFactorHandler implements Parcelable {

    private long requestId;
    private Map<String, String> previousAdditionalHeaders;


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
        final Intent intent = new Intent(context, MssoService.class );
        intent.setAction(MssoIntents.ACTION_PROCESS_REQUEST);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
        if (previousAdditionalHeaders != null) {
            previousAdditionalHeaders.putAll(additionalHeaders);
        } else {
            previousAdditionalHeaders = additionalHeaders;
        }
        intent.putExtra(MssoIntents.EXTRA_ADDITIONAL_HEADERS, (Serializable) previousAdditionalHeaders);


        if(MssoServiceState.getInstance().isBound()){
            MssoServiceState.getInstance().getMssoService().handleWork(intent);
        } else {
            ServiceConnection conn = new MssoServiceConnection(intent);
            MssoServiceState.getInstance().setServiceConnection(conn);
            context.bindService(intent, conn, Context.BIND_AUTO_CREATE);
        }
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


    void setPreviousAdditionalHeaders(Map<String, String> previousAdditionalHeaders) {
        this.previousAdditionalHeaders = previousAdditionalHeaders;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.requestId);
        if (previousAdditionalHeaders == null) {
            dest.writeInt(0);
        } else {
            dest.writeInt(this.previousAdditionalHeaders.size());
            for (Map.Entry<String, String> entry : this.previousAdditionalHeaders.entrySet()) {
                dest.writeString(entry.getKey());
                dest.writeString(entry.getValue());
            }
        }
    }

    protected MASMultiFactorHandler(Parcel in) {
        this.requestId = in.readLong();
        int previousAdditionalHeadersSize = in.readInt();
        if (previousAdditionalHeadersSize > 0) {
            this.previousAdditionalHeaders = new HashMap<>(previousAdditionalHeadersSize);
            for (int i = 0; i < previousAdditionalHeadersSize; i++) {
                String key = in.readString();
                String value = in.readString();
                this.previousAdditionalHeaders.put(key, value);
            }
        }
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
