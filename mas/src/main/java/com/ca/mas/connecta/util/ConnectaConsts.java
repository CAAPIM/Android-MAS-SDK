/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.util;

import com.ca.mas.foundation.util.FoundationConsts;

/**
 * <p>This collection of static values are the constants used in the connecta <i>transport layer</i> package.</p>
 */
public class ConnectaConsts extends FoundationConsts {

    public static int SSL_MESSAGING_PORT = 8883;
    public static String SSL_MESSAGING_SCHEME = "ssl";

    public static int TCP_MESSAGING_PORT = 1888;
    public static String TCP_MESSAGING_SCHEME = "tcp";

    /**
     *
     */
    public static final String MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED = "com.ca.mas.connecta.MESSAGE_ARRIVED";

    /**
     * MASMessage payload keys
     */
    public static final String KEY_VERSION = "Version";
    public static final String KEY_SENDER_ID = "SenderId";
    public static final String KEY_SENDER_TYPE = "SenderType";
    public static final String KEY_DISPLAY_NAME = "DisplayName";
    public static final String KEY_CONTENT_TYPE = "ContentType";
    public static final String KEY_CONTENT_ENCODING = "ContentEncoding";
    public static final String KEY_SENT_TIME = "SentTime";
    public static final String KEY_PAYLOAD = "Payload";
    public static final String KEY_TOPIC = "Topic";

    /**
     * Topic Structure
     */
    public static final String TOPIC_VERSION_ORG = "2.0";
    public static final String CLIENT_ID_SEP = "::";

}
