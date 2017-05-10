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
 * the JSON Message from enterprise endpoint.
 */
public class InvalidResponseException extends EnterpriseBrowserException {

    public InvalidResponseException(Throwable throwable) {
        super(MAGErrorCode.ENTERPRISE_BROWSER_INVALID_RESPONSE, throwable);
    }
}
