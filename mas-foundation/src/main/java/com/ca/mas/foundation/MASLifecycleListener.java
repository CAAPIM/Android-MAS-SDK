/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import java.net.URL;

/**
 * Interface that allows for implementing classes to listen for MAS lifecycle events.
 * Listener is registered with {@link MAS#addLifeCycleListener(MASLifecycleListener)} object.
 */
public interface MASLifecycleListener {


    /**
     * Invoke immediately after the MASSTATE is changed to STARTED.
     */
    void onStarted();

    /**
     * Invoke immediately after the device is registered.
     */
    void onDeviceRegistered();

    /**
     * Invoke immediately after the user is authenticated.
     */
    void onAuthenticated();

    /**
     * Invoke immediately after the device is degregistered.
     */
    void onDeRegistered();



}
