/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.error;

/**
 * Superclass for exceptions thrown when an MAG request cannot proceed due to the current state of the MSSO
 * engine.
 */
public class MAGStateException extends MAGException {

    public MAGStateException() {
        super(MAGErrorCode.UNKNOWN);
    }

    public MAGStateException(String detailMessage) {
        super(MAGErrorCode.UNKNOWN, detailMessage);
    }

    public MAGStateException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.UNKNOWN, detailMessage, throwable);
    }

    public MAGStateException(Throwable throwable) {
        super(MAGErrorCode.UNKNOWN, throwable);
    }
}
