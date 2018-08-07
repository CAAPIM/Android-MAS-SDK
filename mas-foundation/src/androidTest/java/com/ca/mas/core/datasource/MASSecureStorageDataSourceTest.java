
/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.os.Build;

import com.ca.mas.AndroidVersionAwareTestRunner;
import com.ca.mas.MinTargetAPI;
import com.ca.mas.MASTestBase;
import com.ca.mas.foundation.MAS;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;


@RunWith(AndroidVersionAwareTestRunner.class)
public class MASSecureStorageDataSourceTest extends MASTestBase {

    JSONObject param = new JSONObject();

    // - same as authenticator_masunit.xml
    private final String accountType = "com.ca.mas.testAccountType";

    public void shareParameter() {
        try {
            param.put("share",true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void shareParameterFalse() {
        try {
            param.put("share",false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Before
    public void startSDK() {
        MAS.start(getContext());
    }

    @After
    public void stopSDK() {
        MAS.stop();
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

    @Test
    @MinTargetAPI(Build.VERSION_CODES.JELLY_BEAN)
    public void testStorageSaveGetStringShared() {
        shareParameter();
        DataSource<String, String> dataSource = DataSourceFactory.getStorage(
                getContext(),
                MASSecureStorageDataSource.class, param, new StringDataConverter());

        String key = "testKey123";
        String object = "testValue123";
        String retObj = "";

        dataSource.put(key, object);
        retObj = dataSource.get(key);

        assertEquals(object, retObj);
    }

    @Test
    @MinTargetAPI(Build.VERSION_CODES.JELLY_BEAN)
    public void testStorageSaveGetByteShared() {
        shareParameter();

        DataSource<String, byte[]> dataSource = DataSourceFactory.getStorage(
                getContext(),
                MASSecureStorageDataSource.class, param, null);

        String key = "testKey123";
        byte[] object = key.getBytes();
        byte[] retObj;

        dataSource.put(key,object);
        retObj = dataSource.get(key);

        assertEquals(key, new String(retObj));
    }

    @Test
    @MinTargetAPI(Build.VERSION_CODES.JELLY_BEAN)
    public void testStorageSaveGetString() {
        shareParameterFalse();
        DataSource<String, String> dataSource = DataSourceFactory.getStorage(
                getContext(),
                MASSecureStorageDataSource.class, param, new StringDataConverter());

        String key = "testKey123";
        String object = "testValue123";
        String retObj = "";

        dataSource.put(key, object);
        retObj = dataSource.get(key);

        assertEquals(object, retObj);
    }

    @Test
    @MinTargetAPI(Build.VERSION_CODES.JELLY_BEAN)
    public void testStorageSaveGetByte() {
        shareParameterFalse();

        DataSource<String, byte[]> dataSource = DataSourceFactory.getStorage(
                getContext(),
                MASSecureStorageDataSource.class, param, null);

        String key = "testKey123";
        byte[] object = key.getBytes();
        byte[] retObj;

        dataSource.put(key,object);
        retObj = dataSource.get(key);

        assertEquals(new String(retObj), new String(object));
    }

}
