/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.os.Parcel;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASStartTestBase;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;

import junit.framework.Assert;

import org.junit.Test;

import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

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

        assertTrue(authenticationProviders.getProviders().get(0).isFacebook());
        assertEquals("facebook", authenticationProviders.getProviders().get(0).getIdentifier());

        assertTrue(authenticationProviders.getProviders().get(1).isGoogle());
        assertEquals("google", authenticationProviders.getProviders().get(1).getIdentifier());

        assertTrue(authenticationProviders.getProviders().get(2).isSalesForce());
        assertEquals("salesforce", authenticationProviders.getProviders().get(2).getIdentifier());

        assertTrue(authenticationProviders.getProviders().get(3).isLinkedIn());
        assertEquals("linkedin", authenticationProviders.getProviders().get(3).getIdentifier());

        assertTrue(authenticationProviders.getProviders().get(4).isEnterprise());
        assertEquals("enterprise", authenticationProviders.getProviders().get(4).getIdentifier());

        assertTrue(authenticationProviders.getProviders().get(5).isProximityLogin());
        assertEquals("qrcode", authenticationProviders.getProviders().get(5).getIdentifier());
        assertNotNull(authenticationProviders.getProviders().get(5).getPollUrl());
        assertNotNull(authenticationProviders.getProviders().get(5).getAuthenticationUrl());







    }
}
