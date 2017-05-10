/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage.implementation;

import android.util.Log;

import com.ca.mas.core.storage.Storage;
import com.ca.mas.core.storage.StorageException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * Factory that creates the the Storage instance.
 * <p/>
 * Synchronous Usage:
 * <pre>
 * {@code
 *
 * //Creates Factory
 * MASStorageManager factory= new MASStorageManager();
 * //Creates a Storage instance
 * Storage keyStoreStorage = factory.getStorage(MASStorageManager.MASStorageType.TYPE_KEYSTORE,context);
 * //Writes to the storage
 * keyStoreStorage.writeData("key1", "value1".getBytes("UTF-8"));
 * //Reads from storage
 * StorageResult result = keyStoreStorage.readData("key1");
 * if(result.getStatus()==StorageResult.StorageOperationStatus.SUCCESS){
 *  System.out.println("Value: "+new String((byte[])result.getData()));
 * }else if(result.getStatus()==StorageResult.StorageOperationStatus.FAILURE){
 *  StorageException exp = (StorageException)result.getData();
 *  System.out.println("Error: "+exp.getMessage());
 * }
 *
 * }
 * </pre>
 * <p/>
 * <p/>
 * Asynchronous Usage:
 * <pre>
 * {@code
 *
 * //Creates Factory
 * MASStorageManager factory= new MASStorageManager();
 * //Creates a Storage instance
 * Storage keyStoreStorage = factory.getStorage(MASStorageManager.MASStorageType.TYPE_KEYSTORE,context);
 * //Writes to the storage
 * keyStoreStorage.writeData("key1", "value1".getBytes("UTF-8"));
 * //Creates a StorageResultReceiver for Async read
 * StorageResultReceiver receiver = new StorageResultReceiver(null) {
 *  @Override
 *  public void onReceiveResult(StorageResult result) {
 *      if(result.getStatus()==StorageResult.StorageOperationStatus.SUCCESS){
 *        System.out.println("Value: "+new String((byte[])result.getData()));
 *      }else if(result.getStatus()==StorageResult.StorageOperationStatus.FAILURE){
 *        StorageException exp = (StorageException)result.getData();
 *        System.out.println("Error: "+exp.getMessage());
 *      }
 *  }
 * };
 * //Reads from storage
 * StorageResult result = keyStoreStorage.readData("key1",receiver);
 *
 * }
 * </pre>
 */
public class MASStorageManager {


    /**
     * Types of storage.
     */
    public enum MASStorageType {

        TYPE_KEYSTORE(KeyStoreStorage.class),
        TYPE_AMS(AccountManagerStorage.class);



        /*
        Add additional storage types below.
        */
        final private Class<? extends Storage> className;

        MASStorageType(Class<? extends Storage> className) {
            this.className = className;

        }

        private Class<? extends Storage> getClassName() {
            return className;
        }
    }

    /**
     * Gets the named storage using reflection. Use this method ONLY if the StorageType is unknown.
     * The recommended way will be to use
     * {@link com.ca.mas.core.storage.implementation.MASStorageManager#getStorage(com.ca.mas.core.storage.implementation.MASStorageManager.MASStorageType, Object)}
     *
     * @param className The name of the storage
     * @param options configuration input required for instantiating the storage. The type of this
     *                input is storage implementation specific.
     * @return Storage instance that is requested
     * @throws StorageException if there is any instantiation errors
     *
     */
    public Storage getStorage(String className, Object options) throws StorageException {

        try {
            return getStorage((Class<? extends Storage>) Class.forName(className), options);
        } catch (ClassNotFoundException e) {
            String msg = "Error instantiating the requested Storage - " + className + " reason: " + e;
            if (DEBUG) Log.e(TAG, msg);
            throw new StorageException(msg, null, StorageException.STORE_NOT_FOUND);
        }
    }

    /**
     * Gets the named storage using reflection. Use this method ONLY if the StorageType is unknown.
     * The recommended way will be to use
     * {@link com.ca.mas.core.storage.implementation.MASStorageManager#getStorage(com.ca.mas.core.storage.implementation.MASStorageManager.MASStorageType, Object)}
     *
     * @param c The class of the storage
     * @param options configuration input required for instantiating the storage. The type of this
     *                input is storage implementation specific.
     * @return Storage instance that is requested
     * @throws StorageException if there is any instantiation errors
     *
     */
    public Storage getStorage(Class<? extends Storage> c, Object options) throws StorageException {

        try {
            Constructor[] allConstructors = c.getDeclaredConstructors();
            if (allConstructors != null && allConstructors.length > 0) {
                Constructor constructor = allConstructors[0];
                constructor.setAccessible(true);
                return (Storage) constructor.newInstance(options);
            } else {
                throw new Exception("No constructors found");
            }

        }catch (InvocationTargetException in) {
            String msg = "Error instantiating the requested Storage - " + c.getCanonicalName() + " reason: " + in;
            if (DEBUG) Log.e(TAG, msg);
            throw (StorageException) ((InvocationTargetException) in).getTargetException();
        }
        catch (Exception e) {
            String msg = "Error instantiating the requested Storage - " + c.getName() + " reason: " + e;
            if (DEBUG) Log.e(TAG, msg);
            throw new StorageException(msg, null, StorageException.STORE_NOT_FOUND);
        }
    }


    /**
     * Gets the Storage of the specified type.
     *
     * @param type The type of storage. Say
     * {@link com.ca.mas.core.storage.implementation.MASStorageManager.MASStorageType#TYPE_KEYSTORE}
     * @param options configuration input required for instantiating the storage. The type of this
     *                input is storage implementation specific.
     * @return Storage instance
     * @throws StorageException if there is any instantiation errors
     *
     */
    public Storage getStorage(MASStorageType type, Object options) throws StorageException {

        if (type == null) {
            throw new StorageException(StorageException.STORE_NOT_FOUND);
        }
        return getStorage(type.getClassName(), options);

    }


}
