/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.oauth;

import android.support.annotation.Keep;

import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.foundation.MASResponse;

/**
 * This exception is thrown when an error occurs while accessing OAuth endpoint.
 */
@Keep
public class OAuthServerException extends MAGServerException {

    public OAuthServerException(MASResponse response, int errorCode, int status, String contentType, String detailMessage) {
        super(response, errorCode, status, contentType, detailMessage);
    }

    public OAuthServerException(MASResponse response, int errorCode, int status, String contentType, String detailMessage, Throwable throwable) {
        super(response, errorCode, status, contentType, detailMessage, throwable);
    }

    public OAuthServerException(MAGServerException e) {
        super(e);
    }
}
