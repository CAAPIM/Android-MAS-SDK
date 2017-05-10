/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.error.MAGStateException;

/**
 * Exception thrown if the token store is needed but is unavailable (not initialized, device locked)
 */
public class TokenStoreUnavailableException extends MAGStateException {

    public TokenStoreUnavailableException() {
    }

    public TokenStoreUnavailableException(String detailMessage) {
        super(detailMessage);
    }

    public TokenStoreUnavailableException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TokenStoreUnavailableException(Throwable throwable) {
        super(throwable);
    }
}
