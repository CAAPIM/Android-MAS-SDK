/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.content.Context;
import android.util.Base64;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MASRegistrationTest extends MASStartTestBase {

    @Test
    public void testRenewCertification() throws URISyntaxException, InterruptedException, IOException, ExecutionException {

        setDispatcher(new GatewayDefaultDispatcher() {

            @Override
            protected MockResponse registerDeviceResponse(RecordedRequest request) {
                // Expired cert to trigger renew
                String cert = "-----BEGIN CERTIFICATE-----\n" +
                        "MIIB1jCCAT+gAwIBAgIJAMgniDRduPzqMA0GCSqGSIb3DQEBBQUAMC0xCzAJBgNV\n" +
                        "BAYTAkdCMQ8wDQYDVQQHEwZMb25kb24xDTALBgNVBAMTBFRlc3QwHhcNMTYxMTA5\n" +
                        "MDEwNjMwWhcNMTYxMTE5MDEwNjMwWjAtMQswCQYDVQQGEwJHQjEPMA0GA1UEBxMG\n" +
                        "TG9uZG9uMQ0wCwYDVQQDEwRUZXN0MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKB\n" +
                        "gQCZuzicKw/iV3XL+AEoNlBCm9Ssf7bEm5Fn0WFmmL04FFiU3SdiCV76PmI1lpaI\n" +
                        "Xf6u/mP26gLOWg0URkFTPlbq6u8SggOc8+lkqH24RSthjJm9SyziZdj/LCxNxLz7\n" +
                        "YF2NJyh13PLzqs1AFnodoYVJbFDCMQ6/T6YG1cPcRxLiGwIDAQABMA0GCSqGSIb3\n" +
                        "DQEBBQUAA4GBAIZrjaTgJxedR+ChsGUqWvVCejz1Vcjm6pmKKSucbsF3akTrJof4\n" +
                        "15p9JsU3zSyBt7g9y8v02JhksXNHKHhbVYpu35SR+u3YsHX0CYU6Ela6rqBnIkwA\n" +
                        "9gKN/wGTsip9Lzlk1+eV/kaYDfH96sw9Q/r+s9Q6FcZUePctnLS5zsJM\n" +
                        "-----END CERTIFICATE-----";
                return new MockResponse()
                        .setResponseCode(200)
                        .setHeader("device-status", "activated")
                        .setHeader("mag-identifier", "test-device")
                        .setHeader("id-token", "dummy-idToken")
                        .setHeader("id-token-type", "dummy-idTokenType")
                        .setBody(cert);

            }
        });

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        assertNotNull(callback.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback.get().getResponseCode());

        // Repeat to trigger renew endpoint
        MASRequest request2 = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS)).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);
        assertNotNull(callback2.get());
        assertEquals(HttpURLConnection.HTTP_OK, callback2.get().getResponseCode());

        //Make sure it has invoke renew endpoint
        assertNotNull(getRecordRequest(GatewayDefaultDispatcher.CONNECT_DEVICE_RENEW));

    }

}
