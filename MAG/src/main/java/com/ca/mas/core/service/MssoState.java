/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Holds flag variables indicating various things.
 */
class MssoState {
    private static final AtomicBoolean expectingUnlock = new AtomicBoolean(false);

    static boolean isExpectedUnlock() {
        return expectingUnlock.get();
    }

    static void setExpectingUnlock(boolean b) {
        expectingUnlock.set(b);
    }
}
