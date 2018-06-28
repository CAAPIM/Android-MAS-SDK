/*
 * Copyright (c) 2017 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.storage.MASSecureSharedStorage;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;

import org.junit.After;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASSecureSharedStorageTest extends MASLoginTestBase {

    private final String accountName = "testNameEncrypted";
    private final String accountNameb = "testNameNoEncrypted";
    private final int TIME = 3500;

    private final String accountType = "com.ca.mas.testSecureAccountType";
    String value = "Test value";
    String keyName = "key";

    @Test
    public void testStorageCreation() {
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);

        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(accountType);
        assertEquals(1, accounts.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorageCreationMissingParameters() {
        MASSecureSharedStorage storage = new MASSecureSharedStorage(null, true, true, true);
        MASSecureSharedStorage storageShared = new MASSecureSharedStorage("", true, true, true);
    }

    @Test
    public void testStorageSaveGetString() {
        String key = "testKey123";
        String object = "testValue123";
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);
        storage.save(key, object);

        MASSecureSharedStorage storageb = new MASSecureSharedStorage(accountNameb, false, true, true);
        storageb.save(key,object);

        String retrievedObject = storage.getString(key);
        String retrievedObjectb = storageb.getString(key);

        assertEquals(retrievedObject, retrievedObjectb);
    }

    @Test
    public void testStorageSaveGetBytes() {
        String key = "testKey123";
        byte[] bytes = "testValue12345".getBytes();

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);
        storage.save(key, bytes);

        MASSecureSharedStorage storageb = new MASSecureSharedStorage(accountNameb, false, true, true);
        storageb.save(key, bytes);


        byte[] retrievedObject = storage.getBytes(key);
        byte[] retrievedObjectb = storageb.getBytes(key);

        assertTrue(Arrays.equals(retrievedObject, retrievedObjectb));
    }

    @Test
    public void testStorageDeleteGetString() {
        String key = "testKey123";
        String value = "testValue12345";

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);
        storage.save(key, value);
        storage.delete(key);

        MASSecureSharedStorage storageb = new MASSecureSharedStorage(accountNameb, true, true, true);
        storageb.save(key, value);
        storageb.delete(key);

        String retrievedString = storage.getString(key);
        assertNull(retrievedString);

        String retriveStringb = storageb.getString(key);
        assertNull(retriveStringb);

        byte[] retrievedData = storage.getBytes(key);
        assertNull(retrievedData);

        byte[] retrievedDatab = storageb.getBytes(key);
        assertNull(retrievedDatab);
    }


    @Test(expected = IllegalArgumentException.class)
    public void testStorageErrorSaveNullKey() {
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);
        String data = "testValue12345";
        byte[] bytes = "testValue12345".getBytes();

        try {
            storage.save(null, data);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            storage.save(null, bytes);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            storage.save("", data);
            fail();
        } catch (NullPointerException e) {
        }

        storage.save("", bytes);
        fail();
    }

    @Test
    public void testOtherAccountName() {
        String key = "testKey123";
        String value = "testValue12345";


        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);
        storage.save(key, value);

        MASSecureSharedStorage storageb = new MASSecureSharedStorage(accountNameb, true, true, true);
        String retValue = storageb.getString(key);

        assertNull(retValue);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorageErrorSaveNullKeyFalseMode() {
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, false, true, true);
        String data = "testValue12345";
        byte[] bytes = "testValue12345".getBytes();

        try {
            storage.save(null, data);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            storage.save(null, bytes);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            storage.save("", data);
            fail();
        } catch (NullPointerException e) {
        }

        storage.save("", bytes);
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorageErrorDeleteNullKey() {
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName,true, true, true);

        try {
            storage.delete(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        storage.delete("");
        fail();

        MASSecureSharedStorage storageb = new MASSecureSharedStorage(accountName,false, true, true);

        try {
            storageb.delete(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        storageb.delete("");
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorageErrorRetrieveNullKey() {
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName,true, true, true);

        try {
            storage.getString(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            storage.getBytes(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            storage.getString("");
            fail();
        } catch (NullPointerException e) {
        }

        storage.getBytes("");
        fail();
    }

    @Test
    public void saveKeyEncryptedRetrieveUnEncrypted() {
        String key = "testKey123";
        String value = "testValue12345";
        String comparea;
        String compareb;

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);
        storage.save(key, value);
        comparea = storage.getString(key);

        MASSecureSharedStorage storageb = new MASSecureSharedStorage(accountName, false, true, true);
        compareb = storageb.getString(key);

        if (!comparea.equals(compareb)) {
            assert(true);
        }
        else {
            assert(false);
        }
    }


    @Test(expected = RuntimeException.class)
    public void saveKeyUnencryptedRetrieveEncrypted() {
        String key = "testKey123";
        String value = "testValue12345";
        String comparea;
        String compareb;

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, false, true, true);
        storage.save(key, value);
        comparea = storage.getString(key);

        MASSecureSharedStorage storageb = new MASSecureSharedStorage(accountName, true, true, true);
        compareb = storageb.getString(key);

        assertEquals(comparea,compareb);
    }

    @Test
    public void SecureVSSharedStorageNoConflic() {

        String key = "testKey123";
        String value = "testValue12345";
        String comparea;
        String compareb;

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, false, true, true);
        storage.save(key, value);
        comparea = storage.getString(key);

        MASSecureSharedStorage storageb = new MASSecureSharedStorage(accountNameb, true, true, true);
        storageb.save(key,value);
        compareb = storageb.getString(key);

        assertEquals(comparea, compareb);
    }

    @Test
    public void testGetAllKeys() {
        List<String> result;

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);
        String v1 = "value1";
        String v2 = "value2";
        String v3 = "value3";


        storage.save("key1",v1);
        assertNotNull(storage.getString("key1"));
        assertEquals(storage.getString("key1"), v1);

        storage.save("key2", v2);
        assertNotNull(storage.getString("key2"));
        assertEquals(storage.getString("key2"), v2);

        storage.save("key3", v3);
        assertNotNull(storage.getString("key3"));
        assertEquals(storage.getString("key3"), v3);


        result = storage.getKeys();
        assertNotNull(result);
        assertEquals(result.size(), 3);
    }

    @Test
    public void testRemoveAll() {

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);
        String v1 = "value1";
        String v2 = "value2";
        String v3 = "value3";


        storage.save("key1",v1);
        assertNotNull(storage.getString("key1"));
        assertEquals(storage.getString("key1"), v1);

        storage.save("key2", v2);
        assertNotNull(storage.getString("key2"));
        assertEquals(storage.getString("key2"), v2);

        storage.save("key3", v3);
        assertNotNull(storage.getString("key3"));
        assertEquals(storage.getString("key3"), v3);

        storage.removeAll();
    }

    @Test
    public void loadEncryptedACTestString() {


        // - Encrypted, shared, AccountManager
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);

        long totalTime = storageString(storage);

        if (totalTime > TIME*1.5) {
            fail();
        }
    }

    private long storageString(MASSharedStorage storage) {
        long startTime = System.nanoTime();
        for(int i = 0; i<100 ; i++ ){
            keyName = keyName + i;
            storage.save(keyName, value);
            keyName = "key";
        }
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;

        // - nano to miliseconds
        totalTime = totalTime/1000000;
        return totalTime;
    }

    private long storageBytes(MASSharedStorage storage) {
        long startTime = System.nanoTime();
        for(int i = 0; i<100 ; i++ ){
            keyName = keyName + i;
            storage.save(keyName, value.getBytes());
            keyName = "key";
        }
        long endTime   = System.nanoTime();
        long totalTime = endTime - startTime;

        // - nano to miliseconds
        totalTime = totalTime/1000000;
        return totalTime;
    }

    @Test
    public void loadEncryptedACTestBytes() {

        // - Encrypted, shared, AccountManager
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, true);

        // - nano to miliseconds
        long totalTime = storageBytes(storage);

        if (totalTime > TIME*1.5) {
            fail();
        }
    }

    @Test
    public void loadNotEncryptedACTest() {

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, false, true, true);

        long totalTime = storageString(storage);

        if (totalTime > TIME) {
            fail();
        }
    }

    @Test
    public void loadEncryptedSPTestString() {

        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, false);

        long totalTime = storageString(storage);

        if (totalTime > TIME/2) {
            fail();
        }
    }

    @Test
    public void loadEncryptedSPTestBytes() {

        // - encrypted, shared, SharedPreferences
        MASSecureSharedStorage storage = new MASSecureSharedStorage(accountName, true, true, false);

        long totalTime = storageBytes(storage);

        if (totalTime > TIME/2) {
            fail();
        }
    }


    @After
    public void resetAccountsAndData() {
        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(accountType);
        for (Account account : accounts) {
            try {
                am.removeAccountExplicitly(account);
            } catch (NoSuchMethodError e) {
                //ignore
            }
        }
    }
}
