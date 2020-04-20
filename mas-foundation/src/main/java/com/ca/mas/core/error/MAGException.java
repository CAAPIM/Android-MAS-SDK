/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.error;

import android.util.Log;

import static com.ca.mas.foundation.MAS.TAG;

/**
 * The Base Exception class for MAG
 */
public class MAGException extends Exception{

    private int errorCode;

    public MAGException(int errorCode) {
        this.errorCode = errorCode;
    }

    public MAGException(int errorCode, String detailMessage) {
        super(detailMessage);
        Log.d(TAG,detailMessage+" = "+errorCode);
        this.errorCode = errorCode;
    }

    public MAGException(int errorCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        Log.d(TAG,detailMessage+" = "+errorCode);
        this.errorCode = errorCode;
    }

    public MAGException(int errorCode, Throwable throwable) {
        super(throwable);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
