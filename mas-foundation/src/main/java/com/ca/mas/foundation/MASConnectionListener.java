/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Interface that allows for implementing classes to listen for connection events.
 * Listener is registered with {@link MAS#setConnectionListener(MASConnectionListener)} object.
 */
public interface MASConnectionListener {

    /**
     * Invoke immediately after the call {@link URL#openConnection()}.
     * Note that the connection is not connected and not ready to retrieve any response from
     * the connection.
     *
     * @param connection The HttpURLConnection to the MAG Server
     */
    void onObtained(HttpURLConnection connection);

    /**
     * This method will be invoked after the HTTP request is prepared by the SDK.
     * For POST or PUT, the data has been sent to the connection.
     *
     * @param connection The HttpURLConnection to the MAG Server
     */
    void onConnected(HttpURLConnection connection);
}
