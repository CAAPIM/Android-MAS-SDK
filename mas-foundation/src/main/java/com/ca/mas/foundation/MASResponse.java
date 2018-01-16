/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

public interface MASResponse<T> {

    /**
     * Returns an unmodifiable map of the response-header fields and values.
     * Please refer to {@link HttpURLConnection#getHeaderFields()} for details.
     *
     * @return The response headers
     */
    Map<String, List<String>> getHeaders();

    /**
     * Returns the response code returned by the remote MAG server.
     * Please refer to {@link HttpURLConnection#getResponseCode()} ()} for details.
     *
     * @return The response code
     */

    int getResponseCode();

    /**
     * Returns the response message returned by the remote MAG server.
     * Please refer to {@link HttpURLConnection#getResponseMessage()} ()} for details.
     *
     * @return The response message
     */
    String getResponseMessage();

    /**
     * Returns the response body returned by the remote MAG Server
     *
     * @return The response body
     */
    MASResponseBody<T> getBody();

}
