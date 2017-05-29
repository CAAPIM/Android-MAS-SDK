/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.context;

/**
 * Exception thrown if a problem occurs within the MSSO SDK.
 */
public class MssoException extends RuntimeException {
    public MssoException() {
    }

    public MssoException(String detailMessage) {
        super(detailMessage);
    }

    public MssoException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MssoException(Throwable throwable) {
        super(throwable);
    }
}
