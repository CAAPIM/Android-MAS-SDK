/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        MASUserTest.class,
        MASLoginTest.class,
        MASEnrollmentStartTest.class,
        MASClientCredentialTest.class,
        MASTest.class,
        MASStartTest.class,
        MASGeoFencingTest.class,
        MASPhoneNumberTest.class,
        MASOAuthTest.class,
        MASOAuthLoginTest.class,
        MASDeviceTest.class,
        MASJwtSigningTest.class,
        MASDynamicSDKTest.class,
        MASRegistrationTest.class,
        MASAuthorizationCodeFlowTest.class,
        MASOneTimePasswordTest.class,
        MASDeviceTest.class,
        MASConfigurationTest.class,
        MASAuthorizationProviderTest.class,
        MASSharedStorageTest.class,
        MASSharedStorageSDKStopTest.class,
        MASSessionLockTest.class
})

public class MASFoundationTestSuite {
}
