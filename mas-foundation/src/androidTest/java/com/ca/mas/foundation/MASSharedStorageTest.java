package com.ca.mas.foundation;

import android.accounts.Account;
import android.accounts.AccountManager;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;

import org.junit.After;
import org.junit.Test;

import java.util.Arrays;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASSharedStorageTest extends MASLoginTestBase {

    private final String accountName = "testName";
    // Currently matches the value in massharedauthenticator.xml
    private final String accountType = "com.mas.foundation.sharedstorage";

    @Test
    public void testStorageCreation() {
        MASSharedStorage storage = new MASSharedStorage(accountName);

        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(accountType);
        assertEquals(1, accounts.length);
    }

    @Test
    public void testStorageCreationMissingParameters() {
        try {
            MASSharedStorage storage = new MASSharedStorage(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        try {
            MASSharedStorage storage = new MASSharedStorage("");
            fail();
        } catch (IllegalArgumentException e) {
        }
    }

    @Test
    public void testStorageSaveGetString() throws Exception {
        String key = "testKey123";
        String object = "testValue123";
        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, object);

        String retrievedObject = storage.getString(key);
        assertEquals(retrievedObject, object);
    }

    @Test
    public void testStorageSaveGetBytes() throws Exception {
        String key = "testKey123";
        byte[] bytes = "testValue12345".getBytes();

        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, bytes);

        byte[] retrievedObject = storage.getBytes(key);
        assertTrue(Arrays.equals(retrievedObject, bytes));
    }

    @Test
    public void testStorageDeleteGetString() throws Exception {
        String key = "testKey123";
        String value = "testValue12345";

        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, value);
        storage.delete(key);

        String retrievedString = storage.getString(key);
        assertNull(retrievedString);

        byte[] retrievedData = storage.getBytes(key);
        assertNull(retrievedData);
    }

    @Test
    public void testStorageDeleteGetData() throws Exception {
        String key = "testKey123";
        byte[] bytes = "testValue12345".getBytes();

        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, bytes);
        storage.delete(key);

        byte[] retrievedData = storage.getBytes(key);
        assertNull(retrievedData);

        String retrievedString = storage.getString(key);
        assertNull(retrievedString);
    }

    @Test
    public void testStorageStoreStringWithDifferentConfig() throws Exception {
        String key = "testKey123";
        String object = "testValue123";
        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, object);

        String retrievedObject = storage.getString(key);
        assertEquals(retrievedObject, object);

        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config_dynamic_test.json"));

        String retrievedObject2 = storage.getString(key);
        assertEquals(retrievedObject, retrievedObject2);
    }

    @Test(expected = NullPointerException.class)
    public void testStorageErrorSaveNullKey() {
        MASSharedStorage storage = new MASSharedStorage(accountName);
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
        MASSharedStorage storage = new MASSharedStorage(accountName);

        try {
            storage.delete(null);
            fail();
        } catch (IllegalArgumentException e) {
        }

        storage.delete("");
        fail();
    }

    @Test(expected = NullPointerException.class)
    public void testStorageErrorRetrieveNullKey() {
        MASSharedStorage storage = new MASSharedStorage(accountName);

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
    public void testStoreStringDataRetrieveAfterDeregistration() throws Exception {
        MASCallbackFuture<MASUser> loginCallback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), loginCallback);

        String key = "testKey123";
        String object = "testValue123";
        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, object);

        String retrievedObject = storage.getString(key);
        assertEquals(retrievedObject, object);

        MASCallbackFuture<Void> callback = new MASCallbackFuture<>();
        MASDevice.getCurrentDevice().deregister(callback);
        callback.get();
        assertFalse(MASDevice.getCurrentDevice().isRegistered());

        String retrievedObject2 = storage.getString(key);
        assertEquals(retrievedObject2, object);
    }

    //Uncomment if keySet() method ever becomes public.
//    @Test
//    public void testStorageKeySetContents() throws Exception {
//        String key = "testKey123";
//        String object = "testValue123";
//        MASSharedStorage storage = new MASSharedStorage(accountName);
//        storage.save(key, object);
//
//        String retrievedObject = storage.getString(key);
//        assertEquals(retrievedObject, object);
//
//        String key2 = "testKey1234";
//        String object2 = "testValue1234";
//        storage.save(key2, object2);
//
//        String retrievedObject2 = storage.getString(key2);
//        assertEquals(retrievedObject2, object2);
//
//        Set<String> keys = storage.getKeySet();
//        assertEquals(keys.size(), 2);
//    }

    @After
    public void resetAccountsAndData() {
        AccountManager am = AccountManager.get(getContext());
        Account[] accounts = am.getAccountsByType(accountType);
        for (Account account : accounts) {
            am.removeAccountExplicitly(account);
        }
    }

}
