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
        MASAuthorizationCodeFlowTest.class,
        MASAuthorizationProviderTest.class,
        MASClaimsTest.class,
        MASClientCredentialTest.class,
        MASConfigurationTest.class,
        MASDeviceIdentifierTest.class,
        MASDeviceTest.class,
        MASDynamicSDKTest.class,
        MASEnrollmentStartTest.class,
        MASGeoFencingTest.class,
        MASJwtSigningTest.class,
        MASLoginTest.class,
        MASMultiFactorTest.class,
        MASMultiServerTest.class,
        MASMultiUserTest.class,
        MASOAuthLoginTest.class,
        MASOAuthTest.class,
        MASOneTimePasswordTest.class,
        MASPhoneNumberTest.class,
        MASRegistrationTest.class,
        MASSessionLockTest.class,
        MASSharedStorageSDKStopTest.class,
        MASSecureSharedStorageTest.class,
        MASStartTest.class,
        MASTest.class,
        MASUserTest.class,
})

public class MASFoundationTestSuite {
}
