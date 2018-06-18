package com.ca.mas.foundation;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.storage.securestoragesource.MASSecureStorageSource;

import org.junit.After;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASSecureStorageSourceTest extends MASLoginTestBase {

    private final String accountName = "testNameEncrypted";
    private final String accountNameb = "testNameNoEncrypted";

    private final String accountType = "com.mas.foundation.sharedstorage";

    @Test
    public void testStorageCreation() {
        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, true, true);

        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(accountType);
        assertEquals(1, accounts.length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorageCreationMissingParameters() {
        MASSecureStorageSource storage = new MASSecureStorageSource(null, true, true);
        MASSecureStorageSource storageShared = new MASSecureStorageSource("", true, true);
    }

    @Test
    public void testStorageSaveGetString() throws Exception {
        String key = "testKey123";
        String object = "testValue123";
        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, true, true);
        storage.save(key, object);

        MASSecureStorageSource storageb = new MASSecureStorageSource(accountNameb, false, true);
        storageb.save(key,object);

        String retrievedObject = storage.getString(key);
        String retrievedObjectb = storageb.getString(key);

        assertEquals(retrievedObject, retrievedObjectb);
    }

    @Test
    public void testStorageSaveGetBytes() throws Exception {
        String key = "testKey123";
        byte[] bytes = "testValue12345".getBytes();

        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, true, true);
        storage.save(key, bytes);

        MASSecureStorageSource storageb = new MASSecureStorageSource(accountNameb, false, true);
        storageb.save(key, bytes);


        byte[] retrievedObject = storage.getBytes(key);
        byte[] retrievedObjectb = storageb.getBytes(key);

        assertTrue(Arrays.equals(retrievedObject, retrievedObjectb));
    }

    @Test
    public void testStorageDeleteGetString() throws Exception {
        String key = "testKey123";
        String value = "testValue12345";

        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, true, true);
        storage.save(key, value);
        storage.delete(key);

        MASSecureStorageSource storageb = new MASSecureStorageSource(accountNameb, true, true);
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
        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, true, true);
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


        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, true, true);
        storage.save(key, value);

        MASSecureStorageSource storageb = new MASSecureStorageSource(accountNameb, true, true);
        String retValue = storageb.getString(key);

        assertNull(retValue);

    }

    @Test(expected = IllegalArgumentException.class)
    public void testStorageErrorSaveNullKeyFalseMode() {
        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, false, true);
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
        MASSecureStorageSource storage = new MASSecureStorageSource(accountName,true, true);

        try {
            storage.delete(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        storage.delete("");
        fail();

        MASSecureStorageSource storageb = new MASSecureStorageSource(accountName,false, true);

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
        MASSecureStorageSource storage = new MASSecureStorageSource(accountName,true, true);

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
        String comparea = "";
        String compareb = "";

        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, true, true);
        storage.save(key, value);
        comparea = storage.getString(key);

        MASSecureStorageSource storageb = new MASSecureStorageSource(accountName, false, true);
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
        String comparea = "";
        String compareb = "";

        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, false, true);
        storage.save(key, value);
        comparea = storage.getString(key);

        MASSecureStorageSource storageb = new MASSecureStorageSource(accountName, true, true);
        compareb = storageb.getString(key);
    }

    @Test
    public void SecureVSSharedStorageNoConflic() {

        String key = "testKey123";
        String value = "testValue12345";
        String comparea = "";
        String compareb = "";

        MASSecureStorageSource storage = new MASSecureStorageSource(accountName, false, true);
        storage.save(key, value);
        comparea = storage.getString(key);

        MASSecureStorageSource storageb = new MASSecureStorageSource(accountNameb, true, true);
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
