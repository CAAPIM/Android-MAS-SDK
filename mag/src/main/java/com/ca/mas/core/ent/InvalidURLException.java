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
 * This exception is thrown when an error occurs while processing
 * the Auth URL from enterprise endpoint.
 */
public class InvalidURLException extends EnterpriseBrowserException {
    public InvalidURLException() {
        super(MAGErrorCode.ENTERPRISE_BROWSER_WEB_APP_INVALID_URL);
    }

    public InvalidURLException(String detailMessage) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_WEB_APP_INVALID_URL, detailMessage);
    }

    public InvalidURLException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_WEB_APP_INVALID_URL, detailMessage, throwable);
    }

    public InvalidURLException(Throwable throwable) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_WEB_APP_INVALID_URL, throwable);
    }
}
