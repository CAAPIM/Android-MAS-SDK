/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.storage;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.datasource.DataSourceException;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.test.BaseTest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Tests to verify that the SDK reads the storage configuration(specified in the test_msso_config.json
 * file) properly.
 */
@RunWith(AndroidJUnit4.class)
public class StorageProviderTests extends BaseTest {

    private static final String TAG = "StorageProviderTests";

    private static final int TYPE_KEYSTORE = 0;
    private static final int TYPE_AMS = 1;
    private static final int TYPE_TIMA = 2;
    private static final int TYPE_INVALID_CLASS = 3;
    private static final int TYPE_MISSING_CLASS = 4;


    @Override
    public void before() throws Exception {

    }

    @Test
    public void testDefaultDataSource(){
        try {
            mobileSso = MobileSsoFactory.getInstance(InstrumentationRegistry.getInstrumentation().getTargetContext(),
                    getConfig(useMockServer(), "test_msso_config.json"));
            StorageProvider sp = new StorageProvider(InstrumentationRegistry.getInstrumentation().getTargetContext(),mobileSso.getConfigurationProvider());
            assertTrue(sp.hasValidStore());
        }catch (Exception e){
            fail("Did not expect an exception as the default Keystore Storage should have been used. ");
        }
    }


    @Test
    public void testKeystoreDataSource(){
        try {
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            JSONObject configO = createMockConfig(ctx,TYPE_KEYSTORE);
            mobileSso = MobileSsoFactory.getInstance(ctx,configO);
            StorageProvider sp = new StorageProvider(InstrumentationRegistry.getInstrumentation().getTargetContext(),mobileSso.getConfigurationProvider());
            assertTrue(sp.hasValidStore());
        }catch (Exception e){
            fail("Did not expect an exception for the supported Keystore Storage ");
        }
    }

    @Test
    public void testAMSDataSource(){
        try {
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            JSONObject configO = createMockConfig(ctx,TYPE_AMS);
            mobileSso = MobileSsoFactory.getInstance(ctx,configO);
            StorageProvider sp = new StorageProvider(InstrumentationRegistry.getInstrumentation().getTargetContext(),mobileSso.getConfigurationProvider());
            assertTrue(sp.hasValidStore());
        }catch (Exception e){
            fail("Did not expect an exception for the supported ELS Storage ");
        }
    }


    /*
    @Test
    public void testTIMADataSource(){
        try {
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            JSONObject configO = createMockConfig(ctx,TYPE_TIMA);
            mobileSso = MobileSsoFactory.getInstance(ctx,configO);
            StorageProvider sp = new StorageProvider(InstrumentationRegistry.getInstrumentation().getTargetContext(),mobileSso.getConfigurationProvider());
            assertTrue(sp.hasValidStore());
        }catch (Exception e){
            fail("Did not expect an exception for the supported TIMA Storage ");
        }
    }*/

    @Test
    public void testInvalidStorageClassConfig(){
        try {
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            JSONObject configO = createMockConfig(ctx,TYPE_INVALID_CLASS);
            mobileSso = MobileSsoFactory.getInstance(ctx,configO);
            fail("SDk instantiation should have failed, since there is no valid store");
        }catch (Exception e){
            assertTrue(e instanceof DataSourceException);
        }
    }

    @Test
    public void testMissingStorageClassConfig(){
        try {
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            JSONObject configO = createMockConfig(ctx,TYPE_MISSING_CLASS);
            mobileSso = MobileSsoFactory.getInstance(ctx,configO);
            fail("SDk instantiation should have failed, since storage configuration did not have a valid class attribute");
        }catch (Exception e){
            assertTrue(e instanceof DataSourceException);
        }
    }


    /**
     * Utility class that mocks JSON configuration for the tests.
     */
    private JSONObject createMockConfig (Context context, int type){
        String configFileName = "test_msso_config.json";
        StringBuilder jsonConfig = new StringBuilder();
        InputStream is = null;
        try {
            is = context.getAssets().open(configFileName);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                jsonConfig.append(str);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to read Json Configuration file: " + configFileName, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(jsonConfig.toString());
            JSONObject temp = new JSONObject();

            switch (type){
                case TYPE_KEYSTORE:
                    temp.put("class","com.ca.mas.core.datasource.KeystoreDataSource");
                    break;
                case TYPE_AMS:
                    temp.put("class","com.ca.mas.core.datasource.AccountManagerStoreDataSource");
                    break;
                case TYPE_INVALID_CLASS:
                    temp.put("classinvalis","com.ca.mas.core.datasource.AccountManagerStoreDataSource");
                    break;
                case TYPE_MISSING_CLASS:
                    break;

            }
            jsonObject.getJSONObject("mag").getJSONObject("mobile_sdk").put("storage",temp);
            return jsonObject;
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }

    }

}
