/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import com.ca.mas.core.CoreTestSuite;
import com.ca.mas.foundation.MASFoundationTestSuite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        MASFoundationTestSuite.class,
        CoreTestSuite.class,
})

public class MASTestSuite {
}
