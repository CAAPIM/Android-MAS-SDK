/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

public interface KeyStore {

    // ResponseCodes
    int NO_ERROR = 1;
    int LOCKED = 2;
    int UNINITIALIZED = 3;
    int SYSTEM_ERROR = 4;
    int PROTOCOL_ERROR = 5;
    int PERMISSION_DENIED = 6;
    int KEY_NOT_FOUND = 7;
    int VALUE_CORRUPTED = 8;
    int UNDEFINED_ACTION = 9;
    int WRONG_PASSWORD = 10;

    boolean isUnlocked();

    boolean contains(String key);

    boolean delete(String key);

    boolean put(String key, byte[] value);

    byte[] get(String key);

    int getLastError();

    String[] saw(String prefix);


}
