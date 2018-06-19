package com.ca.mas.foundation;

import com.ca.mas.core.storage.storagesource.MASStorageSource;

import org.junit.Test;

import static junit.framework.Assert.fail;

public class MASStorageSourceSDKStopTest {

    private final String accountName = "testName";

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorSaveNonInitializedSdk() {
        String key = "testKey123";
        String object = "testValue123";

        MASStorageSource storage = new MASStorageSource(accountName, true);
        storage.save(key, object);
    }

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorDeleteNonInitializedSdk() throws Exception {
        String key = "testKey123";

        MASStorageSource storage = new MASStorageSource(accountName, true);
        storage.delete(key);
        fail();
    }

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorRetrieveNonInitializedSdk() throws Exception {
        String key = "testKey123";
        MASStorageSource storage = new MASStorageSource(accountName, true);

        try {
            storage.getString(key);
            fail();
        } catch (IllegalStateException e) {
        }

        storage.getBytes(key);
        fail();
    }
}
