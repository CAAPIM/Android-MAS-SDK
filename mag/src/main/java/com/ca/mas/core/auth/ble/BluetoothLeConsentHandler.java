/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth.ble;

/**
 * Represents a consent request for handling Session Sharing.
 * Either {@link #proceed} or {@link #cancel} to set the authorize response
 * to the consent request.
 */
public interface BluetoothLeConsentHandler {

    /**
     * Starts the Session Sharing authorization request.
     */
    void proceed();

    /**
     * Cancels the Session Sharing authorization request.
     */
    void cancel();
}
