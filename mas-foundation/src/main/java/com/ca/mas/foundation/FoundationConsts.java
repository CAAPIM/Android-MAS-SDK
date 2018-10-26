/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

/**
 * <p><b>FoundationConsts</b> contains the base constants used by any classes in the hierarchy. The nature of these constants is
 * intentianally general and is limited to basic timeout, network constants, and character constants that could potentially be
 * used anywhere in the SDK.</p>
 */
public class FoundationConsts {

    // Timer
    public static final long TIMEOUT_VAL = 30000L;
    public static final long SEC_MILLIS = 1000L;

    // Scheme
    public static final String HTTPS_SCIM_SCHEME = "https";

    // Common characters and strings
    public static final String SPACE = " ";
    public static final String COLON = ":";
    public static final String SEMI_COLON = ";";
    public static final String FSLASH = "/";
    public static final String EMPTY = "";
    public static final String COMMA = ",";
    public static final String BASE64 = "base64,";
    public static final String EQ = "=";
    public static final String ENC_SPACE = "%20";
    public static final String ENC_DOUBLE_QUOTE = "%22";
    public static final char CLOSE_PAREN = ')';
    public static final char QM = '?';
    public static final char AMP = '&';
    public static final char DOT = '.';

    // keys
    public static final String KEY_VALUE = "value";
    public static final String KEY_TYPE = "type";
    public static final String KEY_ON_MESSAGE = "onMessage";
    public static final String KEY_MESSAGE = "Message";
    public static final String KEY_EVENT_TYPE = "eventType";

    public static final String UA_MOZILLA = "Mozilla/5.0 (Linux;";
    public static final String UA_KHTML = "(KHTML, like Gecko)";
    public static final String UA_APP_MOBILE_APP_SERVICES = "Application/MobileApplicationServices";

    // Content encodings
    public static final String ENC_UTF8 = "utf8";
    // MIME types and headers
    public static final String MT_APPLICATION_OCTET_STREAM = "application/octet-stream";
    public static final String MT_TEXT_PLAIN = "text/plain";
    public static final String MT_APP_JSON = "application/json";
    public static final String HEADER_KEY_USER_AGENT = "User-Agent";
    public static final String HEADER_KEY_ACCEPT = "Accept";
    public static final String HEADER_VALUE_ACCEPT = "application/scim+json";
    public static final String HEADER_KEY_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_VALUE_CONTENT_TYPE = "application/scim+json";

    // CONFIG constants
    public static final String KEY_CONFIG_SCIM_PATH = "mas.url.scim_path";
    public static final String KEY_CONFIG_CLOUD_STORAGE_PATH = "mas.url.mas_storage_path";
    public static final String KEY_CONFIG_USER_INFO = "mas.url.user_info";


    public static final String KEY_CONFIG_APP_NAME = "application_name";
    public static final String KEY_CONFIG_APP_TYPE = "application_type";
    public static final String KEY_CONFIG_APP_ORGANIZATION = "application_organization";
    public static final String KEY_CONFIG_APP_REGISTERED_BY = "application_registered_by";
    public static final String KEY_CONFIG_APP_DESCRIPTION = "application_description";
    public static final String KEY_ID_TOKEN_SIGN_ALG = "id_token_signed_response_alg";
}
