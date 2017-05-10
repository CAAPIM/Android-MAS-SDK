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
 * the Native App URL from enterprise endpoint.
 */
public class NativeAppNotExistException extends EnterpriseBrowserException {

    public NativeAppNotExistException() {
        super(MAGErrorCode.ENTERPRISE_BROWSER_NATIVE_APP_DOES_NOT_EXIST);
    }

    public NativeAppNotExistException(String detailMessage) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_NATIVE_APP_DOES_NOT_EXIST, detailMessage);
    }

    public NativeAppNotExistException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_NATIVE_APP_DOES_NOT_EXIST, detailMessage, throwable);
    }

    public NativeAppNotExistException(Throwable throwable) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_NATIVE_APP_DOES_NOT_EXIST, throwable);
    }
}
