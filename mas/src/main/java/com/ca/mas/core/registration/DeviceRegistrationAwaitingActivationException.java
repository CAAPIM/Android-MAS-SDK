/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.registration;

import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;

/**
 * Exception thrown if MAG requests cannot proceed because the device is registered but is awaiting
 * approval and activation by an administrator.
 */
public class DeviceRegistrationAwaitingActivationException extends MAGException {
    static final String DEFAULT_MESSAGE = "Device registration is incomplete -- awaiting manual approval";

    public DeviceRegistrationAwaitingActivationException() {
        super(MAGErrorCode.DEVICE_ALREADY_REGISTERED, DEFAULT_MESSAGE);
    }

}
