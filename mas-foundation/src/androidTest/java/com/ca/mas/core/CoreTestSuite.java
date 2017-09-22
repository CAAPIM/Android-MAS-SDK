/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core;

import com.ca.mas.core.datasource.AccountManagerStoreDataSourceTest;
import com.ca.mas.core.datasource.KeystoreDataSourceTest;
import com.ca.mas.core.datasource.SecureAccountManagerStoreDataSourceTest;
import com.ca.mas.core.storage.AccountManagerStorageTests;
import com.ca.mas.core.storage.EncryptionProviderTests;
import com.ca.mas.core.storage.KeyStoreStorageTests;
import com.ca.mas.core.storage.StorageProviderTests;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        AccountManagerStorageTests.class,
        EncryptionProviderTests.class,
        KeyStoreStorageTests.class,
        StorageProviderTests.class,

        AccountManagerStoreDataSourceTest.class,
        KeystoreDataSourceTest.class,
        SecureAccountManagerStoreDataSourceTest.class,


})

public class CoreTestSuite {
}
