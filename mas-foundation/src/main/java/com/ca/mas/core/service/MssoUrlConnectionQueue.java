/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.service;

import android.os.Bundle;

import com.ca.mas.core.util.Functions;
import com.ca.mas.foundation.MASConnectionListener;

import java.net.HttpURLConnection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents pending outbound requests.
 */
class MssoUrlConnectionQueue {

    private static final MssoUrlConnectionQueue INSTANCE = new MssoUrlConnectionQueue();

    // Input queue
    private final Map<Long, HttpURLConnection> connections = new LinkedHashMap<Long, HttpURLConnection>();

    private MssoUrlConnectionQueue() {
    }

    public static MssoUrlConnectionQueue getInstance() {
        return INSTANCE;
    }

    synchronized void addConnection(Long id,  HttpURLConnection urlConnection) {
        connections.put(id, urlConnection);
    }

    synchronized HttpURLConnection getConnection(long requestId) {
        return connections.get(requestId);
    }

    synchronized HttpURLConnection removeConnection(long requestId) {
        return connections.remove(requestId);
    }

}
