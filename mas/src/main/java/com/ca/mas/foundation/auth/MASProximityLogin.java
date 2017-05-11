/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.app.Activity;
import android.view.View;

import com.ca.mas.core.auth.PollingRenderer;

/**
 * Provide a standard interface for Proximity Login
 */
public interface MASProximityLogin {

    /**
     * Render the view for the Proximity login, the view can be a image, button or null for no view.
     * The view will put to the login dialog as one of the login provider.
     *
     * @return The login view to represent the login action.
     */
    View render();

    /**
     * Initialize the Renderer,
     *
     * @param activity The Activity
     * @param providers The Authentication Provider returned from gateway
     */
    boolean init(Activity activity, long requestId, MASAuthenticationProviders providers);

    /**
     * Start the Proximity Login process
     */
    void start();

    /**
     * Stop the Proximity Login process
     */
    void stop();

}
