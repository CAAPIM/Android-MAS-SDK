package com.ca.mas.foundation;

import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;

import org.junit.Test;

import static junit.framework.Assert.fail;

public class MASSharedStorageSDKStopTest {

    private final String accountName = "testName";

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorSaveNonInitializedSdk() {
        String key = "testKey123";
        String object = "testValue123";

        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, object);
    }

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorDeleteNonInitializedSdk() {
        String key = "testKey123";

        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.delete(key);
        fail();
    }

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorRetrieveNonInitializedSdk() {
        String key = "testKey123";
        MASSharedStorage storage = new MASSharedStorage(accountName);

        try {
            storage.getString(key);
            fail();
        } catch (IllegalStateException e) {
        }

        storage.getBytes(key);
        fail();
    }
}
