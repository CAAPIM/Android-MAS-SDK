/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.test.oauth;

import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.request.internal.OAuthTokenRequest;
import com.ca.mas.core.test.BaseTest;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class TokenTest extends BaseTest {

    @Test
    public void oauthTokenTest() throws JSONException, InterruptedException {

        String expected;
        processRequest(new OAuthTokenRequest());
        expected = response.getBody().getContent().toString();
        response = null;
        error = null;

        String actual;

        //Send another request that make sure the tokens are cached
        processRequest(new OAuthTokenRequest());
        actual = response.getBody().getContent().toString();


        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected, actual);

    }
}
