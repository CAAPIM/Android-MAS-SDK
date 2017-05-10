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
    static final String KEY_COMPONENT_CLIENT = "Client";
    static final String KEY_COMPONENT_USER = "User";
    static final String KEY_COMPONENT_DATA = "Data";
    static final String KEY_MODIFIED_DATE = "modifiedDate";
    static final String MAS_STORAGE_MASS = "MASS";
    static final String MAS_STORAGE_VERSION = "v1";
    static final String KEY_RESULTS = "results";
    static final String KEY_KEY = "key";
    static final String KEY_REFERENCE = "$ref";

    static final String DEFAULT_TYPE_STRING = "text/plain";
    static final String TYPE_JSON = "application/json";
}
