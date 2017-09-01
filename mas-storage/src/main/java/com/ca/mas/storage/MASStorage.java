/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.storage;

import com.ca.mas.foundation.MASCallback;

import java.util.Set;

/**
 * A simple CRUD interface for securely storing key-value pairs.
 */
public interface MASStorage {

    /**
     * Save an object into Storage.
     * @param key      Key to be used when saving the object.
     * @param object   Object to be saved. It must be conform to one of the following type
     *                 {@link org.json.JSONObject},
     *                 {@link android.graphics.Bitmap},
     *                 {@link String}, byte[],
     * @param segment  The Storage segment {@link MASStorageSegment} to be used in the search
     * @param callback Notifies the caller with the result of the operation.
     *                 {@link com.ca.mas.foundation.MASCallback#onError(Throwable)} will be called incase
     */
    void save(String key,
              Object object,
              @MASStorageSegment int segment,
              MASCallback<Void> callback);

    /**
     * Find an object from Storage based on a specific key.
     * @param key      The key used to get the object from storage
     * @param segment  The Storage segment {@link MASStorageSegment} to be used in the search
     * @param callback Notifies the caller with the result of the operation.
     *                 {@link com.ca.mas.foundation.MASCallback#onSuccess(Object)} will be called in case
     *                 of success.
     *                 {@link com.ca.mas.foundation.MASCallback#onError(Throwable)} will be called in case of failure.
     */
    void findByKey(String key,
                   @MASStorageSegment int segment,
                   MASCallback callback);

    /**
     * Delete an object from Storage based on a given key.
     * @param key      The key used to delete the object from storage.
     * @param segment  The Storage segment {@link MASStorageSegment} to be used in the search
     * @param callback Notifies the caller with the result of the operation.
     *                 {@link com.ca.mas.foundation.MASCallback#onError(Throwable)} will be called in case of failure.
     */
    void delete(String key,
                @MASStorageSegment int segment,
                MASCallback<Void> callback);

    /**
     * Retrieve ALL keys from local storage.
     * Implementations should perform this operation asynchronously.
     * @param segment  The Storage segment {@link MASStorageSegment}
     * @param callback Notifies the caller with the result of the operation.
     *                 {@link com.ca.mas.foundation.MASCallback#onError(Throwable)} will be called in case of failure.
     */
    void keySet(int segment, MASCallback<Set<String>> callback);

    /**
     * Registers a new object type.
     * Used to convert an object into the form that it will be persisted.
     * @param converter The Converter to convert raw bytes to/from Object
     */
    void register(DataMarshaller converter);
}


