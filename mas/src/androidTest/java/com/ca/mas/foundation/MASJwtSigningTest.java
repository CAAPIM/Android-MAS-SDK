/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.DataSource;
import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import java.net.URI;
import java.security.interfaces.RSAPublicKey;

public class MASJwtSigningTest extends MASLoginTestBase {

    @Test
    public void testJSONPost() throws Exception {

        JSONObject requestData = new JSONObject();
        requestData.put("jsonName", "jsonValue");
        requestData.put("jsonName2", 1234);

        MASRequest request = new MASRequest.MASRequestBuilder(new URI(GatewayDefaultDispatcher.PROTECTED_RESOURCE_PRODUCTS))
                .post(MASRequestBody.jsonBody(requestData))
                //.sign()
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
        Assert.assertEquals("<expected iss>", signedObject.getPayload().toJSONObject().get("iss"));
        Assert.assertEquals(requestData.toString(), signedObject.getPayload().toJSONObject().get("content"));
        //... assert other attribute


    }


}
