/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

/**
 * Used for any issues encountered with creating the MASSharedStorage identifier.
 */
public class MASSharedStorageException extends RuntimeException {

    public MASSharedStorageException(String message) {
        super(message);
    }

    public MASSharedStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}