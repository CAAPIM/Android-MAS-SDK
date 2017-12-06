/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

/**
 * A class that contains user feedback messages.
 */
public class MASFoundationStrings {

    private MASFoundationStrings() {
    }

    // SDK strings
    public static final String SDK_UNINITIALIZED = "MAS SDK has not been initialized.";

    // Android strings
    public static final String API_TARGET_EXCEPTION = "Device API level does not meet the target API requirements.";

    // MASUser strings
    public static final String USER_NOT_CURRENTLY_AUTHENTICATED = "No currently authenticated user.";

    // Token strings
    public static final String TOKEN_ID_EXPIRED = "ID token is expired.";

    // Secure lock strings
    public static final String SECURE_LOCK_SESSION_CURRENTLY_LOCKED = "The session is currently locked.";
    public static final String SECURE_LOCK_FAILED_TO_SAVE_ID_TOKEN = "Failed to save ID token.";
    public static final String SECURE_LOCK_FAILED_TO_RETRIEVE_ID_TOKEN = "Failed to retrieve ID token.";
    public static final String SECURE_LOCK_FAILED_TO_SAVE_SECURE_ID_TOKEN = "Failed to save encrypted ID token.";
    public static final String SECURE_LOCK_FAILED_TO_DELETE_ID_TOKEN = "Failed to delete encrypted ID token.";
    public static final String SECURE_LOCK_FAILED_TO_DELETE_SECURE_ID_TOKEN = "Failed to delete encrypted ID token.";

    // Shared storage strings
    public static final String SHARED_STORAGE_NULL_ACCOUNT_NAME = "Account name cannot be null.";
    public static final String SHARED_STORAGE_NULL_ACCOUNT_TYPE = "Account type cannot be null.";

}
