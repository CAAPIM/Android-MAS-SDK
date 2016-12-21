/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.suite;

import com.ca.mas.sample.testapp.tests.instrumentation.Identity.MASUserTests;
import com.ca.mas.sample.testapp.tests.instrumentation.connecta.MASConnectaPublicBrokerTests;
import com.ca.mas.sample.testapp.tests.instrumentation.connecta.MASConnectaTests;
import com.ca.mas.sample.testapp.tests.instrumentation.foundation.MASApplicationTest;
import com.ca.mas.sample.testapp.tests.instrumentation.foundation.MASConfigurationTest;
import com.ca.mas.sample.testapp.tests.instrumentation.foundation.MASDeviceTests;
import com.ca.mas.sample.testapp.tests.instrumentation.foundation.MASTest;
import com.ca.mas.sample.testapp.tests.instrumentation.foundation.MASUnProtectedAPITest;
import com.ca.mas.sample.testapp.tests.instrumentation.group.MASGroupTests;
import com.ca.mas.sample.testapp.tests.instrumentation.storage.MASSecureStorageForApplication;
import com.ca.mas.sample.testapp.tests.instrumentation.storage.MASSecureStorageForUserApplication;
import com.ca.mas.sample.testapp.tests.instrumentation.storage.MASSecureStorageTests;
import com.ca.mas.sample.testapp.tests.instrumentation.storage.MASSecureLocalStorageForApplicationTest;
import com.ca.mas.sample.testapp.tests.instrumentation.storage.MASSecureLocalStorageTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Runs all Junit3 and Junit4 Instrumentation tests.
 */
@RunWith(Suite.class)
@Suite.SuiteClasses(
        {
                MASDeviceTests.class,
                MASConnectaTests.class,
                MASConnectaPublicBrokerTests.class,

                MASSecureStorageTests.class,
                MASSecureStorageForUserApplication.class,
                MASSecureStorageForApplication.class,

                MASSecureLocalStorageTest.class,
                MASSecureLocalStorageForApplicationTest.class,

                MASGroupTests.class,
                MASUserTests.class,

                MASTest.class,
                MASUnProtectedAPITest.class,
                MASApplicationTest.class,
                MASConfigurationTest.class,
        })
public class InstrumentationTestSuite {
}
