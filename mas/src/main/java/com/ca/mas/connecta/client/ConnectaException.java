/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.client;

public class ConnectaException extends Exception {
    public ConnectaException() {
    }

    public ConnectaException(String detailMessage) {
        super(detailMessage);
    }

    public ConnectaException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ConnectaException(Throwable throwable) {
        super(throwable);
    }
}
