/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test;

import com.ca.mas.core.test.datasource.AccountManagerStoreDataSourceTest;
import com.ca.mas.core.test.datasource.KeystoreDataSourceTest;
import com.ca.mas.core.test.datasource.SecureAccountManagerStoreDataSourceTest;
import com.ca.mas.core.test.dynamicConfig.DynamicConfigTest;
import com.ca.mas.core.test.error.GeoFencingTest;
import com.ca.mas.core.test.error.JWTValidationTest;
import com.ca.mas.core.test.error.MAGEndpointTest;
import com.ca.mas.core.test.error.MSISDNTest;
import com.ca.mas.core.test.error.OAuthEndpointTest;
import com.ca.mas.core.test.http.HttpTest;
import com.ca.mas.core.test.oauth.AccessProtectedEndpointTest;
import com.ca.mas.core.test.oauth.AuthorizationCodeFlowTest;
import com.ca.mas.core.test.oauth.ClientCredentialsGrantTypeTest;
import com.ca.mas.core.test.oauth.DeviceIdTest;
import com.ca.mas.core.test.oauth.TokenTest;
import com.ca.mas.core.test.otp.OtpTest;
import com.ca.mas.core.test.storage.AccountManagerStorageTests;
import com.ca.mas.core.test.storage.KeyStoreStorageTests;
import com.ca.mas.core.test.storage.StorageProviderTests;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({AccessProtectedEndpointTest.class,
        ClientCredentialsGrantTypeTest.class,
        AuthorizationCodeFlowTest.class,
        TokenTest.class,
        AuthenticationTest.class,
        RenewDeviceTest.class,
        KeystoreDataSourceTest.class,
        AccountManagerStoreDataSourceTest.class,
        SecureAccountManagerStoreDataSourceTest.class,
        JWTValidationTest.class,
        MAGEndpointTest.class,
        OAuthEndpointTest.class,
        GeoFencingTest.class,
        MSISDNTest.class,
        DeviceIdTest.class,
        AccountManagerStorageTests.class,
        KeyStoreStorageTests.class,
        HttpTest.class,
        InitSDKTest.class,
        StorageProviderTests.class,
        DynamicConfigTest.class,
        OtpTest.class})

public class AllTest {

}
