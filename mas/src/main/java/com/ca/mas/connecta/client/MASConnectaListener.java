/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.client;

/**
 * Interface to listen for Connecta event
 */
public interface MASConnectaListener {

    /**
     * Called when an Connection Lost
     */
    void onConnectionLost();

    /**
     * Called when Invalid Message received
     */
    void onInvalidMessageFormat();

    /**
     * Called when message delivery completed and successfully retrieve the Message
     */
    void onDeliveryCompletedSuccess();

    /**
     * Called when message delivery completed but failed to retrieve the Message
     */
    void onDeliveryCompletedFailed(Exception e);


}
