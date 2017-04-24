/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.os.Parcel;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.foundation.auth.MASProximityLoginNFC;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class MASAuthorizationProviderTest extends MASStartTestBase {

    @Test
    public void testAuthenticationProviders() throws ExecutionException, InterruptedException {

        MASCallbackFuture<MASAuthenticationProviders> callbackFuture = new MASCallbackFuture<>();
        MASAuthenticationProviders.getAuthenticationProviders(callbackFuture);
        MASAuthenticationProviders authenticationProviders = callbackFuture.get();
        Parcel parcel = Parcel.obtain();
        authenticationProviders.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        authenticationProviders = MASAuthenticationProviders.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(6, authenticationProviders.getProviders().size());
        Assert.assertEquals("all", authenticationProviders.getIdp());

    }
}
