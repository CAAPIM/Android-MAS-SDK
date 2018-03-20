/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.os.Handler;
import android.os.Looper;

import java.util.stream.Stream;

/**
 * Generic callback interface, the callback will be run on main thread.
 *
 * @param <T> The object type that will be received when {@link #onSuccess(Object)} is invoked.
 */
public interface MASCallbackOnMainThread<T> extends MASCallback<T> {

    /**
     * Run response on Main Thread
     *
     * @return Handler on Main Thread.
     */
    @Override
    default Handler getHandler() {
        return new Handler(Looper.getMainLooper());
    }

}

