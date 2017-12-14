/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.connecta.util;

import com.ca.mas.foundation.FoundationConsts;

/**
 * <p>This collection of static values are the constants used in the connecta <i>transport layer</i> package.</p>
 */
public class ConnectaConsts extends FoundationConsts {

    public static int SSL_MESSAGING_PORT = 8883;
    public static String SSL_MESSAGING_SCHEME = "ssl";

    public static final String MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED = "com.ca.mas.connecta.MESSAGE_ARRIVED";

    /**
     * Topic Structure
     */
    public static final String TOPIC_VERSION_ORG = "2.0";
    public static final String CLIENT_ID_SEP = "::";

}
