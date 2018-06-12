package com.ca.mas.foundation;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.storage.securesharedstorage.SecureSharedStorage;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;

import org.junit.After;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class SecureSharedStorageTest extends MASLoginTestBase {

    private final String accountName = "testNameEncrypted";
    private final String accountNameb = "testNameNoEncripted";

    private final String accountType = "com.mas.foundation.sharedstorage";

    @Test
    public void testStorageCreation() {
        SecureSharedStorage storage = new SecureSharedStorage(accountName, true);

        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(accountType);
        assertEquals(0, accounts.length);
    }

    @Test
    public void testStorageCreationMissingParameters() {
        try {
            SecureSharedStorage storage = new SecureSharedStorage(null, true);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            SecureSharedStorage storage = new SecureSharedStorage("", true);
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testStorageSaveGetString() throws Exception {
        String key = "testKey123";
        String object = "testValue123";
        SecureSharedStorage storage = new SecureSharedStorage(accountName, true);
        storage.save(key, object);

        SecureSharedStorage storageb = new SecureSharedStorage(accountNameb, false);
        storageb.save(key,object);

        String retrievedObject = storage.getString(key);
        String retrievedObjectb = storageb.getString(key);

        assertEquals(retrievedObject, retrievedObjectb);
    }

    @Test
    public void testStorageSaveGetBytes() throws Exception {
        String key = "testKey123";
        byte[] bytes = "testValue12345".getBytes();

        SecureSharedStorage storage = new SecureSharedStorage(accountName, true);
        storage.save(key, bytes);

        SecureSharedStorage storageb = new SecureSharedStorage(accountNameb, false);
        storageb.save(key, bytes);


        byte[] retrievedObject = storage.getBytes(key);
        byte[] retrievedObjectb = storageb.getBytes(key);

        assertTrue(Arrays.equals(retrievedObject, retrievedObjectb));
    }

    @Test
    public void testStorageDeleteGetString() throws Exception {
        String key = "testKey123";
        String value = "testValue12345";

        SecureSharedStorage storage = new SecureSharedStorage(accountName, true);
        storage.save(key, value);
        storage.delete(key);

        SecureSharedStorage storageb = new SecureSharedStorage(accountNameb, true);
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


    @Test(expected = NullPointerException.class)
    public void testStorageErrorSaveNullKey() {
        SecureSharedStorage storage = new SecureSharedStorage(accountName, true);
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


        SecureSharedStorage storage = new SecureSharedStorage(accountName, true);
        storage.save(key, value);

        SecureSharedStorage storageb = new SecureSharedStorage(accountNameb, true);
        String retValue = storageb.getString(key);

        assertNull(retValue);

    }

    @Test(expected = NullPointerException.class)
    public void testStorageErrorSaveNullKeyFalseMode() {
        SecureSharedStorage storage = new SecureSharedStorage(accountName, false);
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

    @Test(expected = NullPointerException.class)
    public void testStorageErrorDeleteNullKey() {
        SecureSharedStorage storage = new SecureSharedStorage(accountName,true);

        try {
            storage.delete(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        storage.delete("");
        fail();

        SecureSharedStorage storageb = new SecureSharedStorage(accountName,false);

        try {
            storageb.delete(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        storageb.delete("");
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void testStorageErrorRetrieveNullKey() {
        SecureSharedStorage storage = new SecureSharedStorage(accountName,true);

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
    public void saveKeyEncriptedRetrieveUnEncripted() {
        String key = "testKey123";
        String value = "testValue12345";
        String comparea = "";
        String compareb = "";

        SecureSharedStorage storage = new SecureSharedStorage(accountName, true);
        storage.save(key, value);
        comparea = storage.getString(key);

        SecureSharedStorage storageb = new SecureSharedStorage(accountName, false);
        compareb = storageb.getString(key);

        if (!comparea.equals(compareb)) {
            assert(true);
        }
        else {
            assert(false);
        }
    }


    @Test(expected = RuntimeException.class)
    public void saveKeyUnencriptedRetrieveEncripted() {
        String key = "testKey123";
        String value = "testValue12345";
        String comparea = "";
        String compareb = "";

        SecureSharedStorage storage = new SecureSharedStorage(accountName, false);
        storage.save(key, value);
        comparea = storage.getString(key);

        SecureSharedStorage storageb = new SecureSharedStorage(accountName, true);
        compareb = storageb.getString(key);
    }

    @Test
    public void SecureVSSharedStorageNoConflic() {

        String key = "testKey123";
        String value = "testValue12345";
        String comparea = "";
        String compareb = "";

        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, value);
        comparea = storage.getString(key);

        SecureSharedStorage storageb = new SecureSharedStorage(accountNameb, true);
        storageb.save(key,value);
        compareb = storageb.getString(key);

        assertEquals(comparea, compareb);
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
