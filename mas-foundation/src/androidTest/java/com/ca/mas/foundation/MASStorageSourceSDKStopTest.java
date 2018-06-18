package com.ca.mas.foundation;

import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.storage.storagesource.MASStorageSource;

import org.junit.After;
import org.junit.Test;

import static junit.framework.Assert.fail;

public class MASStorageSourceSDKStopTest extends MASStartTestBase {

    private final String accountName = "testName";
    // Currently matches the value in massharedauthenticator.xml
    private final String accountType = "com.mas.foundation.sharedstorage";

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorSaveNonInitializedSdk() {
        masStop();
        String key = "testKey123";
        String object = "testValue123";

        MASStorageSource storage = new MASStorageSource(accountName, true);
        storage.save(key, object);
    }

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorDeleteNonInitializedSdk() throws Exception {
        masStop();
        String key = "testKey123";

        MASStorageSource storage = new MASStorageSource(accountName, true);
        storage.delete(key);
        fail();
    }

    @Test(expected = IllegalStateException.class)
    public void testStorageErrorRetrieveNonInitializedSdk() throws Exception {
        masStop();
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

    @After
    public void restartSDK() throws Exception {
        masStart();
    }
}
