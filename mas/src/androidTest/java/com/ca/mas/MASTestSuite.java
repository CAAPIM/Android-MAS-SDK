/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import com.ca.mas.foundation.MASFoundationTestSuite;
import com.ca.mas.identity.MASIdentityTestSuite;
import com.ca.mas.storage.MASStorageTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MASFoundationTestSuite.class,
        MASStorageTestSuite.class,
        MASIdentityTestSuite.class,
})

public class MASTestSuite {
}
