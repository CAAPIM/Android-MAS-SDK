/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.store.TokenStoreException;

/**
 * Exception to handle ma-identifier cannot be found from the server.
 * The device status is out of sync with the server.
 */
public class InvalidIdentifierException extends RetryRequestException {

    public static final String INVALID_MAG_IDENTIFIER_SUFFIX = "107";

    public InvalidIdentifierException() {
        super();
    }

    public InvalidIdentifierException(Throwable throwable) {
        super(throwable);
    }

    @Override
    public void recover(MssoContext context) {
        context.destroyPersistentTokens();
    }
}
