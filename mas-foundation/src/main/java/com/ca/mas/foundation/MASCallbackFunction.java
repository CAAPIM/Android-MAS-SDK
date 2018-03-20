/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

/**
 * Generic callback interface.
 * The callback {@link #onResult(Object, Throwable)} will be run on main thread.
 * <p>
 * {@code
 * MAS.invoke(request, (MASCallbackFunction<MASResponse<JSONObject>>) (result, e) -> {});
 * }
 *
 * @param <T> The object type that will be received when {@link #onSuccess(Object)} is invoked.
 */
public interface MASCallbackFunction<T> extends MASCallbackOnMainThread<T> {

    @Override
    default void onSuccess(T result) {
        onResult(result, null);
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
