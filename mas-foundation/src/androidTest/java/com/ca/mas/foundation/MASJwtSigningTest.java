/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.util.Base64;
import android.util.Pair;

import com.ca.mas.DataSource;
import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.http.ContentType;
import com.ca.mas.core.security.KeyStoreException;
import com.ca.mas.core.store.ClientCredentialContainer;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.JWTValidation;
import com.ca.mas.core.token.JWTValidationException;
import com.ca.mas.core.util.KeyUtilsAsymmetric;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.util.DateUtils;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertTrue;

public class MASJwtSigningTest extends MASLoginTestBase {
    @Test
    public void testJSONDefaultTimeoutPost() throws Exception {
        JSONObject requestData = new JSONObject();
        requestData.put("jsonName", "jsonValue");
        requestData.put("jsonName2", 1234);

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .post(MASRequestBody.jsonBody(requestData))
                .sign()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        callback.get();

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        String magIdentifier = rr.getHeader("mag-identifier");
        DataSource.Device device = DataSource.getInstance().getDevice(magIdentifier);
        String signedDoc = rr.getBody().readUtf8();

        JWSObject signedObject = JWSObject.parse(signedDoc);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) device.getRegisteredPublicKey());
        Assert.assertTrue(signedObject.verify(verifier));
        net.minidev.json.JSONObject payload = signedObject.getPayload().toJSONObject();
        Assert.assertEquals(requestData.get("jsonName"), (new JSONObject(payload.get("content").toString())).get("jsonName"));
        Assert.assertEquals(payload.get("aud"), "localhost");
        // Expiry time should be equal to 5 minutes plus the specified timeout
        Assert.assertEquals((long) payload.get("iat") + 300, payload.get("exp"));
        Assert.assertEquals(payload.get("content-type"), ContentType.APPLICATION_JSON.getMimeType());
        Assert.assertNotNull(payload.get("iss"));
        Assert.assertEquals(MASUser.getCurrentUser().getUserName(), payload.get("sub"));
        //... assert other attribute
    }

    @Test
    public void testJSONCustomTimeoutSecondsPost() throws Exception {
        JSONObject requestData = new JSONObject();
        requestData.put("jsonName", "jsonValue");
        requestData.put("jsonName2", 1234);
        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
        calendar.add(Calendar.MINUTE, 10);

        MASClaims claims = new MASClaimsBuilder().expirationTime(calendar.getTime()).build();
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .post(MASRequestBody.jsonBody(requestData))
                .sign(claims)
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        callback.get();

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        String magIdentifier = rr.getHeader("mag-identifier");
        DataSource.Device device = DataSource.getInstance().getDevice(magIdentifier);
        String signedDoc = rr.getBody().readUtf8();

        JWSObject signedObject = JWSObject.parse(signedDoc);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) device.getRegisteredPublicKey());
        Assert.assertTrue(signedObject.verify(verifier));
        net.minidev.json.JSONObject payload = signedObject.getPayload().toJSONObject();
        // Expiry time should be equal to the issued time plus the specified timeout
        Assert.assertEquals(DateUtils.toSecondsSinceEpoch(calendar.getTime()), payload.get("exp"));
        //... assert other attribute
    }

    @Test
    public void testStringPost() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .post(MASRequestBody.stringBody("test"))
                .sign()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        callback.get();

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        String signedDoc = rr.getBody().readUtf8();
        JWSObject signedObject = JWSObject.parse(signedDoc);

        net.minidev.json.JSONObject payload = signedObject.getPayload().toJSONObject();
        Assert.assertEquals("test", payload.get(MASClaimsConstants.CONTENT));
    }

    @Test
    public void testFormPost() throws Exception {
        List<Pair<String, String>> form = new ArrayList<>();
        form.add(new Pair<String, String>("formfield1", "field1Value"));
        form.add(new Pair<String, String>("formfield1", "field2Value"));

        JSONObject expected = new JSONObject();
        for (Pair<String, String> pair : form) {
            if (pair.first != null) {
                try {
                    JSONArray jsonArray = (JSONArray) expected.opt(pair.first);
                    if (jsonArray == null) {
                        jsonArray = new JSONArray();
                        expected.put(pair.first, jsonArray);
                    }
                    if (pair.second != null) {
                        jsonArray.put(pair.second);
                    }
                } catch (JSONException e) {
                    //ignore
                }
            }
        }

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .post(MASRequestBody.urlEncodedFormBody(form))
                .sign()
                .build();

        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        callback.get();

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        String signedDoc = rr.getBody().readUtf8();
        JWSObject signedObject = JWSObject.parse(signedDoc);

        net.minidev.json.JSONObject payload = signedObject.getPayload().toJSONObject();
        Assert.assertEquals(expected.get("formfield1"), (new JSONObject(payload.get(MASClaimsConstants.CONTENT).toString())).get("formfield1"));
    }

    @Test
    public void testByteArrayPost() throws Exception {
        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .post(MASRequestBody.byteArrayBody("test".getBytes()))
                .sign()
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        callback.get();

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        String signedDoc = rr.getBody().readUtf8();
        JWSObject signedObject = JWSObject.parse(signedDoc);

        net.minidev.json.JSONObject payload = signedObject.getPayload().toJSONObject();
        Assert.assertEquals("test", new String(Base64.decode((String) payload.get(MASClaimsConstants.CONTENT), Base64.URL_SAFE | Base64.NO_PADDING | Base64.NO_PADDING)));
    }

    @Test
    public void testSignWithPrivateKey() throws Exception, MASException {
        KeyUtilsAsymmetric.deletePrivateKey("TEST");

        PrivateKey privateKey = KeyUtilsAsymmetric.generateRsaPrivateKey( 2048, "TEST", "CN=msso", false, false, -1, false);
        PublicKey publicKey = KeyUtilsAsymmetric.getRsaPublicKey("TEST");

        JSONObject requestData = new JSONObject();
        requestData.put("jsonName", "jsonValue");
        requestData.put("jsonName2", 1234);

        MASClaims claims = new MASClaimsBuilder().claim(MASClaimsConstants.CONTENT, requestData).build();
        String signedJWT = MAS.sign(claims, privateKey);

        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) publicKey);
        assertTrue(JWSObject.parse(signedJWT).verify(verifier));

        //Clean up for the test
        KeyUtilsAsymmetric.deletePrivateKey("TEST");

        net.minidev.json.JSONObject payload = JWSObject.parse(signedJWT).getPayload().toJSONObject();
        Assert.assertEquals(requestData.get("jsonName"), (new JSONObject(payload.get("content").toString())).get("jsonName"));
    }

    @Test
    public void testSignWithInvalidPrivateKey() throws MASException, CertificateException, UnrecoverableKeyException, NoSuchAlgorithmException, IOException, java.security.KeyStoreException, NoSuchProviderException, InvalidAlgorithmParameterException, JSONException, URISyntaxException, ExecutionException, InterruptedException, ParseException, JOSEException, KeyStoreException {
        KeyUtilsAsymmetric.deletePrivateKey("TEST");

        PrivateKey privateKey = KeyUtilsAsymmetric.generateRsaPrivateKey( 2048, "TEST", "CN=msso", false, false, -1, false);
        JSONObject requestData = new JSONObject();
        requestData.put("jsonName", "jsonValue");
        requestData.put("jsonName2", 1234);

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .post(MASRequestBody.jsonBody(requestData))
                .sign(privateKey)
                .build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);

        callback.get();

        RecordedRequest rr = getRecordRequest(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS);
        String magIdentifier = rr.getHeader("mag-identifier");
        DataSource.Device device = DataSource.getInstance().getDevice(magIdentifier);
        String signedDoc = rr.getBody().readUtf8();
        JWSObject signedObject = JWSObject.parse(signedDoc);
        JWSVerifier verifier = new RSASSAVerifier((RSAPublicKey) device.getRegisteredPublicKey());
        Assert.assertFalse(signedObject.verify(verifier));

        KeyUtilsAsymmetric.deletePrivateKey("TEST");
    }

    @Test
    public void validateTokenWithAlgorithmRS256() throws JWTValidationException {

        ClientCredentialContainer cc = StorageProvider.getInstance().getClientCredentialContainer();

        IdToken idToken = new IdToken("eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsImtpZCI6ImRlZmF1bHRfc3NsX2tleSJ9.ewogInN1YiI6ICJmRHRoVGpQcDB2N2J1Y1BGaGFQVWRmQXl6M0R0eGRhWjFMc1NKeG1oQTh3IiwKICJhdWQiOiAiZThmNDI3YjMtMDJlMy00ODEwLTg0MjQtMzY5YjEzMTliNWRmIiwKICJhY3IiOiAiMCIsCiAiYXpwIjogIlRXSTFjRmhoYUhjM1FuUndNa1pvUTJGMFpUaHJTbnA1YjA5SlBRPT0iLAogImF1dGhfdGltZSI6IDE1NDIyODI5NjksCiAiaXNzIjogImh0dHBzOi8vbWFnZmlkby5jYS5jb206ODQ0MyIsCiAiZXhwIjogMTg1NzY0Mjk2OSwKICJpYXQiOiAxNTQyMjgyOTY5Cn0.by-lQCErcn00D8EEOuA8fpoIyI9-wAkYL5nWdaYtFUzrLqkP4VH9OVXaontZrrnoFgz3EW6Bmr4ZIJftIfGWKTWBCKJDd0mykkIyPUevxjtmfTUAxrNS2FzghlEavnvKQ-Ff2E_QlFOCnXh4PBmms-VP9TX7N02gT1Dy5_w3-xSe80YBkFISaj2yckuSXMsBnuhFijg-SNP1QeCRIGkjyETYu8VaT_3H18dgbeGgavn4f-oa0Q_p1qzxz-2CrEaf1U66zivKAP1BVQPYtC8KxUsyfUPwFSxqEWWR1adphvWZVIcID1CLHWF9tN8vCdvOTi-7JnYHiIkx3QYLv5A64g", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        String deviceIdentifier = "TWI1cFhhaHc3QnRwMkZoQ2F0ZThrSnp5b09JPQ==";
        String clientId = "e8f427b3-02e3-4810-8424-369b1319b5df";
        String clientSecret = cc.getClientSecret();
        // - validate the token
        Assert.assertTrue(JWTValidation.validateIdToken(MssoContext.newContext(), idToken, deviceIdentifier, clientId, clientSecret));
    }

    @Test(expected = JWTValidationException.class)
    public void validateTokenWithInvalidAlgorithm() throws JWTValidationException {

        ClientCredentialContainer cc = StorageProvider.getInstance().getClientCredentialContainer();
        // - generated in https://jwt.io/
        IdToken idToken = new IdToken("eyJhbGciOiJMUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImRlZmF1bHRfc3NsX2tleSJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiYWRtaW4iOnRydWUsImlhdCI6MTUxNjIzOTAyMiwiZXhwIjo5OTk5OTk5OTk5LCJhdWQiOiJkdW1teSIsImF6cCI6ImY0NzM1MjVkLWMxMzAtNGJiYS04NmNjLWRiMjZkODg3NTM4NiJ9.Q25Tm1yqs-KLR_qX-t6iuq38K_yFeobil3oMAXx9E2L1ds-DUG6tzm3BNQZUTQdNALRI47pGJUF4ZLJkqyC-z_THqwZwBq9ISfalmDxmSdf_ec7qt6Ll-mFj7epAfMY5JsEG7YO6ReDmfToke95ZJup9x25GHZOuH_gyiSd94SM", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        String deviceIdentifier = "f473525d-c130-4bba-86cc-db26d8875386";
        String clientId = cc.getClientId();
        String clientSecret = cc.getClientSecret();
        // - validate the token
        JWTValidation.validateIdToken(MssoContext.newContext(), idToken, deviceIdentifier, clientId, clientSecret);
    }

    @Test(expected = IllegalStateException.class)
    public void testSignWithNonRegisteredDevice() throws Exception, MASException {
        if (MASDevice.getCurrentDevice().isRegistered()) {
            MASCallbackFuture<Void> deregisterCallback = new MASCallbackFuture<Void>();
            MASDevice.getCurrentDevice().deregister(deregisterCallback);
            Assert.assertNull(deregisterCallback.get());
        }
        MASClaims claims = new MASClaimsBuilder().build();
        MAS.sign(claims);
    }
}
