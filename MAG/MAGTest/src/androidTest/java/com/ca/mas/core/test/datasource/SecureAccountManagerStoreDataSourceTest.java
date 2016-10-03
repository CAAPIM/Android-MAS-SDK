/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.datasource;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.datasource.DataConverter;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceFactory;
import com.ca.mas.core.datasource.SecureAccountManagerStoreDataSource;

import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class SecureAccountManagerStoreDataSourceTest extends AccountManagerStoreDataSourceTest{

    @Override
    protected DataSource<?, ?> getDataSource(DataConverter dataConverter) {
        return DataSourceFactory.getStorage(InstrumentationRegistry.getTargetContext(),
                SecureAccountManagerStoreDataSource.class, param, dataConverter);
    }
}
