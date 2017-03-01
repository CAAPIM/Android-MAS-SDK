/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.error.MAGServerException;

/**
 * <p><b>MASServerException</b> is a general exception wrapper used by the MAS SDK</p>
 */

public class MASServerException extends MAGServerException {

    public MASServerException(int errorCode, int status, String contentType, String detailMessage) {
        super(errorCode, status, contentType, detailMessage);
    }

    public MASServerException(int errorCode, int status, String contentType, String detailMessage, Throwable throwable) {
        super(errorCode, status, contentType, detailMessage, throwable);
    }

    public MASServerException(MAGServerException e) {
        super(e);
    }
}
