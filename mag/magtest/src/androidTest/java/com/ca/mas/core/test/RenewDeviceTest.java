package com.ca.mas.core.test;

import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.http.MAGRequest;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class RenewDeviceTest extends BaseTest {

    @Before
    public void before() throws Exception {
        super.before();
        ssg.setDispatcher(new DefaultDispatcher() {
            @Override
            protected MockResponse registerDeviceResponse() {
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
                        .setHeader("id-token", getIdToken())
                        .setHeader("id-token-type", getIdTokenType())
                        .setBody(cert);
            }
        });
    }

    @Test
    public void testRenewDevice() throws URISyntaxException, InterruptedException, IOException {
        MAGRequest request = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).password().build();
        processRequest(request);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (useMockServer()) {
            ssg.takeRequest(); // Authorize Request
            ssg.takeRequest(); // Client Credentials Request

            //Make sure the register request contain authorization header
            RecordedRequest registerRequest = ssg.takeRequest();
            assertNotNull(registerRequest.getHeader("authorization"));

            //Make sure the access token request use id-token grant type
            RecordedRequest accessTokenRequest = ssg.takeRequest();
            String s = new String(accessTokenRequest.getBody().readByteArray(), "US-ASCII");
            assertTrue(s.contains("assertion=" + getIdToken()));
            assertTrue(s.contains("grant_type=" + getIdTokenType()));
        }

        // Repeat to trigger renew endpoint
        MAGRequest request2 = new MAGRequest.MAGRequestBuilder(getURI("/protected/resource/products?operation=listProducts")).password().build();
        processRequest(request2);
        assertEquals(HttpURLConnection.HTTP_OK, response.getResponseCode());

        if (useMockServer()) {
            RecordedRequest renewRequest = ssg.takeRequest();
            assertEquals("/connect/device/renew", renewRequest.getPath());
            ssg.takeRequest(); // Product request
        }
    }
}
