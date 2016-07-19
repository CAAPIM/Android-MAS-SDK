/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.foundation;

import android.support.test.InstrumentationRegistry;

import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MASConfigurationTest extends MASIntegrationBaseTest {

    private JSONObject expected;

    @Before
    public void before() throws Exception {
        expected = getConfig("msso_config.json");
    }

    @Test
    public void testAttributes() throws Exception {
        MASConfiguration configuration = MASConfiguration.getCurrentConfiguration();
        assertEquals(expected.getJSONObject("oauth").getJSONObject("client").getString("description"), configuration.getApplicationDescription());
        assertEquals(expected.getJSONObject("oauth").getJSONObject("client").getString("organization"), configuration.getApplicationOrganization());
        assertEquals(expected.getJSONObject("oauth").getJSONObject("client").getString("client_name"), configuration.getApplicationName());
        assertEquals(expected.getJSONObject("oauth").getJSONObject("client").getString("client_type"), configuration.getApplicationType());
        assertEquals(expected.getJSONObject("oauth").getJSONObject("client").getString("registered_by"), configuration.getApplicationRegisteredBy());
        assertEquals(expected.getJSONObject("mag").getJSONObject("mobile_sdk").getBoolean("sso_enabled"), configuration.isSsoEnabled());
        assertEquals(expected.getJSONObject("oauth").getJSONObject("system_endpoints").getString("authorization_endpoint_path"), configuration.getEndpointPath("msso.url.authorize"));
    }

    private JSONObject getConfig(String filename) {
        InputStream is = null;
        StringBuilder jsonConfig = new StringBuilder();

        try {
            is = InstrumentationRegistry.getInstrumentation().getTargetContext().getAssets().open(filename);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                jsonConfig.append(str);
            }
            return new JSONObject(jsonConfig.toString());
        } catch (IOException | JSONException e) {
            throw new IllegalArgumentException("Unable to read Json Configuration file: " + filename, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }
    }
}
