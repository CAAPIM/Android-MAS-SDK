/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.store;

/**
 * Exception thrown if the token store can't be accessed.
 */
public class TokenStoreException extends Exception {
    public TokenStoreException() {
    }

    public TokenStoreException(String detailMessage) {
        super(detailMessage);
    }

    public TokenStoreException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TokenStoreException(Throwable throwable) {
        super(throwable);
    }
}
