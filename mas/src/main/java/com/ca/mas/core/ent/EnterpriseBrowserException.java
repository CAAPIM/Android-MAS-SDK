/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.ent;

import com.ca.mas.core.error.MAGException;

/**
 * The Base Exception class for Enterprise Browser
 */
public abstract class EnterpriseBrowserException extends MAGException {

    public EnterpriseBrowserException(int errorCode) {
        super(errorCode);
    }

    public EnterpriseBrowserException(int errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    public EnterpriseBrowserException(int errorCode, String detailMessage, Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    public EnterpriseBrowserException(int errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
