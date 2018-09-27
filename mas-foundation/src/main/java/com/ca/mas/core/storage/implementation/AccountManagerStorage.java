/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage.implementation;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.support.annotation.Keep;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.storage.Storage;
import com.ca.mas.core.storage.StorageException;
import com.ca.mas.core.storage.StorageResult;
import com.ca.mas.core.storage.StorageResultReceiver;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/***
 * Android Account based storage implementation. This leverages the {@link AccountManager}
 * to store key value pairs in the Android Accounts Database (inside the "extras" table).
 */
@Keep
public class AccountManagerStorage extends Storage {

    /**
     * Limits for key/value
     */
    private static final int SQLITE_MAX_LENGTH = 1000000000;
    private static final int MAX_DATA_SIZE = SQLITE_MAX_LENGTH;
    private static final int MAX_KEY_SIZE = SQLITE_MAX_LENGTH;

    /**
     * Column name for the key-index
     */
    private static final String KEYINDEX_COLUMN_NAME = "lookup_index";

    /**
     * Key Prefix. The value will be the package name for private mode and
     * "SHARED_" for shared mode
     */
    private String mPrefix = "";

    /**
     * The application context
     */
    private Context mContext;

    /**
     * Handle to the class responsible to marshaling and unmarshaling the key index.
     */
    private AccountIndexFormatter mFormatter;

    /**
     * AMS Manager
     */
    private AMSSManager accountManager;

    /**
     * Initialize the storage.
     *
     * @param options Array of Objects
     *                First object: Context
     *                (Optional) Second: boolean value indicating shared or private space
     * @throws StorageException if any of the mandatory inputs are invalid/null
     *                          or if the initialization of storage fails.
     */
    protected AccountManagerStorage(Object options) throws StorageException {
        super(options);
        try {
            if (options == null || !(options instanceof Object[])) {
                throw new StorageException(StorageException.INVALID_INPUT);
            }
            Object[] inputs = (Object[]) options;
            try {
                mContext = (Context) inputs[0];
                mContext.getPackageName();
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "Missing Context input " + e);
                throw new StorageException(StorageException.INVALID_INPUT);
            }

            try {
                mPrefix = (boolean) inputs[1] ? "SHARED_" : mContext.getPackageName() + "_";
            } catch (Exception e) {
                if (DEBUG) Log.w(TAG, "Wrong shared input attribute, falling back to private" + e);
                //if not specified , assume default as "not shared"
                mPrefix = mContext.getPackageName() + "_";
            }

            accountManager = AMSSManager.getInstance(mContext);

            mFormatter = new AccountIndexFormatter();
        } catch (StorageException bubble) {
            throw bubble;
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Failed to initialize storage", e);
            throw new StorageException(StorageException.INSTANTIATION_ERROR);
        }
    }

    @Override
    public MASStorageManager.MASStorageType getType() {
        return MASStorageManager.MASStorageType.TYPE_AMS;
    }

    private StorageResult writeData(String key, byte[] value, int option) throws StorageException {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.WRITE);
        StorageException returnException = null;
        validateInputs(key, value);
        try {
            byte[] keyBytes = (mPrefix + key).getBytes(UTF8);
            String encodedKey = Base64.encodeToString(keyBytes, Base64.DEFAULT);
            String data = readAccountData(mContext, encodedKey);

            switch (option) {
                case 0://write
                    if (data != null) {
                        returnException = new StorageException(StorageException.WRITE_DATA_ALREADY_EXISTS);
                    }
                    break;
                case 1://update
                    if (data == null) {
                        returnException = new StorageException(StorageException.READ_DATA_NOT_FOUND);
                    }
                    break;
                case 2://write or update
                default:
                    break;
            }

            if (returnException == null) {
                String encodedValue = Base64.encodeToString(value, Base64.DEFAULT);
                writeAccountData(mContext, encodedKey, encodedValue);
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error writing data ", e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }
        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);

        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(key);
        }
        return returnValue;
    }

    @Override
    public StorageResult writeData(String key, byte[] value) throws StorageException {
        return writeData(key, value, 0);
    }

    @Override
    public void writeData(String key, byte[] value, StorageResultReceiver callback) throws StorageException {
        StorageResult result = writeData(key, value);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult writeString(String key, String value) throws StorageException {
        validateInputs(key, value);
        try {
            StorageResult result = writeData(key, value.getBytes(UTF8), 0);
            result.setType(StorageResult.StorageOperationType.WRITE_STRING);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new StorageException(StorageException.INVALID_INPUT);
        }
    }

    @Override
    public void writeString(String key, String value, StorageResultReceiver callback) throws StorageException {
        StorageResult result = writeString(key, value);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult readData(String key) throws StorageException {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.READ);
        StorageException returnException = null;
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }
        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }

        try {
            byte[] keyBytes = (mPrefix + key).getBytes(UTF8);
            String encodedKey = Base64.encodeToString(keyBytes, Base64.DEFAULT);
            String data = readAccountData(mContext, encodedKey);
            if (data == null) {
                returnException = new StorageException(StorageException.READ_DATA_NOT_FOUND);
            } else {
                byte[] retrievedData = Base64.decode(data.getBytes(UTF8), Base64.DEFAULT);
                if (retrievedData == null) {
                    returnException = new StorageException(StorageException.OPERATION_FAILED);
                } else {
                    returnValue.setData(retrievedData);
                }
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error Writing data ", e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }

        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
        }
        return returnValue;
    }

    @Override
    public void readData(String key, StorageResultReceiver callback) throws StorageException {
        StorageResult result = readData(key);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult readString(String key) throws StorageException {
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }
        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }
        StorageResult returnValue = readData(key);
        if (returnValue.getStatus() != StorageResult.StorageOperationStatus.FAILURE) {
            try {
                returnValue.setData(new String((byte[]) returnValue.getData(), UTF8));
            } catch (UnsupportedEncodingException e) {
                if (DEBUG) Log.w(TAG, "UTF-8 decoding of the data failed, reverting to system default");
                returnValue.setData(new String((byte[]) returnValue.getData()));
            }
        }
        returnValue.setType(StorageResult.StorageOperationType.READ_STRING);
        return returnValue;
    }

    @Override
    public void readString(String key, StorageResultReceiver callback) throws StorageException {
        StorageResult result = readString(key);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult updateData(String key, byte[] value) throws StorageException {
        StorageResult result = writeData(key, value, 1);
        result.setType(StorageResult.StorageOperationType.UPDATE);
        return result;
    }

    @Override
    public void updateData(String key, byte[] value, StorageResultReceiver callback) throws StorageException {
        StorageResult result = updateData(key, value);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult updateString(String key, String value) throws StorageException {
        validateInputs(key,value);
        try {
            StorageResult result = writeData(key, value.getBytes(UTF8), 1);
            result.setType(StorageResult.StorageOperationType.UPDATE_STRING);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new StorageException(StorageException.INVALID_INPUT);
        }
    }

    @Override
    public void updateString(String key, String value, StorageResultReceiver callback) throws StorageException {
        StorageResult result = updateString(key, value);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult writeOrUpdateData(String key, byte[] value) throws StorageException {
        StorageResult result = writeData(key, value, 2);
        result.setType(StorageResult.StorageOperationType.WRITE_OR_UPDATE);
        return result;
    }

    @Override
    public void writeOrUpdateData(String key, byte[] value, StorageResultReceiver callback) throws StorageException {
        StorageResult result = writeOrUpdateData(key, value);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult writeOrUpdateString(String key, String value) throws StorageException {
        validateInputs(key, value);
        try {
            StorageResult result = writeOrUpdateData(key, value.getBytes(UTF8));
            result.setType(StorageResult.StorageOperationType.WRITE_OR_UPDATE_STRING);
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new StorageException(StorageException.INVALID_INPUT);
        }
    }

    @Override
    public void writeOrUpdateString(String key, String value, StorageResultReceiver callback) throws StorageException {
        StorageResult result = writeOrUpdateString(key, value);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult deleteData(String key) throws StorageException {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.DELETE);
        StorageException returnException = null;
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }
        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEYSTORE_KEY_SIZE_LIMIT_EXCEEDED);
        }

        try {
            byte[] keyBytes = (mPrefix + key).getBytes(UTF8);
            String encodedKey = Base64.encodeToString(keyBytes, Base64.DEFAULT);
            String data = readAccountData(mContext, encodedKey);
            if (data == null) {
                returnException = new StorageException(StorageException.READ_DATA_NOT_FOUND);
            } else {
                writeAccountData(mContext, encodedKey, null);
            }
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, "Error writing data ", e);
            returnException = new StorageException(StorageException.OPERATION_FAILED);
        }

        if (returnException != null) {
            returnValue.setStatus(StorageResult.StorageOperationStatus.FAILURE);
            returnValue.setData(returnException);
        } else {
            returnValue.setStatus(StorageResult.StorageOperationStatus.SUCCESS);
            returnValue.setData(key);
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
    public void deleteData(String key, StorageResultReceiver callback) throws StorageException {
        StorageResult result = deleteData(key);
        notifyCallback(callback, result);
    }

    @Override
    public void deleteString(String key, StorageResultReceiver callback) throws StorageException {
        StorageResult result = deleteString(key);
        notifyCallback(callback, result);
    }

    @Override
    public StorageResult deleteAll() {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.DELETE_ALL);
        StorageException returnError = null;
        int successCount = 0;
        int failureCount = 0;
        try {
            StorageResult keyQuery = getAllKeys();
            ArrayList<String> keys = (ArrayList<String>) keyQuery.getData();
            for (String currentKey : keys) {
                try {
                    StorageResult deleteQuery = deleteData(currentKey);
                    if (deleteQuery.getStatus() == StorageResult.StorageOperationStatus.FAILURE) {
                        failureCount++;
                    } else {
                        successCount++;
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
            if (DEBUG) Log.e(TAG, "deleteAll failed ", e);
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
    public void deleteAll(StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = deleteAll();
        notifyCallback(callback, returnValue);
    }

    @Override
    public StorageResult getAllKeys() {
        StorageResult returnValue = new StorageResult(StorageResult.StorageOperationType.GET_ALL_KEYS);
        StorageException returnError = null;
        ArrayList<String> keys = new ArrayList<>();
        try {
            String keyBlob = readAccountData(mContext, "lookup_index");
            keys = mFormatter.unmarshal(keyBlob, true);
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
    public void getAllKeys(StorageResultReceiver callback) throws StorageException {
        StorageResult returnValue = getAllKeys();
        notifyCallback(callback, returnValue);
    }

    //Utility methods

    private void notifyCallback(StorageResultReceiver callback, StorageResult result) {
        if (callback == null && DEBUG) Log.w(TAG, "No AccountManagerStorage callback set.");

        if (callback != null) {
            try {
                callback.send(result);
            } catch (Exception e) {
                if (DEBUG) Log.w(TAG, "AccountManagerStorage threw exception: ", e);
            }
        }
    }

    private void updateAccountKeyIndex(Context ctx, String key, String value) throws Exception {
        AccountManager am = AccountManager.get(ctx);
        Account account = accountManager.getAccount();
        String keyBlob = readAccountData(ctx, KEYINDEX_COLUMN_NAME);
        //ArrayList<String> keyList = keyBlob==null?new ArrayList<String>():new ArrayList<String>(Arrays.asList(keyBlob.split(ACCOUNT_KEY_SEPERATOR)));
        ArrayList<String> keyList = mFormatter.unmarshal(keyBlob, false);
        if (value == null) {
            //DELETE
            if (keyList.contains(key)) {
                keyList.remove(key);
            }
        } else {
            //WRITE
            if (!keyList.contains(key)) {
                keyList.add(key);
            }
        }
        keyBlob = mFormatter.marshal(keyList, false);
        am.setUserData(account, KEYINDEX_COLUMN_NAME, keyBlob);
    }

    private void writeAccountData(Context ctx, String encodedKey, String value) throws Exception {
        AccountManager am = AccountManager.get(ctx);
        Account account = accountManager.getAccount();
        if (value == null) {//DELETE
            updateAccountKeyIndex(ctx, encodedKey, null);
            am.setUserData(account, encodedKey, null);
        } else {//WRITE or UPDATE
            am.setUserData(account, encodedKey, value);
            updateAccountKeyIndex(ctx, encodedKey, value);
        }
    }

    private String readAccountData(Context ctx,String key) throws Exception {
        AccountManager am = AccountManager.get(ctx);
        Account account = accountManager.getAccount();
        return am.getUserData(account, key);
    }

    /**
     * This methods checks for cases such as null or min and max length of the key and data.
     *
     * @param key   the key to put in the store
     * @param value the value corresponding to the key to be put in the store
     * @throws StorageException
     */

    private void validateInputs(String key, String value) throws StorageException {
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        }

        if (value == null) {
            throw new StorageException(StorageException.INVALID_INPUT_VALUE);
        }

        if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEY_SIZE_LIMIT_EXCEEDED);
        }

        byte[] byteData;
        try {
            byteData = value.getBytes(UTF8);
        } catch (UnsupportedEncodingException e) {
            throw new StorageException(StorageException.UNSUPPORTED_DATA);
        }

        if (byteData.length > MAX_DATA_SIZE) {
            throw new StorageException(StorageException.DATA_SIZE_LIMIT_EXCEEDED);
        }
    }

    /**
     * This methods checks for cases such as null or min and max length of the key and data.
     *
     * @param key   the key to put in the store
     * @param value the value corresponding to the key to be put in the store
     * @throws StorageException
     */

    private void validateInputs(String key, byte[] value) throws StorageException {
        if (key == null) {
            throw new StorageException(StorageException.INVALID_INPUT_KEY);
        } else if (key.length() > MAX_KEY_SIZE) {
            throw new StorageException(StorageException.KEY_SIZE_LIMIT_EXCEEDED);
        }

        if (value == null) {
            throw new StorageException(StorageException.INVALID_INPUT_VALUE);
        } else if (value.length > MAX_DATA_SIZE) {
            throw new StorageException(StorageException.DATA_SIZE_LIMIT_EXCEEDED);
        }
    }

    /**
     * Class responsible for marshalling and unmarshalling the key index.
     */
    private class AccountIndexFormatter {

        private static final String ACCOUNT_KEY_SEPARATOR = ":";

        /**
         * String representation of the ArrayList<String>.
         * @param items
         * @param encode
         * @return
         */
        private String marshal(ArrayList<String> items, boolean encode) {
            if (items == null || items.size() == 0) {
                return "";
            }
            try {
                StringBuffer buff = new StringBuffer();
                for (String keyElement : items) {
                    String modifiedKey = keyElement;
                    if (encode) {
                        modifiedKey = Base64.encodeToString((mPrefix + keyElement).getBytes(UTF8), Base64.DEFAULT);
                    }
                    buff.append(modifiedKey);
                    buff.append(ACCOUNT_KEY_SEPARATOR);
                }
                String keyBlob = buff.toString();
                keyBlob = keyBlob.endsWith(ACCOUNT_KEY_SEPARATOR) ? keyBlob.substring(0, keyBlob.length() - 1) : keyBlob;
                return keyBlob;
            } catch (Exception e) {
                if (DEBUG) Log.e(TAG, "Error in marshal: " + e);
                return "";
            }
        }

        /**
         * Creates ArrayList<String> out of the String blob.
         * @param blob
         * @param decode if you want to Base64 decode the items
         * @return
         */
        private ArrayList<String> unmarshal(String blob, boolean decode) {
            ArrayList<String> keys = new ArrayList<>();
            if (blob == null || blob.length() == 0) {
                return keys;
            }

            try {
                ArrayList<String> temp = new ArrayList<String>(Arrays.asList(blob.split(ACCOUNT_KEY_SEPARATOR)));
                if (decode) {
                    for (String key : temp) {
                        byte[] decodedData = Base64.decode(key.getBytes(UTF8), Base64.DEFAULT);
                        if (decodedData != null) {
                            String sanitizedKey = new String((byte[]) decodedData, UTF8);
                            if (sanitizedKey.startsWith(mPrefix)) {
                                keys.add(sanitizedKey.substring(mPrefix.length()));
                            }
                        } else {
                            if (DEBUG) Log.w(TAG, "Unable to process key retrieved from store");
                        }
                    }
                } else {
                    keys = temp;
                }
            } catch (UnsupportedEncodingException e) {
                if (DEBUG) Log.e(TAG, "Error in unmarshal: " + e);
            }

            return keys;
        }
    }

}

