/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import com.ca.mas.storage.MASSecureLocalStorageApplicationTest;
import com.ca.mas.storage.MASSecureLocalStorageUserTest;
import com.ca.mas.storage.MASSecureStorageAppTest;
import com.ca.mas.storage.MASSecureStorageUserAppTest;
import com.ca.mas.storage.MASSecureStorageUserTest;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        MASSecureLocalStorageUserTest.class,
        MASSecureLocalStorageApplicationTest.class,
        MASSecureStorageUserTest.class,
        MASSecureStorageAppTest.class,
        MASSecureStorageUserAppTest.class,
})

public class MASStorageTestSuite {
}
