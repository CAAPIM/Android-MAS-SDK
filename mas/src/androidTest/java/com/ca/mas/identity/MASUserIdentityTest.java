/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity;

import com.ca.mas.MASCallbackFuture;
import com.ca.mas.MASLoginTestBase;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.common.MASFilteredRequestBuilder;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.user.UserNotAuthenticatedException;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import junit.framework.Assert;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
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
    public void testGetUserByFilterContains() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).contains("username", "a");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        List<MASUser> users = callbackFuture.get();
        assertNotNull(users);
        assertEquals(11, users.size());
        for (MASUser user : users) {
            assertNotNull(user.getAsJSONObject());
            assertNotNull(user.getUserName());
        }
    }

    @Test
    public void testGetUserByUserID() throws InterruptedException, ExecutionException {
        MASCallbackFuture<MASUser> masUserMASCallbackFuture = new MASCallbackFuture<>();
        MASUser.getCurrentUser().getUserById("sarek", masUserMASCallbackFuture);
        MASUser masUser = masUserMASCallbackFuture.get();
        assertEquals("sarek", masUser.getUserName());
        assertEquals("sarek", masUser.getId());
        assertEquals("Sarek", masUser.getName().getFamilyName());
        assertEquals("Ms. Barbara J Jensen, III", masUser.getName().getFormatted());
        assertEquals("Sarek", masUser.getName().getGivenName());
        assertEquals("Jane", masUser.getName().getMiddleName());
        assertEquals("Ms.", masUser.getName().getHonorificPrefix());
        assertEquals("III", masUser.getName().getHonorificSuffix());
        assertEquals("Sarek", masUser.getDisplayName());
        assertEquals("Babs", masUser.getNickName());
        assertEquals("https://login.example.com/bjensen", masUser.getProfileUrl());
        assertEquals("sarek@layer7tech.com", masUser.getEmailList().get(0).getValue());
        assertEquals("work", masUser.getEmailList().get(0).getType());
        assertEquals("babs@jensen.org", masUser.getEmailList().get(1).getValue());
        assertEquals("home", masUser.getEmailList().get(1).getType());

        assertEquals(2, masUser.getAddressList().size());
        assertEquals("work", masUser.getAddressList().get(0).getType());
        assertEquals("100 Universal City Plaza", masUser.getAddressList().get(0).getStreetAddress());
        assertEquals("Hollywood", masUser.getAddressList().get(0).getLocality());
        assertEquals("CA", masUser.getAddressList().get(0).getRegion());
        assertEquals("91608", masUser.getAddressList().get(0).getPostalCode());
        assertEquals("USA", masUser.getAddressList().get(0).getCountry());
        assertEquals("100 Universal City Plaza\nHollywood, CA 91608 USA", masUser.getAddressList().get(0).getFormatted());
        assertEquals(true, masUser.getAddressList().get(0).isPrimary());

        assertEquals(2, masUser.getPhoneList().size());
        assertEquals("778-329-9992", masUser.getPhoneList().get(0).getValue());
        assertEquals("work", masUser.getPhoneList().get(0).getType());

        assertEquals(1, masUser.getImsList().size());
        assertEquals("someaimhandle", masUser.getImsList().get(0).getValue());
        assertEquals("aim", masUser.getImsList().get(0).getType());

        assertEquals(2, masUser.getPhotoList().size());
        assertNotNull(masUser.getPhotoList().get(0).getValue());
        assertEquals("thumbnail", masUser.getPhotoList().get(0).getType());

        assertEquals("User", masUser.getMeta().getResourceType());
        assertEquals("2010-01-23T04:56:22Z", masUser.getMeta().getCreated());
        assertEquals("2011-05-13T04:42:34Z", masUser.getMeta().getLastModified());
        assertEquals("W/\"a330bc54f0671c9\"", masUser.getMeta().getVersion());
        assertEquals("https://mobile-staging-mysql.l7tech.com:8443/mysql/SCIM/MAS-IIP/v2/Users/sarek", masUser.getMeta().getLocation());

        assertEquals("Employee", masUser.getUserType());
        assertEquals("Tour Guide", masUser.getTitle());
        assertEquals("en-US", masUser.getPreferredLanguage());
        assertEquals("en-US", masUser.getLocale());
        assertEquals("America/Los_Angeles", masUser.getTimeZone());
        assertEquals("t1meMa$heen", masUser.getPassword());
        assertEquals(3, masUser.getGroupList().size());
        assertEquals("e9e30dba-f08f-4109-8486-d5c6a331660a", masUser.getGroupList().get(0).getValue());
        assertEquals("https://example.com/v2/Groups/e9e30dba-f08f-4109-8486-d5c6a331660a", masUser.getGroupList().get(0).getReference());

    }

    @Test
    public void testNotAuthenticatedUserOperation() throws Exception {
        MASCallbackFuture<MASUser> masUserMASCallbackFuture = new MASCallbackFuture<>();
        MASUser.getCurrentUser().getUserById("sarek", masUserMASCallbackFuture);
        MASUser masUser = masUserMASCallbackFuture.get();
        try {
            masUser.requestUserInfo(null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        assertFalse(masUser.isAuthenticated());
        assertFalse(masUser.isCurrentUser());

        try {
            masUser.startListeningToMyMessages(null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.sendMessage((MASTopic) null, null, null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.sendMessage((MASMessage) null, null, null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.sendMessage(null, null, null, null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.stopListeningToMyMessages(null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.logout(null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.getUserById(null, null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.getUsersByFilter(null, null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.getUserMetaData(null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        Assert.assertNotNull(masUser.getThumbnailImage());

        try {
            masUser.lockSession(null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        try {
            masUser.unlockSession(null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }

        Assert.assertFalse(masUser.isSessionLocked());

        try {
            masUser.removeSessionLock(null);
            fail();
        } catch (UserNotAuthenticatedException ignored) {
        }
    }

    @Test
    public void testGetUserMetaData() throws InterruptedException, ExecutionException, JSONException {
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

        userAttributes.save(getContext());

        UserAttributes savedAttributes = new UserAttributes();
        assertFalse(savedAttributes.getAttributes().isEmpty());
        savedAttributes.clear(getContext());

        UserAttributes nonSavedAttributes = new UserAttributes();
        assertTrue(nonSavedAttributes.getAttributes().isEmpty());
    }

    @Test
    public void testGetUserByFilterLessThenOrEqual() throws InterruptedException, ExecutionException, JSONException {

        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).isLessThanOrEqual("username", "z");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20le%20%22z%22");
            assertNotNull(recordedRequest);
        }


    }
    @Test
    public void testGetUserByFilterLessThan() throws InterruptedException, ExecutionException, JSONException {

        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).isLessThan("username", "z");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20lt%20%22z%22");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterGreaterThanOrEqual() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).isGreaterThanOrEqual("username", "z");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20ge%20%22z%22");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterGreaterThan() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).isGreaterThan("username", "z");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20gt%20%22z%22");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterIsPresent() throws InterruptedException, ExecutionException, JSONException {

        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).isPresent("username");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20pr");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterEndWith() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).endsWith("username", "z");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20ew%20%22z%22");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterStartWith() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).startsWith("username", "z");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20sw%20%22z%22");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterIsNotEqual() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES).isNotEqualTo("username", "z");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20ne%20%22z%22");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterWithSortOrder() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES)
                .setSortOrder(MASFilteredRequestBuilder.SortOrder.ascending, "username");
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?sortBy=username&sortOrder=ascending");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterWithPagination() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES)
                .setPagination(1, 2);
        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?startIndex=1&count=2");
            assertNotNull(recordedRequest);
        }
    }

    @Test
    public void testGetUserByFilterCompound() throws InterruptedException, ExecutionException, JSONException {
        MASCallbackFuture<List<MASUser>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(userAttributes.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES)
                .setPagination(1, 2)
                .setSortOrder(MASFilteredRequestBuilder.SortOrder.descending, "username")
                .startsWith("username", "a");

        MASUser.getCurrentUser().getUsersByFilter(request, callbackFuture);
        callbackFuture.get();
        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Users?filter=username%20sw%20%22a%22&sortBy=username&sortOrder=descending&startIndex=1&count=2");
            assertNotNull(recordedRequest);
        }
    }






}
