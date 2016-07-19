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

public class LocationInvalidException extends MAGException {

    public LocationInvalidException() {
        super(MAGErrorCode.GEOLOCATION_IS_INVALID);
    }

    public LocationInvalidException(String detailMessage) {
        super(MAGErrorCode.GEOLOCATION_IS_INVALID, detailMessage);
    }

    public LocationInvalidException(String detailMessage, Throwable throwable) {
        super(MAGErrorCode.GEOLOCATION_IS_INVALID, detailMessage, throwable);
    }

    public LocationInvalidException(Throwable throwable) {
        super(MAGErrorCode.GEOLOCATION_IS_INVALID, throwable);
    }
}
