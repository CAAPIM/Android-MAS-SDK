/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

public class DataSourceException extends RuntimeException {

    public DataSourceException() {
    }

    public DataSourceException(String detailMessage) {
        super(detailMessage);
    }

    public DataSourceException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public DataSourceException(Throwable throwable) {
        super(throwable);
    }
}
