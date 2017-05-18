/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class MASOAuthLoginTest extends MASLoginTestBase {

    @Test
    public void oauthTokenTest() throws JSONException, InterruptedException, ExecutionException, URISyntaxException {

        String expected;
        MASRequest request = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback = new MASCallbackFuture<>();
        MAS.invoke(request, callback);
        expected = callback.get().getBody().getContent().toString();

        String actual;

        MASRequest request2 = new MASRequest.MASRequestBuilder(new URI("/protected/resource/products?operation=listProducts")).build();
        MASCallbackFuture<MASResponse<JSONObject>> callback2 = new MASCallbackFuture<>();
        MAS.invoke(request2, callback2);

        //Send another request that make sure the tokens are cached
        actual = callback2.get().getBody().getContent().toString();


        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected, actual);

    }
}
