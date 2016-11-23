/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.suite;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite that runs all tests, unit + instrumentation tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({InstrumentationTestSuite.class, UnitTestSuite.class})
public class MASTestSuite {}