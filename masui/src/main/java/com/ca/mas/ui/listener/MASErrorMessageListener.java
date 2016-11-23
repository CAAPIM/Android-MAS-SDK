/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.ui.listener;

/**
 * Listener interface to return error message to the calling activity.
 */
public interface MASErrorMessageListener {

    /**
     * This is the method which is used as a callback on error.
     *
     * @param message
     */
    void getErrorMessage(String message);
}