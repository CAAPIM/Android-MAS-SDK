/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.os.Handler;

public abstract class MASCallback<T> {

    /**
     * The Handler to handle the callback, refer to {@link Handler} for details
     */
    public Handler getHandler() {
        return null;
    }

    /**
     * Called when an asynchronous call completes successfully.
     * @param result the value returned
     */
    public abstract void onSuccess(T result);

    /**
     * Called when an asynchronous call fails to complete.
     * @param e the reason for failure
     */
    public abstract void onError(Throwable e);

}
