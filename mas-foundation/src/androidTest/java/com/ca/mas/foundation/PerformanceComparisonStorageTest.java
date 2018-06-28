/*
 * Copyright (c) 2017 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.storage.MASSecureSharedStorage;
import com.ca.mas.core.storage.Storage;
import com.ca.mas.core.storage.StorageException;
import com.ca.mas.core.storage.implementation.MASStorageManager;

import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

public class PerformanceComparisonStorageTest extends MASLoginTestBase {

    private final String accountName = "testPerformanceEncrypted";
    String keyName = "key";
    String value = "test value";

    @Test
    public void testCompareEncryptedACVSOld() throws StorageException {

        // - Encrypted, shared, AccountManager
        long timeNew = getNewTime(true, true, true);
        long oldTime = getOldTime();

        if (Double.valueOf(timeNew)/Double.valueOf(oldTime) > 1.5) {
            fail();
        }
    }

    @Test
    public void testCompareEncryptedNotSharedACVSOld() throws StorageException {

        // - Encrypted, not shared, AccountManager
        long timeNew = getNewTime(true, false, true);
        long oldTime = getOldTime();

        if (Double.valueOf(timeNew)/Double.valueOf(oldTime) > 1.5) {
            fail();
        }
    }

    @Test
    public void testCompareNotEncryptedSharedACVSOld() throws StorageException {

        // - not Encrypted, shared, AccountManager
        long timeNew = getNewTime(false, true, true);
        long oldTime = getOldTime();

        if (timeNew > oldTime) {
            fail();
        }
    }

    @Test
    public void testCompareNotEncryptedNotSharedACVSOld() throws StorageException {

        // - not Encrypted, not shared, AccountManager
        long timeNew = getNewTime(false, false, true);
        long oldTime = getOldTime();

        if (timeNew > oldTime) {
            fail();
        }
    }

    @Test
    public void testCompareNotEncryptedACVSOld() throws StorageException {

        // - not encrypted, shared, AccountManager
        long timeNew = getNewTime(false, true, true);
        long oldTime = getOldTime();

        if (timeNew > oldTime) {
             fail();
        }
    }

    @Test
    public void testCompareNotEncryptedSPVSOld() throws StorageException {

        // - not encrypted, not shared, SharedPreferences
        long timeNew = getNewTime(false, false, false);
        long oldTime = getOldTime();

        if (timeNew > oldTime) {
            fail();
        }

    }

    @Test
    public void testCompareEncryptedSPVSOld() throws StorageException {

        // - encrypted, not shared, SharedPreferences
        long timeNew = getNewTime(true, false, false);
        long oldTime = getOldTime();

        if (timeNew > oldTime) {
            fail();
        }
    }

    @Test
    public void testCompareNotEncryptedSharedSPVSOld() throws StorageException {

        // - encrypted, shared, SharedPreferences
        long timeNew = getNewTime(true, true, false);
        long oldTime = getOldTime();

        if (timeNew > oldTime) {
            fail();
        }
    }

    private long getNewTime(boolean secure, boolean shared, boolean storageMode) {
        MASSecureSharedStorage newStorage = new MASSecureSharedStorage(accountName, secure, shared, storageMode);

        long newStartTime = System.nanoTime();
        for(int i = 0; i<100 ; i++ ){
            keyName = keyName + i;
            newStorage.save(keyName, value);
            String retValue = newStorage.getString(keyName);
            assertEquals(value, retValue);
            newStorage.delete(keyName);
            keyName = "key";
        }
        long newEndTime   = System.nanoTime();
        long newTotalTime = newEndTime - newStartTime;

        newTotalTime = newTotalTime/1000000;

        return newTotalTime;
    }

    private long getOldTime() throws StorageException {
        Storage oldStorage = new MASStorageManager().getStorage(MASStorageManager.MASStorageType.TYPE_AMS, new Object[]{getContext(), false});
        long oldStartTime = System.nanoTime();
        for(int i = 0; i<100 ; i++ ){
            keyName = keyName + i;
            oldStorage.writeString(keyName, value);
            String retValue =(String)oldStorage.readString(keyName).getData();
            assertEquals(value, retValue);
            oldStorage.deleteString(keyName);
            keyName = "key";
        }
        long oldEndTime   = System.nanoTime();
        long oldTotalTime = oldEndTime - oldStartTime;

        // - nano to miliseconds
        oldTotalTime = oldTotalTime/1000000;

        return oldTotalTime;
    }
}
