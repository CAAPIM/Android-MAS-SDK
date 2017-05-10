/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * This is the data transfer object that gets passed to the caller as either the return value
 * or via the callback.
 * <p/>
 */
public class StorageResult implements Parcelable {


    public static final Creator CREATOR =
            new Creator() {
                public StorageResult createFromParcel(Parcel in) {
                    return new StorageResult(in);
                }

                public StorageResult[] newArray(int size) {
                    return new StorageResult[size];
                }
            };

    /**
     * The Status of the operation, which defaults to {@link StorageResult.StorageOperationStatus#SUCCESS}
     */
    private StorageOperationStatus status = StorageOperationStatus.SUCCESS;
    /**
     * The data that needs to be passed across.
     * <p/>
     * The type and value of the data will vary with implementation and
     * its the responsibility of the implementation class to define the data
     */
    private Object data;

    /**
     * The type of the operation, this result represents
     */
    private StorageOperationType type;


    public StorageResult(StorageOperationType type){
        this.type = type;
    }

    private StorageResult(Parcel input) {
        this.type = (StorageOperationType) input.readSerializable();
        this.status = (StorageOperationStatus) input.readSerializable();
        this.data = input.readArray(this.getClass().getClassLoader())[0];

    }

    public StorageOperationType getType() {
        return type;
    }

    public void setType(StorageOperationType type) {
        this.type = type;
    }

    public StorageOperationStatus getStatus() {
        return status;
    }

    public void setStatus(StorageOperationStatus status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(getType());
        dest.writeSerializable(getStatus());
        dest.writeArray(new Object[]{data});
    }

    public enum StorageOperationStatus {
        SUCCESS(0),
        FAILURE(1);

        private int value;

        private StorageOperationStatus(int value) {
            this.value = value;
        }

    }

    public enum StorageOperationType {
        WRITE(0),
        WRITE_STRING(1),
        READ(2),
        READ_STRING(3),
        UPDATE(4),
        UPDATE_STRING(5),
        DELETE(6),
        DELETE_STRING(7),
        DELETE_ALL(8),
        WRITE_OR_UPDATE(9),
        WRITE_OR_UPDATE_STRING(10),
        GET_ALL_KEYS(11);

        private int value;

        private StorageOperationType(int value) {
            this.value = value;
        }


    }
}
