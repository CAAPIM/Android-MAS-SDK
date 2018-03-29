/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage.implementation;

import android.content.Context;
import android.util.Log;

import com.ca.mas.core.security.KeyStore;
import com.ca.mas.core.security.KeyStoreAdapter;
import com.ca.mas.core.storage.Storage;
import com.ca.mas.core.storage.StorageException;
import com.ca.mas.core.storage.StorageResult;
import com.ca.mas.core.storage.StorageResultReceiver;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * KeyStore backed Storage implementation.
 */
public final class KeyStoreStorage extends Storage {

    /**
     * The L7 KeyStore utility instance.
     */
    private KeyStore ks;

    /**
     * Key Prefix.
     * The value will be the package name for private mode and "SHARED_" for shared mode.
     */
    private String prefix = "";

    /**
     * MAX size of the Key, in characters, that can be written in to this storage.
     * Please note that this is inclusive of the prefix.
     */
    private static int MAX_KEY_SIZE = 120;

    /**
     * MAX size of data, in bytes, that can be written in to this storage.
     */
    private static final int MAX_DATA_SIZE = 32768;

    /**
     * Initialize the storage.
     *
     * @param options Object[] with 2 elements
     *                1) An android.content.Context
     *                2) (optional) Boolean value indicating whether the storage is Shared or not
     *
     * @throws StorageException if initialization of storage fails.
     */
    protected KeyStoreStorage(Object options) throws StorageException {
        super(options);
        try {
            if (options == null || !(options instanceof Object[])) {
                throw new StorageException(StorageException.INVALID_INPUT);
            }
            Object[] inputs = (Object[]) options;
            Context ctx;
            try {
                ctx = (Context) inputs[0];
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "Missing Context input.", e);
                throw new StorageException(StorageException.INVALID_INPUT);
            }

            try {
                prefix = (boolean) inputs[1] ? "SHARED_" : ctx.getPackageName() + "_";
            } catch (Exception e) {
                if (DEBUG) Log.w(TAG, "Wrong shared input attribute, falling back to private.", e);
                //if not specified , assume default as "not shared"
                prefix = ctx.getPackageName()+"_";
            }

            this.ks = KeyStoreAdapter.getKeyStore();
            /*if (!ks.isUnlocked()) {
                throw new StorageException(StorageException.STORE_NOT_UNLOCKED);
            }*/
        } catch (StorageException bubble) {
            throw bubble;
        } 
    }

    @Override
    public MASStorageManager.MASStorageType getType() {
        return MASStorageManager.MASStorageType.TYPE_KEYSTORE;
    }

    @Override
    public StorageResult writeData(String key, byte[] value) throws StorageException {
        validateInputs(key, value);
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.WRITE);
        if(!isReady(returnValue)){
            return returnValue;
        }
        StorageException returnException = null;
        try {
            if (readData(key).getStatus() == StorageResult.StorageOperationStatus.SUCCESS) {
                returnException = new StorageException(StorageException.WRITE_DATA_ALREADY_EXISTS);
            } else {
                key = sanitizeKey(key);
                boolean status = ks.put(key, value);
                try {
                    checkForError(status);
                } catch (StorageException e) {
                    returnException = e;
                }
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "KeyStoreStorage write error.", e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }
        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(key.substring(prefix.length()));
        }
        return returnValue;
    }

    @Override
    public void writeData(final String key, final byte[] value, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = writeData(key, value);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult writeString(String key, String value) throws StorageException {
        StorageResult returnValue;
        validateInputs(key, value);
        try {
            returnValue = writeData(key, value.getBytes(UTF8));
            returnValue.setType(StorageResult.StorageOperationType.WRITE_STRING);
        } catch (UnsupportedEncodingException ignore) {
            //As the validateInputs should have taken care of this exception.
            throw new StorageException(StorageException.UNSUPPORTED_DATA);
        }
        return returnValue;
    }

    @Override
    public void writeString(final String key, final String value, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = writeString(key, value);
        returnValue.setType(StorageResult.StorageOperationType.WRITE_STRING);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult readData(String key) throws StorageException {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.READ);
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }
        key = sanitizeKey(key);
        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }
        if (!isReady(returnValue)) {
            return returnValue;
        }
        StorageException returnError = null;
        try {
            byte[] value = ks.get(key);
            if (value == null) {
                returnError = new StorageException(StorageException.READ_DATA_NOT_FOUND);
            } else {
                returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
                returnValue.setData(value);
            }

        } catch (Exception e) {
            returnError = new StorageException(StorageException.OPERATION_FAILED);
        }

        if (returnError != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnError);
        }
        return returnValue;
    }

    @Override
    public void readData(final String key, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = readData(key);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult readString(String key) throws StorageException {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.READ_STRING);
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }
        key = sanitizeKey(key);
        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }
        if (!isReady(returnValue)) {
            return returnValue;
        }
        StorageException returnError = null;
        try {
            byte[] value = ks.get(key);
            if (value == null) {
                returnError = new StorageException(StorageException.READ_DATA_NOT_FOUND);
            } else {
                returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
                String stringValue;
                try {
                    stringValue = new String(value, UTF8);
                } catch (UnsupportedEncodingException e) {
                    if (DEBUG) Log.w(TAG, "The data is not UTF-8 " + e);
                    stringValue = new String(value);
                }
                returnValue.setData(stringValue);
            }
        } catch (Exception e) {
            returnError = new StorageException(StorageException.OPERATION_FAILED);
        }

        if (returnError != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnError);
        }
        return returnValue;
    }

    @Override
    public void readString(final String key, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = readString(key);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult updateData(String key, byte[] value) throws StorageException {
        validateInputs(key, value);
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.UPDATE);
        if (!isReady(returnValue)) {
            return returnValue;
        }
        StorageException returnException = null;
        try {
            if (readData(key).getStatus() == StorageResult.StorageOperationStatus.SUCCESS) {
                key = sanitizeKey(key);
                boolean status = ks.put(key, value);
                try {
                    checkForError(status);
                } catch (StorageException e) {
                    returnException = e;
                }
            } else {
                returnException = new StorageException(StorageException.READ_DATA_NOT_FOUND);
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "update error " + e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }
        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(key.substring(prefix.length()));
        }
        return returnValue;
    }

    @Override
    public void updateData(final String key, final byte[] value, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = updateData(key, value);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult updateString(String key, String value) throws StorageException {
        validateInputs(key, value);

        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.UPDATE_STRING);
        if (!isReady(returnValue)) {
            return returnValue;
        }
        StorageException returnException = null;
        try {
            if (readData(key).getStatus() == StorageResult.StorageOperationStatus.SUCCESS) {
                key = sanitizeKey(key);
                boolean status = ks.put(key, value.getBytes(UTF8));
                try {
                    checkForError(status);
                } catch (StorageException e) {
                    returnException = e;
                }
            } else {
                returnException = new StorageException(StorageException.READ_DATA_NOT_FOUND);
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "update string error " + e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }
        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(key.substring(prefix.length()));
        }
        return returnValue;
    }

    @Override
    public void updateString(final String key, final String value, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = updateString(key, value);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult writeOrUpdateData(String key, byte[] value) throws StorageException {
        validateInputs(key, value);
        key = sanitizeKey(key);
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.WRITE_OR_UPDATE);
        StorageException returnException = null;
        try {
            boolean status = ks.put(key, value);
            try {
                checkForError(status);
            } catch (StorageException e) {
                returnException = e;
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "write/update error " + e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }
        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(key.substring(prefix.length()));
        }
        return returnValue;
    }

    @Override
    public void writeOrUpdateData(final String key, final byte[] value, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = writeOrUpdateData(key, value);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult writeOrUpdateString(String key, String value) throws StorageException {
        validateInputs(key, value);
        key = sanitizeKey(key);
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING);
        StorageException returnException = null;
        try {
            boolean status = ks.put(key, value.getBytes(UTF8));
            try {
                checkForError(status);
            } catch (StorageException e) {
                returnException = e;
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "write/update string error " + e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }
        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(key.substring(prefix.length()));
        }
        return returnValue;
    }

    @Override
    public void writeOrUpdateString(final String key, final String value, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = writeOrUpdateString(key, value);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult deleteData(String key) throws StorageException {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.DELETE);
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }

        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }
        if(!isReady(returnValue)){
            return returnValue;
        }
        StorageException returnException = null;
        try {
            if (readData(key).getStatus() != StorageResult.StorageOperationStatus.SUCCESS) {
                returnException = new StorageException(StorageException.READ_DATA_NOT_FOUND);
            } else {
                key = sanitizeKey(key);
                boolean status = ks.delete(key);
                try {
                    checkForError(status);
                } catch (StorageException e) {
                    returnException = e;
                }
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "delete error " + e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }
        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(key.substring(prefix.length()));
        }
        return returnValue;
    }

    @Override
    public StorageResult deleteString(String key) throws StorageException {
        StorageResult result = deleteData(key);
        result.setType(StorageResult.StorageOperationType.DELETE_STRING);
        return result;
    }

    @Override
    public void deleteData(final String key, final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = deleteData(key);
        notifyCallback(callback, returnValue);
    }

    @Override
    public void deleteString(String key, StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = deleteString(key);
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult deleteAll() {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.DELETE_ALL);
        StorageException returnError = null;
        int successCount = 0;
        int failureCount = 0;
        try {
            ArrayList<String> screenedKeys = (ArrayList) getAllKeys().getData();
            for (String currentKey : screenedKeys) {
                try {
                    if (ks.delete(prefix+currentKey)) {
                        successCount++;
                    } else {
                        failureCount++;
                    }
                } catch (Exception e) {
                    failureCount++;
                }
            }

            if (failureCount != 0) {
                String msg = "Failed to deleteData " + failureCount + " entries. Entries deleted: " + successCount;
                if (DEBUG) Log.e(TAG, msg);
                returnError = new StorageException(msg, null, StorageException.OPERATION_FAILED);
            } else {
                if (DEBUG) Log.i(TAG, "Deleted " + successCount + " entries ");
            }

        } catch (Exception e) {
            returnError = new StorageException(StorageException.OPERATION_FAILED);
        }

        if (returnError != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnError);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(successCount);
        }
        return returnValue;
    }

    @Override
    public void deleteAll(final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = deleteAll();
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult getAllKeys() {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.GET_ALL_KEYS);
        if (!isReady(returnValue)) {
            return returnValue;
        }
        StorageException returnError = null;
        ArrayList<String> keys = new ArrayList<>();
        try {
            String[] realKeys = ks.saw("");
            if (realKeys != null && realKeys.length > 0) {
                for (String key : realKeys) {
                    if (key.startsWith(prefix)) {
                        keys.add(key.substring(prefix.length()));
                    }
                }
            }
        } catch (Exception e) {
            returnError = new StorageException(StorageException.OPERATION_FAILED);
        }

        if (returnError != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnError);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(keys);
        }
        return returnValue;
    }

    @Override
    public void getAllKeys(final StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = getAllKeys();
        notifyCallback(callback, returnValue);
    }

    //Utility methods

    /**
     * Adds the prefix to the key. Beware that this method is dumb and
     * does not check if the key is already prefixed or not.
     * @param key Key to sanitize.
     * @return    Prefix and key.
     */
    private String sanitizeKey(String key)  {
        return key == null ? key : prefix + key;
    }

    private void validateInputs(String key, byte[] value) throws StorageException {
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }

        key = sanitizeKey(key);
        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }

        if (value == null) {
            throw new StorageException(StorageException.INVALID_INPUT_VALUE);
        } else if (value.length > MAX_DATA_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_DATA_SIZE_LIMIT_EXCEEDED);
        }
    }

    private void validateInputs(String key, String value) throws StorageException {
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }
        key = sanitizeKey(key);
        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }

        if (value == null) {
            throw new StorageException(StorageException.INVALID_INPUT_VALUE);
        }

        byte[] byteData;
        try {
            byteData = value.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new StorageException(StorageException.UNSUPPORTED_DATA);
        }

        if (byteData.length > MAX_DATA_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_DATA_SIZE_LIMIT_EXCEEDED);
        }
    }

    private void notifyCallback(StorageResultReceiver callback, StorageResult result) {
        if (callback == null && DEBUG) Log.w(TAG, "No KeyStoreStorage callback set.");

        if (callback != null) {
            try {
                callback.send(result);
            } catch (Exception e) {
                //ignored purposefully as its the callers responsibility to handle errors in the callback
                if (DEBUG) Log.w(TAG, "KeyStoreStorage threw exception: ", e);
            }
        }
    }

    private void checkForError(boolean success) throws StorageException {
        if (!success) {
            int code = ks.getLastError();

            if (code == KeyStore.NO_ERROR) {
        /*
        Code is a work around: Most of the KeyStore methods, say put(), get() etc.. don't update
        the last error attribute (mError) and hence getLastError will always return NO_ERROR. The
        below code would atleast check if the error was caused due to the KeyStore being locked,
        as its slightly better that just giving the caller a generic UNKNOWN_ERROR.
        */

                if (!ks.isUnlocked()) {
                    throw new StorageException(StorageException.STORE_NOT_UNLOCKED);
                } else {
                    throw new StorageException(StorageException.UNKNOWN_ERROR);
                }

            } else {
                String errorStr = rcToStr(code);
                if (DEBUG) Log.d(TAG, "last error = " + errorStr);
                throw new StorageException("KeyStore error: " + errorStr, null, StorageException.OPERATION_FAILED);
            }


        }
    }

    private String rcToStr(int rc) {
        switch (rc) {
            case KeyStore.NO_ERROR:
                return "NO_ERROR";
            case KeyStore.LOCKED:
                return "LOCKED";
            case KeyStore.UNINITIALIZED:
                return "UNINITIALIZED";
            case KeyStore.SYSTEM_ERROR:
                return "SYSTEM_ERROR";
            case KeyStore.PROTOCOL_ERROR:
                return "PROTOCOL_ERROR";
            case KeyStore.PERMISSION_DENIED:
                return "PERMISSION_DENIED";
            case KeyStore.KEY_NOT_FOUND:
                return "KEY_NOT_FOUND";
            case KeyStore.VALUE_CORRUPTED:
                return "VALUE_CORRUPTED";
            case KeyStore.UNDEFINED_ACTION:
                return "UNDEFINED_ACTION";
            case KeyStore.WRONG_PASSWORD:
                return "WRONG_PASSWORD";
            default:
                return "Unknown RC";
        }
    }

    private boolean isReady(StorageResult result){
        try {
            if(!ks.isUnlocked() ){
                result.setStatus(StorageResult.StorageOperationStatus.FAILURE);
                result.setData(new StorageException(StorageException.STORE_NOT_UNLOCKED));
                return false;
            }
            return true;
        } catch (Exception e) {
            result.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            result.setData(new StorageException(StorageException.UNKNOWN_ERROR));
            return false;
        }
    }
}
