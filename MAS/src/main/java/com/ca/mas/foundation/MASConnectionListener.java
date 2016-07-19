/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.http.MAGRequest;

/**
 * Interface that allows for implementing classes to listen for connection events.
 * Listener is registered with {@link MAS#setConnectionListener(MASConnectionListener)} object.
 */
public interface MASConnectionListener extends MAGRequest.MAGConnectionListener {
}
