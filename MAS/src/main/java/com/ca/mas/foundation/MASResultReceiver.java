/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.os.Handler;

import com.ca.mas.core.MAGResultReceiver;

/**
 * <p><b>MASResultReceiver</b> extends {@link com.ca.mas.core.MAGResultReceiver} as a callback interface for management requests and
 * responses in the SDK. This abstract class is used to hide the {@link com.ca.mas.core.MAGResultReceiver#onRequestCancelled} callback because
 * it is not used in the MAS sdk.</p>
 */
public abstract class MASResultReceiver<T> extends MAGResultReceiver<T> {

    /**
     * <b>Description:</b> Required constructor.
     *
     * @param handler the Android Handler object used in asynchronous call handling.
     */
    public MASResultReceiver(Handler handler) {
        super(handler);
    }

    /**
     * <b>Description:</b> Required constructor.
     */
    public MASResultReceiver() {
        super(null);
    }

    @Override
    public void onRequestCancelled() {
        // do nothing
    }
}
