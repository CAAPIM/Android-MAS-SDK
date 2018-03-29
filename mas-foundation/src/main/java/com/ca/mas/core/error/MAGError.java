/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.error;

/**
 * Thrown when a MAG error occurs.
 */
public class MAGError extends Error {

    public MAGError(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public MAGError(Throwable throwable) {
        super(throwable.getMessage(), throwable);

    }
}
