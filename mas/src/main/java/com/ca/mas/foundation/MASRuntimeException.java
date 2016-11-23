/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.error.MAGRuntimeException;

public class MASRuntimeException extends MAGRuntimeException {

    public MASRuntimeException(int errorCode) {
        super(errorCode);
    }

    public MASRuntimeException(int errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    public MASRuntimeException(int errorCode, String detailMessage, Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    public MASRuntimeException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
