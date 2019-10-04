/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.content.Context;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.oauth.OAuthServerException;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import org.junit.Test;

import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CountDownLatch;

import okhttp3.mockwebserver.MockResponse;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASMultiUserTest extends MASLoginTestBase {


    @Test()
    public void testMultiUserFalse() throws Exception {
        setDispatcher(new GatewayDefaultDispatcher() {
            @Override
            protected MockResponse retrieveTokenResponse() {
                //Mock response for retrieve token
                String error = "{\n" +
                        "  \"error\":\"invalid_request\",\n" +
                        "  \"error_description\":\"The given mag-identifier is either invalid or points to an unknown device\"\n" +
                        "}\n";
                return new MockResponse().setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
                        .setHeader("Content-type", ContentType.APPLICATION_JSON)
                        .setBody(error);
            }
        });
        MASCallbackFuture<MASUser> callbackFuture = new MASCallbackFuture<>();
        MASUser.login("other", "user".toCharArray(), callbackFuture);
        try {
            callbackFuture.get();
            fail();
        } catch (Exception e) {
            assertTrue(e.getCause().getCause() instanceof OAuthServerException);
        }

        final CountDownLatch countDownLatch = new CountDownLatch(1);
        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                countDownLatch.countDown();
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MAS.invoke(request, null);
        countDownLatch.await();
        //onAuthenticateRequest called

    }
}
