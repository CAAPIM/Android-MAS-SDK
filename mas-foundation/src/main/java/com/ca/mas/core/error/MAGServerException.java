/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.error;

import com.ca.mas.foundation.MASResponse;

/**
 * The Base Exception class for MAG Server communication,
 * thrown when an error occurs while accessing the MAG Server endpoint
 */
public class MAGServerException extends Exception {

    private int errorCode;
    private int status;
    private String contentType;
    private MASResponse response;

    public MAGServerException(MASResponse response, int errorCode, int status, String contentType, String detailMessage) {
        super(detailMessage);
        this.response = response;
        this.errorCode = errorCode;
        this.status = status;
        this.contentType = contentType;
    }

    public MAGServerException(MASResponse response, int errorCode, int status, String contentType, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        this.response = response;
        this.errorCode = errorCode;
        this.status = status;
        this.contentType = contentType;
    }

    public MAGServerException(MAGServerException e) {
        this(e.getResponse(), e.getErrorCode(), e.getStatus(), e.getContentType(), e.getMessage(), e.getCause());
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getStatus() {
        return status;
    }

    public String getContentType() {
        return contentType;
    }

    public MASResponse getResponse() {
        return response;
    }
}
