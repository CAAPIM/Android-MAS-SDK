/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.ent;

import com.ca.mas.core.error.MAGErrorCode;

/**
 * This exception is thrown when there is no Native URL and Auth URL from enterprise endpoint.
 */
public class BrowserAppNotExistException extends EnterpriseBrowserException {

    public BrowserAppNotExistException() {
        super(MAGErrorCode.ENTERPRISE_BROWSER_APP_DOES_NOT_EXIST);
    }

    public BrowserAppNotExistException(String detailMessage) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_APP_DOES_NOT_EXIST, detailMessage);
    }

    public BrowserAppNotExistException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_APP_DOES_NOT_EXIST, detailMessage, throwable);
    }

    public BrowserAppNotExistException(Throwable throwable) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_APP_DOES_NOT_EXIST, throwable);
    }
}
