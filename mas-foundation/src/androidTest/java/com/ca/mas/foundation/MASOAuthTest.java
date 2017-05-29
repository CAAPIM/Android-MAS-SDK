/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.oauth.OAuthException;
import com.squareup.okhttp.mockwebserver.MockResponse;

import junit.framework.Assert;

import org.junit.Test;

import java.util.Date;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASOAuthTest extends MASStartTestBase {

    @Test
    public void getAccessTokenWithMissingAccessToken() throws InterruptedException {

        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                String token = "{\n" +
                        "  \"token_type\":\"Bearer\",\n" +
                        "  \"expires_in\":" + new Date().getTime() + 3600 + ",\n" +
                        "  \"refresh_token\":\"19785fca-4b86-4f8e-a73c-7de1d420f88d\",\n" +
                        "  \"scope\":\"openid msso phone profile address email\"\n" +
                        "}";
                return new MockResponse().setResponseCode(200).setBody(token);
            }
        });

        MASCallbackFuture<MASUser> callback = new MASCallbackFuture<>();
        MASUser.login("test", "test".toCharArray(), callback);
        try {
            Assert.assertNotNull(callback.get());
            fail();
        } catch (ExecutionException e) {
            assertTrue(e.getCause().getCause() instanceof OAuthException);
            assertTrue(((MASException) e.getCause()).getRootCause() instanceof OAuthException);
        }
    }

}
