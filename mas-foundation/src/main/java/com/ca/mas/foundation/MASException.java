/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;


/**
 * Encapsulates a general MAS error as an exception that returns to a {@link MASCallback#onError(Throwable)}.
 */
public class MASException extends Throwable {

    public MASException(Throwable cause) {
        super(cause);
    }

    public MASException(String message, Throwable cause) {
        super(message, cause);
    }

    public Throwable getRootCause() {
        return getRootCause(getCause());
    }

    protected Throwable getRootCause(Throwable t) {
        if (t.getCause() == null) {
            return t;
        } else {
            if (!t.getClass().getPackage().getName().startsWith("com.ca") &&
                    !(t instanceof RuntimeException)) {
                return t;
            } else {
                return getRootCause(t.getCause());
            }
        }
    }
}
