/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity;

import com.ca.mas.GatewayDefaultDispatcher;
import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.core.policy.exceptions.MobileNumberInvalidException;
import com.ca.mas.core.policy.exceptions.MobileNumberRequiredException;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.common.MASFilteredRequestBuilder;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.util.IdentityConsts;
import com.squareup.okhttp.mockwebserver.MockResponse;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

public class MASUserIdentityTest extends MASLoginTestBase {

    private UserAttributes userAttributes;

    @Before
    public void setUp() throws Exception {
        MASCallbackFuture<UserAttributes> userAttributesMASCallback = new MASCallbackFuture<>();
        MASUser.getCurrentUser().getUserMetaData(userAttributesMASCallback);
        userAttributes = userAttributesMASCallback.get();

    }

    @Test
    public void testGetUserByFilter() throws InterruptedException, ExecutionException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).contains("username", "a");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        List<MASUser> users = callbackFuture.get();
        assertNotNull(users);
        assertEquals(11, users.size());

    }

    @Test
    public void testGetUserByUserID() throws InterruptedException, ExecutionException {
        MASCallbackFuture<MASUser> masUserMASCallbackFuture = new MASCallbackFuture<>();
        MASUser.getCurrentUser().getUserById("sarek", masUserMASCallbackFuture);
        MASUser masUser = masUserMASCallbackFuture.get();
        assertEquals("sarek", masUser.getUserName());
        assertEquals("sarek", masUser.getId());
        assertEquals("Sarek", masUser.getName().getFamilyName());
        assertEquals("Sarek", masUser.getDisplayName());
        assertEquals("sarek@layer7tech.com", masUser.getEmailList().get(0).getValue());
        assertEquals("work", masUser.getEmailList().get(0).getType());
        assertEquals("778-329-9992", masUser.getPhoneList().get(0).getValue());
        assertEquals("work", masUser.getPhoneList().get(0).getType());
        assertNotNull(masUser.getPhotoList().get(0).getValue());
        assertEquals("thumbnail", masUser.getPhotoList().get(0).getType());
    }

    @Test
    public void testGetUserMetaData() throws InterruptedException, ExecutionException {
        MASCallbackFuture<UserAttributes> userAttributesMASCallback = new MASCallbackFuture<>();
        MASUser.getCurrentUser().getUserMetaData(userAttributesMASCallback);
        UserAttributes userAttributes = userAttributesMASCallback.get();
        assertTrue(userAttributes.getAttributes().contains("userName"));
        assertTrue(userAttributes.getAttributes().contains("displayName"));
        assertTrue(userAttributes.getAttributes().contains("name.familyName"));
        assertTrue(userAttributes.getAttributes().contains("name.givenName"));
        assertTrue(userAttributes.getAttributes().contains("password"));
        assertTrue(userAttributes.getAttributes().contains("emails.value"));
        assertTrue(userAttributes.getAttributes().contains("emails.display"));
        assertTrue(userAttributes.getAttributes().contains("emails.type"));
        assertTrue(userAttributes.getAttributes().contains("emails.primary"));
        assertTrue(userAttributes.getAttributes().contains("phoneNumbers.value"));
        assertTrue(userAttributes.getAttributes().contains("phoneNumbers.display"));
        assertTrue(userAttributes.getAttributes().contains("phoneNumbers.type"));
        assertTrue(userAttributes.getAttributes().contains("phoneNumbers.primary"));
        assertTrue(userAttributes.getAttributes().contains("photos.value"));
        assertTrue(userAttributes.getAttributes().contains("photos.display"));
        assertTrue(userAttributes.getAttributes().contains("photos.type"));
        assertTrue(userAttributes.getAttributes().contains("groups.value"));
        assertTrue(userAttributes.getAttributes().contains("groups.$ref"));
        assertTrue(userAttributes.getAttributes().contains("groups.display"));
        assertTrue(userAttributes.getAttributes().contains("groups.type"));
    }
}
