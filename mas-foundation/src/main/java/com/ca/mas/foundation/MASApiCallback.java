/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

/**
 * API Callback interface for {@link MAS#invoke(MASRequest, MASCallback)}, this simplify the {@link MASCallback} interface,
 * and the callback {@link #onResponse(Object, Throwable)} will be run on main thread.
 * To retrieve the response header, status, and other attribute, please refer to {@link MASCallbackFunction}
 *
 * <p>
 * {@code
 * MAS.invoke(request, (MASApiCallback<JSONObject>) (result, e) -> {});
 * }
 *
 * @param <T> The object type that will be received when {@link #onSuccess(Object)} is invoked.
 */
public interface MASApiCallback<T> extends MASCallbackFunction<MASResponse<T>> {

    @Override
    default void onResult(MASResponse<T> result, Throwable e) {
        if (result.getBody() != null) {
            onResponse(result.getBody().getContent(), null);
        } else {
            onResponse(null, null);
        }
    }

    void onResponse(T result, Throwable e);
}
