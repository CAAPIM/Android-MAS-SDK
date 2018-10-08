/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.SystemClock;

import com.ca.mas.MASMockGatewayTestBase;
import com.ca.mas.TestUtils;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.token.JWTRS256Validator;

import org.junit.After;
import org.junit.Assert;
import org.junit.Test;


public class MASJWKSPreloadTest extends MASMockGatewayTestBase {


    @After
    public void clearJWKS(){
        final SharedPreferences prefs = getContext().getSharedPreferences(JWTRS256Validator.JWT_KEY_SET_FILE, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        JWTRS256Validator.jwks = null;
    }

    @Test
    public void testJWKSPreloadEnabledAlgHS256() throws Exception {

        MAS.enableJwksPreload(true);
        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config_rs256.json"));
        Thread.sleep(1000);
        Assert.assertNotNull(JWTRS256Validator.jwks);

    }

    @Test
    public void testJWKSPreloadEnabledAlgRS256() throws Exception {

        MAS.enableJwksPreload(true);
        MAS.start(getContext(), TestUtils.getJSONObject("/msso_config_rs256.json"));
        Thread.sleep(1000);
        Assert.assertNotNull(JWTRS256Validator.jwks);
    }

}
