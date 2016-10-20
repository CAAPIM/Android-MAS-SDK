/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.security;

public class SecureLockException extends RuntimeException {

    public SecureLockException() {
    }

    public SecureLockException(String message) {
        super(message);
    }

    public SecureLockException(Throwable cause) {
        super(cause);
    }

    public SecureLockException(String message, Throwable cause) {
        super(message, cause);
    }

}
