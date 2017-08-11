/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

/**
 * Thrown when an invalid host is identified.
 */
public class MASInvalidHostException extends MASException {

    public MASInvalidHostException(Throwable cause) {
        super(cause);
    }

    public MASInvalidHostException(String message, Throwable cause) {
        super(message, cause);
    }

    public Throwable getRootCause() {
        return super.getRootCause(getCause());
    }
}
