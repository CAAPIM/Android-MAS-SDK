/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

public class UserNotAuthenticatedException extends RuntimeException {
    public UserNotAuthenticatedException() {
    }

    public UserNotAuthenticatedException(String detailMessage) {
        super(detailMessage);
    }

    public UserNotAuthenticatedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public UserNotAuthenticatedException(Throwable throwable) {
        super(throwable);
    }
}
