/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.os.Handler;
import android.os.Looper;

/**
 * API Callback interface for {@link MAS#invoke(MASRequest, MASCallback)}, this simplify the {@link MASCallback} interface,
 * and the callback {@link #onResult(Object, Throwable)} will be run on main thread.
 * <p>
 * {@code
 * MAS.invoke(request, (MASApiCallback<JSONObject>) (result, e) -> {});
 * }
 *
 * @param <T> The object type that will be received when {@link #onSuccess(Object)} is invoked.
 */
public interface MASApiCallback<T> extends MASCallback<MASResponse<T>> {

    /**
     * Run response on Main Thread
     *
     * @return Handler on Main Thread.
     */
    @Override
    default Handler getHandler() {
        return new Handler(Looper.getMainLooper());
    }

    @Override
    default void onSuccess(MASResponse<T> result) {
        if (result.getBody() != null) {
            onResult(result.getBody().getContent(), null);
        } else {
            onResult(null, null);
        }
    }

    @Override
    default void onError(Throwable e) {
        onResult(null, e);
    }

    /**
     * Called when an asynchronous call completes successfully or failed.
     *
     * @param result The result of the API call
     * @param e      Exception if there is any error during the API call, or null if no error.
     */
    void onResult(T result, Throwable e);
}
