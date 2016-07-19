/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.notify;

import android.os.Handler;
import android.os.Looper;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASResponse;

public class Callback {

    public static <T> void onSuccess(final MASCallback<T> callback, final T value) {
        if (callback != null) {
            if (callback.getHandler() != null) {
                Looper looper = callback.getHandler().getLooper();
                if (looper != null && Thread.currentThread() == callback.getHandler().getLooper().getThread()) {
                    callback.onSuccess(value);
                } else {
                    callback.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onSuccess(value);
                        }
                    });
                }
            } else {
                callback.onSuccess(value);
            }
        }
    }

    public static void onError(final MASCallback callback, final Throwable t) {

        if (callback != null) {
            if (callback.getHandler() != null) {
                Looper looper = callback.getHandler().getLooper();
                if (looper != null && Thread.currentThread() == callback.getHandler().getLooper().getThread()) {
                    callback.onError(t);
                } else {
                    callback.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            callback.onError(t);
                        }
                    });
                }
            } else {
                callback.onError(t);
            }
        }
    }

    public static Handler getHandler(MASCallback callback) {
        if (callback != null) {
            return callback.getHandler();
        }
        return null;
    }



}
