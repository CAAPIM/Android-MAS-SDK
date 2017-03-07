/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import com.ca.mas.foundation.MASClientCredentialTest;
import com.ca.mas.foundation.MASDeviceTest;
import com.ca.mas.foundation.MASEnrollmentStartTest;
import com.ca.mas.foundation.MASGeoFencingTest;
import com.ca.mas.foundation.MASLoginTest;
import com.ca.mas.foundation.MASOAuthLoginTest;
import com.ca.mas.foundation.MASOAuthTest;
import com.ca.mas.foundation.MASPhoneNumberTest;
import com.ca.mas.foundation.MASTest;
import com.ca.mas.foundation.MASUserTest;

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
        MASGeoFencingTest.class,
        MASPhoneNumberTest.class,
        MASOAuthTest.class,
        MASOAuthLoginTest.class,
        MASDeviceTest.class})

public class MASTestSuite {
}
