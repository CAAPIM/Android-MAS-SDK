/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.http.MAGResponse;

public interface MASResponse<T> extends MAGResponse {

    /**
     * Returns the response body returned by the remote MAG Server
     *
     * @return The response body
     */
    MASResponseBody<T> getBody();

}
