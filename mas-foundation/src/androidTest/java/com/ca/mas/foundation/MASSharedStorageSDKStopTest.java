package com.ca.mas.foundation;

import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;

import org.junit.After;
import org.junit.Test;

import static junit.framework.Assert.fail;

public class MASSharedStorageSDKStopTest extends MASStartTestBase {

    private final String accountName = "testName";
    // Currently matches the value in massharedauthenticator.xml
    private final String accountType = "com.mas.foundation.sharedstorage";

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorSaveNonInitializedSdk() {
        masStop();
        String key = "testKey123";
        String object = "testValue123";

        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.save(key, object);
    }

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorDeleteNonInitializedSdk() throws Exception {
        masStop();
        String key = "testKey123";

        MASSharedStorage storage = new MASSharedStorage(accountName);
        storage.delete(key);
        fail();
    }

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorRetrieveNonInitializedSdk() throws Exception {
        masStop();
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

    @After
    public void restartSDK() throws Exception {
        masStart();
    }
}
