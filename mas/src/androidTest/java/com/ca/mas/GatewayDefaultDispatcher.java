/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import android.net.Uri;
import android.util.Base64;

import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.io.IoUtils;
import com.ca.mas.identity.IdentityDispatcher;
import com.ca.mas.storage.StorageDispatcher;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.QueueDispatcher;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Date;
import java.util.UUID;

import sun.security.pkcs.PKCS10;

public class GatewayDefaultDispatcher extends QueueDispatcher {

    //Endpoints
    public static final String CONNECT_DEVICE_CONFIG = "/connect/device/config";
    public static final String CONNECT_DEVICE_EXPIRED_CONFIG = "/connect/device/expiredConfig";
    public static final String CONNECT_DEVICE_REGISTER = "/connect/device/register";
    public static final String CONNECT_CLIENT_INITIALIZE = "/connect/client/initialize";
    public static final String AUTH_OAUTH_V2_TOKEN = "/auth/oauth/v2/token";
    public static final String PROTECTED_RESOURCE_SLOW = "/protected/resource/slow";
    public static final String PROTECTED_RESOURCE_PRODUCTS_AS_ARRAY = "/protected/resource/productsAsArray";
    public static final String PROTECTED_RESOURCE_PRODUCTS = "/protected/resource/products";
    public static final String TEST_NO_CONTENT = "/testNoContent";
    public static final String AUTH_OAUTH_V2_AUTHORIZE = "/auth/oauth/v2/authorize";
    public static final String CONNECT_DEVICE_REGISTER_CLIENT = "/connect/device/register/client";
    public static final String CONNECT_DEVICE_RENEW = "/connect/device/renew";
    public static final String CONNECT_DEVICE_REMOVE = "/connect/device/remove";
    public static final String CONNECT_SESSION_LOGOUT = "/connect/session/logout";
    public static final String OTP_PROTECTED_URL = "/otpProtected";
    public static final String AUTH_OTP = "/auth/generateOTP";
    public static final String USER_INFO = "/openid/connect/v1/userinfo";
    public static final String OTHER = "other";

    public static final String ENTERPRISE_BROWSER = "/connect/enterprise/browser";
    public static final String ENTERPRISE_BROWSER_APPC = "/connect/enterprise/browser/websso/authorize/appc";

    public static final String ID_TOKEN = "dummy-idToken";
    public static final String ID_TOKEN_TYPE = "dummy-idTokenType";

    public static String TARGET_RESPONSE = "{ \"products\": [\n" +
            "    {\"id\": 1, \"name\": \"Red Stapler\", \"price\": \"54.44\"},\n" +
            "    {\"id\": 2, \"name\": \"Chewing Gum Wrapper\", \"price\": \"0.25\"},\n" +
            "    {\"id\": 3, \"name\": \"Microsoft Mouse\", \"price\": \"15.00\"},\n" +
            "    {\"id\": 4, \"name\": \"Mapple Smartphone\", \"price\": \"750.00\"},\n" +
            "    {\"id\": 5, \"name\": \"Can of Cola\", \"price\": \"1.25\"},\n" +
            "    {\"id\": 6, \"name\": \"Backpack\", \"price\": \"32.50\"}\n" +
            "  ],\n" +
            "  \"device_geo\": \"\",\n" +
            "  \"clientCert.subject\": \"CN=admin, OU=000000000000000, DC=sdk, O=Exampletronics Ltd\"\n" +
            "}\n";

    private StorageDispatcher storageDispatcher = new StorageDispatcher();
    private IdentityDispatcher identityDispatcher = new IdentityDispatcher();

    @Override
    public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
        try {
            request.getBody();
            if (request.getPath().contains(CONNECT_DEVICE_CONFIG)) {
                return configDeviceResponse();
            } else if (request.getPath().contains(CONNECT_DEVICE_EXPIRED_CONFIG)) {
                return expiredConfigDeviceResponse();
            } else if (request.getPath().contains(CONNECT_DEVICE_REGISTER)) {
                return registerDeviceResponse(request);
            } else if (request.getPath().contains(CONNECT_CLIENT_INITIALIZE)) {
                return initializeResponse();
            } else if (request.getPath().contains(AUTH_OAUTH_V2_TOKEN)) {
                return retrieveTokenResponse();
            } else if (request.getPath().contains(PROTECTED_RESOURCE_PRODUCTS_AS_ARRAY)) {
                return secureServiceResponseAsArray();
            } else if (request.getPath().contains(PROTECTED_RESOURCE_PRODUCTS)) {
                return secureServiceResponse();
            } else if (request.getPath().contains(PROTECTED_RESOURCE_SLOW)) {
                Thread.sleep(1000);
                return secureServiceResponse();
            } else if (request.getPath().contains(TEST_NO_CONTENT)) {
                return secureServiceResponseWithNoContent();
            } else if (request.getPath().contains(AUTH_OAUTH_V2_AUTHORIZE)) {
                return authorizeResponse(request);
            } else if (request.getPath().contains(CONNECT_DEVICE_REGISTER_CLIENT)) {
                return registerDeviceResponse(request);
            } else if (request.getPath().contains(CONNECT_DEVICE_RENEW)) {
                return renewDeviceResponse();
            } else if (request.getPath().contains(CONNECT_SESSION_LOGOUT)) {
                return logout();
            } else if (request.getPath().contains(CONNECT_DEVICE_REMOVE)) {
                return deRegister();
            } else if (request.getPath().contains(OTP_PROTECTED_URL)) {
                String xOtp = request.getHeader("X-OTP");
                if (xOtp == null) {
                    return otpMissingHeader();
                } else {
                    return otpProtectedResponse();
                }
            } else if (request.getPath().contains(AUTH_OTP)) {
                return generateOtp();
            } else if (request.getPath().contains(USER_INFO)) {
                return userInfo();
            } else if (request.getPath().startsWith(ENTERPRISE_BROWSER)) {
                return enterpriseBrowser(request);
            }

            MockResponse response = storageDispatcher.dispatch(request);
            if (response != null) {
                return response;
            }
            response = identityDispatcher.dispatch(request);
            if (response != null) {
                return response;
            }
            return other();
        } catch (Exception e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR).
                    setBody(e.toString());

        }
    }

    protected MockResponse other() {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
    }

    private MockResponse logout() {
        return new MockResponse().setResponseCode(200).setBody("{\"session_status\":\"logged out\"}");
    }

    protected MockResponse initializeResponse() {
        String result = "{\"client_id\":\"dummy\", \"client_secret\":\"dummy\", \"client_expiration\":" + new Date().getTime() + 36000 + "}";
        return new MockResponse().setResponseCode(200).setBody(result);
    }

    protected MockResponse authorizeResponse(RecordedRequest request) throws IOException, JSONException {
        Uri uri = Uri.parse(request.getPath());
        return new MockResponse()
                .setResponseCode(200)
                .setBody(TestUtils.getJSONObject(uri.getPath()).toString());
    }

    protected MockResponse deRegister() {
        return new MockResponse()
                .setResponseCode(200);
    }

    protected MockResponse configDeviceResponse() {
        byte[] bytes = new byte[0];
        try {
            bytes = IoUtils.slurpStream(getClass().getResourceAsStream("/msso_config.json"), 10485760);
            JSONObject jsonObject = new JSONObject(new String(bytes));
            return new MockResponse()
                    .setResponseCode(200)
                    .setBody(jsonObject.toString())
                    .addHeader("Content-type", ContentType.APPLICATION_JSON.toString());
        } catch (Exception e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }

    protected MockResponse expiredConfigDeviceResponse() {
        try {
            JSONObject jsonObject = new JSONObject("{\n" +
                    "  \"error\": \"invalid_request\",\n" +
                    "  \"error_description\": \"The server configuration is invalid. Contact the administrator\"\n" +
                    "}");
            return new MockResponse()
                    .setResponseCode(400)
                    .setHeader("x-ca-err", "132")
                    .setBody(jsonObject.toString())
                    .addHeader("Content-type", ContentType.APPLICATION_JSON.toString());
        } catch (Exception e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
    }


    protected MockResponse registerDeviceResponse(RecordedRequest request) {

        String magIdentifier = UUID.randomUUID().toString();

        PKCS10 pkcs10 = null;
        try {
            pkcs10 = new PKCS10(Base64.decode(request.getBody().readByteArray(), Base64.NO_WRAP | Base64.NO_PADDING | Base64.URL_SAFE));
        } catch (Exception e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }
        DataSource.getInstance().storeDevice(magIdentifier, new DataSource.Device(pkcs10.getSubjectPublicKeyInfo()));

        //Mock response for device registration
        String cert = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDCjCCAfKgAwIBAgIIKzRkwk/TRDswDQYJKoZIhvcNAQEMBQAwIzEhMB8GA1UEAxMYYXdpdHJp\n" +
                "c25hLWRlc2t0b3AuY2EuY29tMB4XDTEzMTEyNzE5MzkwOVoXDTE4MTEyNjE5MzkwOVowIzEhMB8G\n" +
                "A1UEAxMYYXdpdHJpc25hLWRlc2t0b3AuY2EuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n" +
                "CgKCAQEAoaCzdLbRhqt3T4ROTgOBD5gizxsJ/vhqmIpagXU+3OPhZocwf0FIVjvbrybkj8ZynTve\n" +
                "p1cJsAmdkuX+w6m8ow2rAR/8BQnIaBD281gNqDCYXAGkguEZBbCQ2TvD4FZYnJZSmrE9PJtIe5pq\n" +
                "DneOqaO0Kqj3sJpYIG11U8djio9UNAqTd0J9q5+fEMVle/QG0X0ro3MR30PaHIA7bpvISpjFZ0zD\n" +
                "54rQc+85bOamg4aJFcfiNSMIaAYaFMi/peJLmW8Q4DZriAQSG6PIBcekMx1mi4tuXkSrr3P3ycKu\n" +
                "bU0ePKnxckxWHygK42bQ5ClLuJeYNPxqHiBapZj2hwmzsQIDAQABo0IwQDAdBgNVHQ4EFgQUZddX\n" +
                "bkxC+asQgSCSIViGKuGS2f4wHwYDVR0jBBgwFoAUZddXbkxC+asQgSCSIViGKuGS2f4wDQYJKoZI\n" +
                "hvcNAQEMBQADggEBAHK/QdXrRROjKjxwU05wo1KZNRmi8jBsKF/ughCTqcUCDmEuskW/x9VCIm/r\n" +
                "ZMFgOA3tou7vT0mX8gBds+95td+aNci1bcBBpiVIwiqOFhBrtbiAhYofgXtbcYchL9SRmIpek/3x\n" +
                "BwBj5CBmaimOZsTLp6wqzLE4gpAdTMaU+RIlwq+uSUmKhQem6fSthGdWx5Ea9gwKuVi8PwSFCs/Q\n" +
                "nwUfNnCvOTP8PtQgvmLsXeaFfy/lYK7iQp1CiwwXYpc3Xivv9A7DH7MqVSQZdtjDrRI2++1/1Yw9\n" +
                "XoYtMDN0dQ5lBNIyJB5rWtCixZgfacHp538bMPMskLePU3dxNdCqhas=\n" +
                "-----END CERTIFICATE-----";
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("device-status", "activated")
                .setHeader("mag-identifier", magIdentifier)
                .setHeader("id-token", ID_TOKEN)
                .setHeader("id-token-type", ID_TOKEN_TYPE)
                .setBody(cert);

    }

    protected MockResponse renewDeviceResponse() {
        String newCert = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDCjCCAfKgAwIBAgIIKzRkwk/TRDswDQYJKoZIhvcNAQEMBQAwIzEhMB8GA1UEAxMYYXdpdHJp\n" +
                "c25hLWRlc2t0b3AuY2EuY29tMB4XDTEzMTEyNzE5MzkwOVoXDTE4MTEyNjE5MzkwOVowIzEhMB8G\n" +
                "A1UEAxMYYXdpdHJpc25hLWRlc2t0b3AuY2EuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n" +
                "CgKCAQEAoaCzdLbRhqt3T4ROTgOBD5gizxsJ/vhqmIpagXU+3OPhZocwf0FIVjvbrybkj8ZynTve\n" +
                "p1cJsAmdkuX+w6m8ow2rAR/8BQnIaBD281gNqDCYXAGkguEZBbCQ2TvD4FZYnJZSmrE9PJtIe5pq\n" +
                "DneOqaO0Kqj3sJpYIG11U8djio9UNAqTd0J9q5+fEMVle/QG0X0ro3MR30PaHIA7bpvISpjFZ0zD\n" +
                "54rQc+85bOamg4aJFcfiNSMIaAYaFMi/peJLmW8Q4DZriAQSG6PIBcekMx1mi4tuXkSrr3P3ycKu\n" +
                "bU0ePKnxckxWHygK42bQ5ClLuJeYNPxqHiBapZj2hwmzsQIDAQABo0IwQDAdBgNVHQ4EFgQUZddX\n" +
                "bkxC+asQgSCSIViGKuGS2f4wHwYDVR0jBBgwFoAUZddXbkxC+asQgSCSIViGKuGS2f4wDQYJKoZI\n" +
                "hvcNAQEMBQADggEBAHK/QdXrRROjKjxwU05wo1KZNRmi8jBsKF/ughCTqcUCDmEuskW/x9VCIm/r\n" +
                "ZMFgOA3tou7vT0mX8gBds+95td+aNci1bcBBpiVIwiqOFhBrtbiAhYofgXtbcYchL9SRmIpek/3x\n" +
                "BwBj5CBmaimOZsTLp6wqzLE4gpAdTMaU+RIlwq+uSUmKhQem6fSthGdWx5Ea9gwKuVi8PwSFCs/Q\n" +
                "nwUfNnCvOTP8PtQgvmLsXeaFfy/lYK7iQp1CiwwXYpc3Xivv9A7DH7MqVSQZdtjDrRI2++1/1Yw9\n" +
                "XoYtMDN0dQ5lBNIyJB5rWtCixZgfacHp538bMPMskLePU3dxNdCqhas=\n" +
                "-----END CERTIFICATE-----";
        //Mock response for device renew
        return new MockResponse()
                .setResponseCode(200)
                .setBody(newCert)
                .setHeader("mag-identifier", "test-device");

    }


    protected MockResponse registerDeviceClientResponse() {

        //Mock response for device registration
        String cert = "-----BEGIN CERTIFICATE-----\n" +
                "MIIDCjCCAfKgAwIBAgIIKzRkwk/TRDswDQYJKoZIhvcNAQEMBQAwIzEhMB8GA1UEAxMYYXdpdHJp\n" +
                "c25hLWRlc2t0b3AuY2EuY29tMB4XDTEzMTEyNzE5MzkwOVoXDTE4MTEyNjE5MzkwOVowIzEhMB8G\n" +
                "A1UEAxMYYXdpdHJpc25hLWRlc2t0b3AuY2EuY29tMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIB\n" +
                "CgKCAQEAoaCzdLbRhqt3T4ROTgOBD5gizxsJ/vhqmIpagXU+3OPhZocwf0FIVjvbrybkj8ZynTve\n" +
                "p1cJsAmdkuX+w6m8ow2rAR/8BQnIaBD281gNqDCYXAGkguEZBbCQ2TvD4FZYnJZSmrE9PJtIe5pq\n" +
                "DneOqaO0Kqj3sJpYIG11U8djio9UNAqTd0J9q5+fEMVle/QG0X0ro3MR30PaHIA7bpvISpjFZ0zD\n" +
                "54rQc+85bOamg4aJFcfiNSMIaAYaFMi/peJLmW8Q4DZriAQSG6PIBcekMx1mi4tuXkSrr3P3ycKu\n" +
                "bU0ePKnxckxWHygK42bQ5ClLuJeYNPxqHiBapZj2hwmzsQIDAQABo0IwQDAdBgNVHQ4EFgQUZddX\n" +
                "bkxC+asQgSCSIViGKuGS2f4wHwYDVR0jBBgwFoAUZddXbkxC+asQgSCSIViGKuGS2f4wDQYJKoZI\n" +
                "hvcNAQEMBQADggEBAHK/QdXrRROjKjxwU05wo1KZNRmi8jBsKF/ughCTqcUCDmEuskW/x9VCIm/r\n" +
                "ZMFgOA3tou7vT0mX8gBds+95td+aNci1bcBBpiVIwiqOFhBrtbiAhYofgXtbcYchL9SRmIpek/3x\n" +
                "BwBj5CBmaimOZsTLp6wqzLE4gpAdTMaU+RIlwq+uSUmKhQem6fSthGdWx5Ea9gwKuVi8PwSFCs/Q\n" +
                "nwUfNnCvOTP8PtQgvmLsXeaFfy/lYK7iQp1CiwwXYpc3Xivv9A7DH7MqVSQZdtjDrRI2++1/1Yw9\n" +
                "XoYtMDN0dQ5lBNIyJB5rWtCixZgfacHp538bMPMskLePU3dxNdCqhas=\n" +
                "-----END CERTIFICATE-----";
        return new MockResponse()
                .setResponseCode(200)
                .setHeader("device-status", "activated")
                .setHeader("mag-identifier", "test-device")
                .setBody(cert);
    }


    protected MockResponse retrieveTokenResponse() {
        //Mock response for retrieve token
        String token = "{\n" +
                "  \"access_token\":\"caa5871c-7c0f-44c7-b03b-1783609170e4\",\n" +
                "  \"token_type\":\"Bearer\",\n" +
                "  \"expires_in\":" + new Date().getTime() + 3600 + ",\n" +
                "  \"refresh_token\":\"19785fca-4b86-4f8e-a73c-7de1d420f88d\",\n" +
                "  \"scope\":\"openid msso phone profile address email msso_register msso_client_register mas_messaging mas_storage mas_identity mas_identity_retrieve_users mas_identity_create_users mas_identity_update_users mas_identity_delete_users mas_identity_retrieve_groups mas_identity_create_groups mas_identity_update_groups mas_identity_delete_groups\"\n" +
                "}";
        return new MockResponse().setResponseCode(200).setBody(token);

    }

    protected MockResponse retrieveTokenResponseWithIdToken() {
        //Mock response for retrieve token
        String token = "{\n" +
                "  \"access_token\":\"caa5871c-7c0f-44c7-b03b-1783609170e4\",\n" +
                "  \"token_type\":\"Bearer\",\n" +
                "  \"expires_in\":3600,\n" +
                "  \"refresh_token\":\"19785fca-4b86-4f8e-a73c-7de1d420f88d\",\n" +
                "  \"scope\":\"openid msso phone profile address email msso_register msso_client_register mas_messaging mas_storage mas_identity mas_identity_retrieve_users mas_identity_create_users mas_identity_update_users mas_identity_delete_users mas_identity_retrieve_groups mas_identity_create_groups mas_identity_update_groups mas_identity_delete_groups\",\n" +
                "  \"id-token\":\"dummy-idToken\",\n" +
                "  \"id-token-type\":\"dummy-idTokenType\"\n" +
                "}";
        return new MockResponse().setResponseCode(200).setBody(token);
    }

    protected MockResponse secureServiceResponse() {
        //Mock response for the secure service
        return new MockResponse().setResponseCode(200)
                .setBody(TARGET_RESPONSE)
                .addHeader("Content-type", ContentType.APPLICATION_JSON.toString());
    }

    protected MockResponse secureServiceResponseWithNoContent() {
        return new MockResponse().setResponseCode(HttpURLConnection.HTTP_NO_CONTENT);
    }

    protected MockResponse secureServiceResponseAsArray() {
        //Mock response for the secure service
        try {
            JSONArray jsonArray = new JSONArray("[\n" +
                    "  \"test1\",\n" +
                    "  \"test2\",\n" +
                    "  \"test3\"\n" +
                    "]");
            return new MockResponse().setResponseCode(200)
                    .setBody(jsonArray.toString())
                    .addHeader("Content-type", ContentType.APPLICATION_JSON.toString());

        } catch (JSONException e) {
            return new MockResponse().setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR);
        }


    }

    protected MockResponse otpProtectedResponse() {
        return new MockResponse().setResponseCode(200)
                .addHeader("Content-type", ContentType.TEXT_PLAIN);
    }

    protected MockResponse otpMissingHeader() {
        return new MockResponse().setResponseCode(401)
                .setHeader("X-OTP", "required")
                .setHeader("X-OTP-CHANNEL", "EMAIL,SMS,QRCODE")
                .setHeader("x-ca-err", "8000140")
                .setBody("{\n" +
                        "error\":\"otp_select_channel\",\n" +
                        "\"error_description\":\"This page is OTP protected. Please select the channel for OTP delivery.\"\n" +
                        "}\n");
    }

    protected MockResponse generateOtp() {
        return new MockResponse().setResponseCode(200)
                .setHeader("X-OTP", "generated");
    }

    protected MockResponse userInfo() {
        return new MockResponse().setResponseCode(200)
                .setHeader("Content-type", ContentType.APPLICATION_JSON)
                .setBody("{\n" +
                        "  \"sub\": \"6vKMGM8Xsw6o54D-FurMX1zXDhYLrf9fBBPFr-HwWXY\",\n" +
                        "  \"given_name\": \"Admin\",\n" +
                        "  \"family_name\": \"Admin\",\n" +
                        "  \"preferred_username\": \"admin\",\n" +
                        "  \"picture\": \"https://photos.example.com/profilephoto/72930000000Ccne/F\",\n" +
                        "  \"email\": \"sarek@layer7tech.com\",\n" +
                        "  \"phone_number\": \"555-555-5555\",\n" +
                        "  \"address\": {\n" +
                        "    \"street_address\": \"100 Universal City Plaza\",\n" +
                        "    \"locality\": \"Hollywood\",\n" +
                        "    \"region\": \"CA\",\n" +
                        "    \"postal_code\": \"91608\",\n" +
                        "    \"country\": \"USA\"\n" +
                        "  }\n" +
                        "}");
    }

    protected MockResponse enterpriseBrowser(RecordedRequest request) throws IOException, JSONException {
        return new MockResponse().setResponseCode(200)
                .setHeader("Content-type", ContentType.APPLICATION_JSON)
                .setBody(TestUtils.getJSONObject(request.getPath()).toString());

    }


}
