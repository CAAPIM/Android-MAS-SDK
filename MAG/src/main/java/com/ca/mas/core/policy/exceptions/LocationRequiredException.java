/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.policy.exceptions;

import com.ca.mas.core.error.MAGErrorCode;
import com.ca.mas.core.error.MAGException;

public class LocationRequiredException extends MAGException {

    public LocationRequiredException() {
        super(MAGErrorCode.GEOLOCATION_IS_MISSING);
    }

    public LocationRequiredException(String detailMessage) {
        super(MAGErrorCode.GEOLOCATION_IS_MISSING, detailMessage);
    }

    public LocationRequiredException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.GEOLOCATION_IS_MISSING, detailMessage, throwable);
    }

    public LocationRequiredException(Throwable throwable) {
        super(MAGErrorCode.GEOLOCATION_IS_MISSING, throwable);
    }
}
