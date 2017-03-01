/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

public class OnErrorResult extends RuntimeException {

    public OnErrorResult(Throwable cause) {
        super(cause);
    }
}
