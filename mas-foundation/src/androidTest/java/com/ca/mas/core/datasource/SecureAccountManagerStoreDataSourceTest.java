/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

import android.os.Build;

import com.ca.mas.AndroidVersionAwareTestRunner;
import com.ca.mas.TargetApi;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidVersionAwareTestRunner.class)
public class SecureAccountManagerStoreDataSourceTest extends AccountManagerStoreDataSourceTest{

    @Override
    protected DataSource<?, ?> getDataSource(DataConverter dataConverter) {
        return DataSourceFactory.getStorage(getContext(),
                SecureAccountManagerStoreDataSource.class, param, dataConverter);
    }


    @After
    public void cleanupData() {
        super.cleanupData();
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testReadWriteString() {
        super.testReadWriteString();
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testReadWriteByteArray() {
       super.testReadWriteByteArray();
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testReadWriteGeneric() {
        super.testReadWriteGeneric();
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testGetKeys() {
        super.testGetKeys();
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testRemove() {
       super.testRemove();
    }

}
