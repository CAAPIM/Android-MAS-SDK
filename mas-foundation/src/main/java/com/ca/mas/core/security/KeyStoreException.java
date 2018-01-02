/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.security;

public class KeyStoreException extends Exception {
    public KeyStoreException(String message, Throwable cause) {
        super(message, cause);
    }

    public KeyStoreException(Throwable cause) {
        super(cause);
    }
}
