/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

/**
 * Custom exception class to deal with all the storage related errors.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class StorageException extends Exception implements Parcelable {

    public static final Creator CREATOR =
            new Creator() {
                public StorageException createFromParcel(Parcel in) {
                    return new StorageException(in);
                }

                public StorageException[] newArray(int size) {
                    return new StorageException[size];
                }
            };


    // Generic Storage error messages (100-115)
    public static final int UNKNOWN_ERROR = 100;
    public static final int INVALID_INPUT_KEY = 101;
    public static final int INVALID_INPUT_VALUE = 102;
    public static final int INVALID_INPUT_CALLBACK = 103;
    public static final int WRITE_DATA_ALREADY_EXISTS = 104;
    public static final int READ_DATA_NOT_FOUND = 105;
    public static final int INSTANTIATION_ERROR = 106;
    public static final int DO_NOT_USE = 107;
    public static final int STORE_NOT_FOUND = 108;
    public static final int UNSUPPORTED_DATA = 109;
    public static final int OPERATION_FAILED = 110;
    public static final int INVALID_INPUT = 111;
    public static final int UNSUPPORTED_OPERATION = 112;
    public static final int KEY_SIZE_LIMIT_EXCEEDED = 113;
    public static final int DATA_SIZE_LIMIT_EXCEEDED = 114;
    public static final int INSTANTIATION_ERROR_UNAUTHORIZED = 115;


    //KeyStore Related error messages (151-170)

    public static final int KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED = 151;
    public static final int KEYSTORE_DATA_SIZE_LIMIT_EXCEEDED = 152;
    public static final int STORE_NOT_UNLOCKED = 153;

    /**
     * The code that is associated with this instance
     */
    private int code = UNKNOWN_ERROR;

    public StorageException(int code) {
        super(null, null);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public StorageException(String detailMessage, Throwable throwable, int code) {
        super(detailMessage, throwable);
        this.code = code;
    }


    //--PARCELABLE methods follows--
    private StorageException(Parcel input) {
        super(input.readString(), null);
        code = input.readInt();

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel destination, int flags) {
        destination.writeString(getMessage());
        destination.writeInt(code);
    }


}
