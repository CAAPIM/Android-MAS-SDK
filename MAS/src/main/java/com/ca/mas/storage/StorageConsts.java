/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.storage;

import com.ca.mas.foundation.util.FoundationConsts;

/**
 * <b>StorageConsts</b> Contains the constants used during both cloud and local encrypted storage operations.
 */
class StorageConsts extends FoundationConsts {

    // /Client/{clientId}/Data/{dataKey}
    public static final String KEY_COMPONENT_CLIENT = "Client";
    public static final String KEY_COMPONENT_USER = "User";
    public static final String KEY_COMPONENT_DATA = "Data";
    public static final String KEY_VALUE = "value";
    public static final String KEY_MODIFIED_DATE = "modifiedDate";
    public static final String KEY_TYPE = "type";
    public static final String MAS_STORAGE_MASS = "MASS";
    public static final String MAS_STORAGE_VERSION = "v1";
    public static final String KEY_RESULTS = "results";
    public static final String KEY_KEY = "key";
    public static final String KEY_REFERENCE = "$ref";

    public static final String DEFAULT_TYPE_STRING = "text/plain";
    public static final String TYPE_JSON = "application/json";
}
