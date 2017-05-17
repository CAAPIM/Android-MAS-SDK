/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.messaging;


import com.ca.mas.foundation.FoundationConsts;

/**
 * <p><b>MessagingConsts</b> contains the consts used by the messaging micro-service.</p>
 */
public class MessagingConsts extends FoundationConsts{

    static final String DEFAULT_VERSION = "1.0";
    static final String DEFAULT_SENDER_TYPE = "USER";
    public static final String DEFAULT_TEXT_PLAIN_CONTENT_TYPE = "text/plain";
    static final String DEFAULT_BASE64_ENCODING = "BASE64";

    /*
      * MASMessage payload keys
     */
    static final String KEY_VERSION = "Version";
    static final String KEY_SENDER_ID = "SenderId";
    static final String KEY_SENDER_TYPE = "SenderType";
    static final String KEY_DISPLAY_NAME = "DisplayName";
    static final String KEY_CONTENT_TYPE = "ContentType";
    static final String KEY_CONTENT_ENCODING = "ContentEncoding";
    static final String KEY_SENT_TIME = "SentTime";
    static final String KEY_PAYLOAD = "Payload";
    static final String KEY_TOPIC = "Topic";

    public static final int AT_MOST_ONCE = 0;
    public static final int AT_LEAST_ONCE = 1;
    public static final int EXACTLY_ONCE = 2;

    public static final String MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED = "com.ca.mas.connecta.MESSAGE_ARRIVED";

}
