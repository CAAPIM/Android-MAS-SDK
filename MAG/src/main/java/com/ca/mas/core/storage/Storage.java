/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

import com.ca.mas.core.storage.implementation.MASStorageManager;

/**
 * Storage interfaces to support CRUD operations.
 * <p/>
 * <p/>
 * The implementation(s) should extend this abstract class and override the required methods. They
 * should notify the caller about the operation status via the
 * {@link StorageResult}, when ever possible.
 * <p/>
 * The interface has methods that
 * <p/>
 * a) Returns a value: These are generally meant for Synchronous operations. Implementations should
 * throw an {@link StorageException} with code {@link StorageException#UNSUPPORTED_OPERATION}
 * if the storage cannot be accessed in a Synchronous/Atomic way, so that a value can be returned.
 * <p/>
 * b) Does not return a value: These are meant for Asynchronous operations. The caller should be
 * notified about the operation status via the callback parameter that is passed in. Also these
 * methods could throw Exceptions, in case of some input validations.
 * <p/>
 *
 * */
public abstract class Storage {


    /**
     * Initialize the storage.
     *
     * @param options Input to set up the storage
     * @throws StorageException if initialization of storage fails.
     */
    protected Storage(Object options) throws StorageException {

    }

    /**
     * Gets the type of storage. This should ideally be the type that the
     * Manager use to instantiate the Storage.
     *
     * @return Type of the Storage which will be something like
     * {@link com.ca.mas.core.storage.implementation.MASStorageManager.MASStorageType#TYPE_KEYSTORE}
     */
    public abstract MASStorageManager.MASStorageType getType();


//CREATE

    /**
     * Writes some generic data to the Storage. The operation will fail if there is any previous
     * data associated with the key. This method is expected to block and hence its recommended to
     * call this method from a non-UI thread.
     *
     * @param key   The key against which the data has to be stored
     * @param value The generic data that needs to be stored
     * @return StorageResult which is expected to have
     * - the key as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     *
     */
    public abstract StorageResult writeData(String key, byte[] value) throws StorageException;


    /**
     * Writes some generic data to the Storage. The operation will fail if there is any previous
     * data associated with the key. Implementations are requested to offload all heavy duty work to
     * another thread and use the callback to notify the caller.
     *
     * @param key      The key against which the data has to be stored
     * @param value    The generic data that needs to be stored
     * @param callback Notifies the caller with a StorageResult which is expected to have
     *                 - the key as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void writeData(String key, byte[] value, StorageResultReceiver callback) throws StorageException;

    /**
     * Writes String data to the Storage. This is a convenience method and will generally use other
     * flavours of writeData to complete the operation. The operation will fail if there is any
     * previous data associated with the key. This method is expected to block and hence its
     * recommended to call this method from a non-UI thread.
     *
     * @param key   The key against which the data has to be stored
     * @param value The String data that need to be stored
     * @return StorageResult ,which is expected to have
     * - the key as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     *
     */
    public abstract StorageResult writeString(String key, String value) throws StorageException;

    /**
     * Writes String data to the Storage. This is a convenience method and will generally use other
     * flavours of writeData to complete the operation. The operation will fail if there is any
     * previous data associated with the key. This method is expected to block and hence its
     * recommended to call this method from a non-UI thread.
     *
     * @param key      The key against which the data has to be stored
     * @param value    The String data that need to be stored
     * @param callback Notifies the caller with a StorageResult which is expected to have
     *                 - the key as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     *
     */
    public abstract void writeString(String key, String value, StorageResultReceiver callback) throws StorageException;


//READ

    /**
     * Fetches generic data from the Store. The operation will fail if there is no
     * previous data associated with the key. This method is expected to block and hence its
     * recommended to call this method from a non-UI thread.
     *
     * @param key The key for which the data has to be retrieved
     * @return StorageResult ,which is expected to have
     * - the requested data as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract StorageResult readData(String key) throws StorageException;

    /**
     * Fetches generic data from the Store.  The operation will fail if there is no
     * previous data associated with the key. Implementations are requested to offload all
     * heavy duty work to another thread and use the callback to notify the caller.
     *
     * @param key      The key for which the data has to be retrieved
     * @param callback send a StorageResult, which is expected to have
     *                 - the requested data as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void readData(String key, StorageResultReceiver callback) throws StorageException;

    /**
     * Fetches String data from the Store.  The operation will fail if there is no
     * previous data associated with the key.This method is expected to block and hence its
     * recommended to call this method from a non-UI thread.
     *
     * @param key The key for which the String data has to be retrieved
     * @return StorageResult ,which is expected to have
     * - the String data as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract StorageResult readString(String key)throws StorageException;

    /**
     * Fetches String from the Store.  The operation will fail if there is no
     * previous data associated with the key. Implementations are requested to offload all
     * heavy duty work to another thread and use the callback to notify the caller.
     *
     * @param key      The key for which the String data has to be retrieved
     * @param callback send a StorageResult, which is expected to have
     *                 - the requested data as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void readString(String key, StorageResultReceiver callback) throws StorageException;




//UPDATE


    /**
     * Updates a pre-existing data with a new one.  This operation will only update, if the data
     * already exists for the key. This method is expected to block and hence its
     * recommended to call this method from a non-UI thread.
     *
     * @param key   The key for which the data has to be retrieved
     * @param value The new data, that need to be stored
     * @return StorageResult which is expected to have
     * - the key as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     */
    public abstract StorageResult updateData(String key, byte[] value) throws StorageException;

    /**
     * Updates a pre-existing data with a new one. This operation will only update, if the data
     * already exists for the key. Implementations are requested to offload all
     * heavy duty work to another thread and use the callback to notify the caller.
     *
     * @param key      The key for which the data has to be retrieved
     * @param value    The new data, that need to be stored
     * @param callback Notifies the caller with a StorageResult which is expected to have
     *                 - the key as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     *
     */
    public abstract void updateData(String key, byte[] value, StorageResultReceiver callback) throws StorageException;


    /**
     * Updates a pre-existing data with a new String.  This operation will only update, if the data
     * already exists for the key. This method is expected to block and hence its
     * recommended to call this method from a non-UI thread.
     *
     * @param key   The key for which the data has to be retrieved
     * @param value The new String data, that need to be stored
     * @return StorageResult which is expected to have
     * - the key as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     */
    public abstract StorageResult updateString(String key, String value) throws StorageException;

    /**
     * Updates a pre-existing data with a new String. This operation will only update, if the data
     * already exists for the key. Implementations are requested to offload all
     * heavy duty work to another thread and use the callback to notify the caller.
     *
     * @param key      The key for which the data has to be retrieved
     * @param value    The new String data, that need to be stored
     * @param callback Notifies the caller with a StorageResult which is expected to have
     *                 - the key as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void updateString(String key, String value, StorageResultReceiver callback) throws StorageException;


//WRITE OR UPDATE
    /**
     * Updates a pre-existing data with a new one.  This operation will update, if the data
     * already exists for the key and if there is none, will create. This method is expected to
     * block and hence its recommended to call this method from a non-UI thread.
     *
     * @param key   The key for which the data has to be retrieved
     * @param value The new data, that need to be stored
     * @return StorageResult which is expected to have
     * - the key as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     */
    public abstract StorageResult writeOrUpdateData(String key, byte[] value) throws StorageException;

    /**
     * Updates a pre-existing data with a new one. This operation will update, if the data
     * already exists for the key and if there is none, will create. Implementations are requested
     * to offload all heavy duty work to another thread and use the callback to notify the caller.
     *
     * @param key      The key for which the data has to be retrieved
     * @param value    The new data, that need to be stored
     * @param callback Notifies the caller with a StorageResult which is expected to have
     *                 - the key as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void writeOrUpdateData(String key, byte[] value, StorageResultReceiver callback) throws StorageException;

    /**
     * Updates a pre-existing data with a new one.  This operation will update, if the data
     * already exists for the key and if there is none, will create. This method is expected to
     * block and hence its recommended to call this method from a non-UI thread.
     *
     * @param key   The key for which the data has to be retrieved
     * @param value The new String data, that need to be stored
     * @return StorageResult which is expected to have
     * - the key as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     */
    public abstract StorageResult writeOrUpdateString(String key, String value) throws StorageException;

    /**
     * Updates a pre-existing data with a new one. This operation will update, if the data
     * already exists for the key and if there is none, will create. Implementations are requested
     * to offload all heavy duty work to another thread and use the callback to notify the caller.
     *
     * @param key      The key for which the data has to be retrieved
     * @param value    The new String data, that need to be stored
     * @param callback Notifies the caller with a StorageResult which is expected to have
     *                 - the key as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void writeOrUpdateString(String key, String value, StorageResultReceiver callback) throws StorageException;

//DELETE

    /**
     * Deletes an entry, essentially a key-value pair, from the storage. This method is expected to
     * block and hence its recommended to call this method from a non-UI thread.
     *
     * @param key The key for which the data will be deleted
     * @return StorageResult which is expected to have
     * - the key as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     */
    public abstract StorageResult deleteData(String key) throws StorageException;

    public abstract StorageResult deleteString(String key) throws StorageException;

    /**
     * Deletes an entry, essentially a key-value pair, from the storage. Implementations are
     * requested to offload all heavy duty work to another thread and use the callback to
     * notify the caller.
     *
     * @param key The key for which the data will be deleted
     * @param callback Notifies the caller with a StorageResult which is expected to have
     *                 - the key as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void deleteData(String key, StorageResultReceiver callback) throws StorageException;

    public abstract void deleteString(String key, StorageResultReceiver callback) throws StorageException;

    /**
     * Deletes all entries from the storage. This method is expected to block and hence
     *  its recommended to call this method from a non-UI thread.
     *
     * @return StorageResult which is expected to have
     * - status part to be SUCCESS, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     */
    public abstract StorageResult deleteAll();

    /**
     * Deletes all entries from the storage. Implementations are requested to offload all heavy
     * duty work to another thread and use the callback to notify the caller.
     *
     * @param callback Notifies the caller with a StorageResult which is expected to have
     *                 - status part to be SUCCESS, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void deleteAll(StorageResultReceiver callback) throws StorageException;


//GET ALL KEYS

    /**
     * Fetch all the keys against which data are stored. This method is expected to block and hence
     *  its recommended to call this method from a non-UI thread.
     *
     * @return An StorageResult , which is expected to have
     * - a list of all the keys (or an empty list) as the data part, if the operation is a SUCCESS
     * - StorageException as the data part, if the operation is a FAILURE
     */
    public abstract StorageResult getAllKeys();

    /**
     * Fetch all the keys against which data are stored. Implementations are requested to offload
     * all heavy duty work to another thread and use the callback to notify the caller.
     *
     * @param callback send a StorageResult , which is expected to have
     *                 - a list of all the keys (or an empty list) as the data part, if the operation is a SUCCESS
     *                 - StorageException as the data part, if the operation is a FAILURE
     * @throws StorageException if input validation fails.
     */
    public abstract void getAllKeys(StorageResultReceiver callback) throws StorageException;

}
