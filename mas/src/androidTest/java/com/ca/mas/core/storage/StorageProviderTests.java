/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage;

import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.MASTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.core.datasource.DataSourceException;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.MAS;

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Tests to verify that the SDK reads the storage configuration(specified in the test_msso_config.json
 * file) properly.
 */
@RunWith(AndroidJUnit4.class)
public class StorageProviderTests extends MASTestBase {

    private static final int TYPE_KEYSTORE = 0;
    private static final int TYPE_AMS = 1;
    private static final int TYPE_INVALID_CLASS = 2;
    private static final int TYPE_MISSING_CLASS = 3;

    @Test
    public void testDefaultDataSource() {
        try {
            MAS.start(getContext(), TestUtils.getJSONObject("/msso_config.json"));
            assertTrue(StorageProvider.getInstance().hasValidStore());
        } catch (Exception e) {
            fail("Did not expect an exception as the default Keystore Storage should have been used. ");
        }
    }


    @Test
    public void testKeystoreDataSource() {
        try {
            JSONObject configO = createMockConfig(TYPE_KEYSTORE);
            MAS.start(getContext(), configO);
            assertTrue(StorageProvider.getInstance().hasValidStore());
        } catch (Exception e) {
            fail("Did not expect an exception for the supported Keystore Storage ");
        }
    }

    @Test
    public void testAMSDataSource() {
        try {
            JSONObject configO = createMockConfig(TYPE_AMS);
            MAS.start(getContext(), configO);
            assertTrue(StorageProvider.getInstance().hasValidStore());
        } catch (Exception e) {
            fail("Did not expect an exception for the supported ELS Storage ");
        }
    }

    @Test(expected = DataSourceException.class)
    public void testInvalidStorageClassConfig() throws Exception {
        JSONObject configO = createMockConfig(TYPE_INVALID_CLASS);
        MAS.start(getContext(), configO);
    }

    @Test(expected = DataSourceException.class)
    public void testMissingStorageClassConfig() throws Exception {
        JSONObject configO = createMockConfig(TYPE_MISSING_CLASS);
        MAS.start(getContext(), configO);
    }


    /**
     * Utility class that mocks JSON configuration for the tests.
     */
    private JSONObject createMockConfig(int type) throws Exception {
        JSONObject jsonObject;
        jsonObject = TestUtils.getJSONObject("/msso_config.json");
        JSONObject temp = new JSONObject();

        switch (type) {
            case TYPE_KEYSTORE:
                temp.put("class", "com.ca.mas.core.datasource.KeystoreDataSource");
                break;
            case TYPE_AMS:
                temp.put("class", "com.ca.mas.core.datasource.AccountManagerStoreDataSource");
                break;
            case TYPE_INVALID_CLASS:
                temp.put("classinvalis", "com.ca.mas.core.datasource.AccountManagerStoreDataSource");
                break;
            case TYPE_MISSING_CLASS:
                break;

        }
        jsonObject.getJSONObject("mag").getJSONObject("mobile_sdk").put("storage", temp);
        return jsonObject;

    }

}
