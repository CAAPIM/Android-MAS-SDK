/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.util.Log;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;


/**
 * Callback object that is used as a transport for the StorageResult, especially for an
 * Async operation. This is essentially a wrapper around @{link ResultReceiver}
 * <p/>
 */
public class StorageResultReceiver {

    private final ResultReceiver receiver;

    public StorageResultReceiver(Handler handler) {

        receiver = new ResultReceiver(handler) {

            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {

                StorageResult result = resultData.getParcelable("result");
                try {
                    StorageResultReceiver.this.onReceiveResult(result);
                } catch (Exception e) {
                    if (DEBUG) Log.e(TAG, "Error in Storage Callback",  e);
                }
            }
        };
    }

    /**
     * Deliver a result to this receiver. Will send @{link #onReceiveResult} asynchronously if the receiver
     * has supplied a Handler in which to dispatch the result.
     *
     * @param result The @{link StorageResult} to onReceiveResult to the caller.
     */
    public final void send(StorageResult result) {
        Bundle wrapper = new Bundle();
        wrapper.putParcelable("result", result);
        receiver.send(0, wrapper);
    }


    /**
     * Override to receive results delivered to this object
     *
     * @param result The @{link StorageResult} holding the operation result
     */
    public void onReceiveResult(StorageResult result) {
    }

}
