/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

/**
 * Contains enums for describing the grant flow and the SDK state.
 */
public class MASConstants {

    private MASConstants() {
    }

    /**
     * The user credentials grant flow.
     */
    public static final int MAS_GRANT_FLOW_PASSWORD = 2;
    /**
     * The client credentials grant flow.
     */
    public static final int MAS_GRANT_FLOW_CLIENT_CREDENTIALS = 1;

    public static final int MAS_USER = 1;
    public static final int MAS_APPLICATION = 2;

    /**
     * State that SDK has not been initialized and does not have configuration file
     * either in local file system based on the default configuration file name, nor in the keychain storage.
     */
    public static final int MAS_STATE_NOT_CONFIGURED = 1;

    /**
     * State that SDK has the active configuration either in the local file system, but has not been initialized yet.
     */
    public static final int MAS_STATE_NOT_INITIALIZED = 2;

    /**
     * State that SDK did start; at this state, SDK should be fully functional.
     */
    public static final int MAS_STATE_STARTED = 3;

    /**
     * State that SDK did stop; at this state, SDK is properly stopped and should be able to re-start.
     */
    public static final int MAS_STATE_STOPPED = 4;

}

