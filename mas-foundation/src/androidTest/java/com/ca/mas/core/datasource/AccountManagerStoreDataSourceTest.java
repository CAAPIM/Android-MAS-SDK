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
import com.ca.mas.MASTestBase;
import com.ca.mas.TargetApi;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidVersionAwareTestRunner.class)
public class AccountManagerStoreDataSourceTest extends MASTestBase {

    private final String KEY = "KEY";
    private final String KEY2 = "KEY2";
    private final String VALUE = "VALUE";
    private final String VALUE2 = "VALUE2";

    JSONObject param = null;


    @After
    public void cleanupData() {
        if (isSkipped) return;
        DataSource<String, String> d = DataSourceFactory.getStorage(getContext(),
                AccountManagerStoreDataSource.class, param, null);
        d.remove(KEY);
        d.remove(KEY2);
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testReadWriteString() {
        DataSource<String, String> d = (DataSource<String, String>) getDataSource(new StringDataConverter());
        d.put(KEY, VALUE);
        assertEquals(VALUE, d.get(KEY));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testReadWriteByteArray() {
        DataSource<String, byte[]> d = (DataSource<String, byte[]>) getDataSource( null );
        d.put(KEY, VALUE.getBytes());
        assertEquals(VALUE, new String(d.get(KEY)));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testReadWriteGeneric() {
        DataSource<String, Object> d = (DataSource<String, Object>) getDataSource(new DataConverter() {
            @Override
            public Object convert(Object key, byte[] value) {
                if (key.equals(KEY)) {
                    return new String(value);
                } else {
                    return value;
                }
            }
        });

        d.put(KEY, VALUE);
        d.put(KEY2, VALUE2.getBytes());
        assertEquals(VALUE, d.get(KEY));
        assertEquals(VALUE2, new String((byte[]) d.get(KEY2)));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testGetKeys() {
        DataSource<String, Object> d = (DataSource<String, Object>) getDataSource(null);

        d.put(KEY, VALUE);
        d.put(KEY2, VALUE2.getBytes());
        assertTrue(d.getKeys((String)null).contains(KEY));
        assertTrue(d.getKeys((String)null).contains(KEY2));
    }

    @Test
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public void testRemove() {
        DataSource<String, Object> d = (DataSource<String, Object>) getDataSource(null);
        d.put(KEY, VALUE);
        d.remove(KEY);
        assertNull(d.get(KEY));
    }

    protected DataSource<?, ?> getDataSource(DataConverter dataConverter){
        return DataSourceFactory.getStorage(getContext(),
                AccountManagerStoreDataSource.class, param, dataConverter);
    }
}
