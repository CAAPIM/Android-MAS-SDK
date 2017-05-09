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
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.group.GroupAttributes;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.util.IdentityConsts;
import com.squareup.okhttp.mockwebserver.RecordedRequest;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.concurrent.ExecutionException;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MASGroupIdentityTest extends MASLoginTestBase {

    //Only work for Server test
    private String GROUP_NAME = "TEST_GROUP_1";

    private GroupAttributes groupAttributes;

    @Before
    public void setUp() throws Exception {
        MASCallbackFuture<GroupAttributes> callbackFuture = new MASCallbackFuture<>();
        MASGroup.newInstance().getGroupMetaData(callbackFuture);
        groupAttributes = callbackFuture.get();
    }

    @After
    public void after() throws Exception {
        MASCallbackFuture<List<MASGroup>> callbackFuture = new MASCallbackFuture<>();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(groupAttributes.getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES)
                .isEqualTo("owner.value", "admin");

        MASGroup.newInstance().getGroupsByFilter(request, callbackFuture);
        List<MASGroup> groups = callbackFuture.get();
        for (MASGroup group : groups) {
            MASCallbackFuture<Void> deleteCallback = new MASCallbackFuture<>();
            group.delete(deleteCallback);
            try {
                deleteCallback.get();
            } catch (Exception e) {
                if (((MASException) e.getCause()).getRootCause() instanceof TargetApiException) {
                    TargetApiException targetApiException = (TargetApiException) ((MASException) e.getCause()).getRootCause();
                    if (!(targetApiException.getResponse().getResponseCode() == HttpURLConnection.HTTP_NOT_FOUND)) {
                        throw e;
                    }
                }
            }
        }
        if (!isLocal()) {
            //Server may cache the data
            Thread.sleep(5000);
        }
    }

    @Test
    public void testGetUserByGroupID() throws InterruptedException, ExecutionException, JSONException {
        MASGroup masGroup = MASGroup.newInstance();
        String groupName = GROUP_NAME;
        masGroup.setGroupName(groupName);
        MASCallbackFuture<MASGroup> callbackFuture = new MASCallbackFuture<>();
        masGroup.save(callbackFuture);
        MASGroup savedGroup = callbackFuture.get();

        MASCallbackFuture<MASGroup> result = new MASCallbackFuture<>();
        masGroup.getGroupById(savedGroup.getId(), result);
        MASGroup resultMASGroup = result.get();

        assertNotNull(resultMASGroup.getAsJSONObject());
        assertEquals(groupName, resultMASGroup.getGroupName());
        assertEquals(savedGroup.getId(), resultMASGroup.getId());
        assertEquals("admin", resultMASGroup.getOwner().getValue());
        assertEquals("Admin", resultMASGroup.getOwner().getDisplay());
        assertTrue(resultMASGroup.getOwner().getRef().contains("/SCIM/MAS/"));
        assertEquals("Group", resultMASGroup.getMeta().getResourceType());

    }

    @Test
    public void testCreateGroupWithMember() throws InterruptedException, ExecutionException, JSONException {
        MASGroup masGroup = MASGroup.newInstance();
        String groupName = GROUP_NAME;
        masGroup.setGroupName(groupName);

        MASCallbackFuture<MASUser> masUserMASCallbackFuture = new MASCallbackFuture<>();
        MASUser.getCurrentUser().getUserById("sarek", masUserMASCallbackFuture);
        MASUser masUser = masUserMASCallbackFuture.get();
        assertEquals("sarek", masUser.getUserName());

        masGroup.addMember(new MASMember(masUser));

        MASCallbackFuture<MASGroup> callbackFuture = new MASCallbackFuture<>();
        masGroup.save(callbackFuture);
        MASGroup savedGroup = callbackFuture.get();

        MASCallbackFuture<MASGroup> result = new MASCallbackFuture<>();
        masGroup.getGroupById(savedGroup.getId(), result);
        MASGroup resultMASGroup = result.get();

        assertEquals("sarek", resultMASGroup.getMembers().get(0).getValue());


        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequest(IdentityDispatcher.GROUPS);
            String expected = "{\n" +
                    "  \"schemas\": [\n" +
                    "    \"urn:ietf:params:scim:schemas:core:2.0:Group\"\n" +
                    "  ],\n" +
                    "  \"displayName\": \"TEST_GROUP_1\",\n" +
                    "  \"owner\": {\n" +
                    "    \"value\": \"admin\",\n" +
                    "    \"display\": \"Admin\"\n" +
                    "  },\n" +
                    "  \"members\": [\n" +
                    "    {\n" +
                    "      \"value\": \"sarek\",\n" +
                    "      \"display\": \"Sarek\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            JSONAssert.assertEquals(expected, recordedRequest.getBody().readUtf8(), false);
        }
    }

    @Test
    public void testGetGroupByFilter() throws InterruptedException, ExecutionException, JSONException {

        testCreateGroupWithMember();

        MASCallbackFuture<List<MASGroup>> callbackFuture = new MASCallbackFuture<>();

        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(groupAttributes.getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES)
                .isEqualTo("owner.value", "admin");

        MASGroup.newInstance().getGroupsByFilter(request, callbackFuture);
        List<MASGroup> groups = callbackFuture.get();
        assertNotNull(groups);

        assertEquals(1, groups.size());
        for (MASGroup group : groups) {
            assertNotNull(group.getAsJSONObject());
            assertNotNull(group.getGroupName());
            assertNotNull(group.getId());
            assertEquals("admin", group.getOwner().getValue());
            assertEquals("Admin", group.getOwner().getDisplay());
            assertTrue(group.getOwner().getRef().contains("/SCIM/MAS/"));
        }

        if (isLocal()) {
            RecordedRequest recordedRequest = getRecordRequestWithQueryParameter("/SCIM/MAS/v2/Groups?filter=owner.value%20eq%20%22admin%22");
            assertNotNull(recordedRequest);
        }
    }



    @Test
    public void testDeleteGroup() throws Exception {
        MASCallbackFuture<List<MASGroup>> callbackFuture = new MASCallbackFuture<>();
        testCreateGroupWithMember();
        MASFilteredRequest request = (MASFilteredRequest) new MASFilteredRequest(groupAttributes.getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES)
                .isEqualTo("owner.value", "admin");

        MASGroup.newInstance().getGroupsByFilter(request, callbackFuture);
        List<MASGroup> groups = callbackFuture.get();
        for (MASGroup group : groups) {
            MASCallbackFuture<Void> deleteCallback = new MASCallbackFuture<>();
            group.delete(deleteCallback);
            deleteCallback.get();
        }
    }

    @Test
    public void testGetGroupMetaData() throws InterruptedException, ExecutionException {
        MASCallbackFuture<GroupAttributes> callbackFuture = new MASCallbackFuture<>();
        MASGroup.newInstance().getGroupMetaData(callbackFuture);
        GroupAttributes groupAttributes = callbackFuture.get();

        assertTrue(groupAttributes.getAttributes().contains("displayName"));
        assertTrue(groupAttributes.getAttributes().contains("owner.value"));
        assertTrue(groupAttributes.getAttributes().contains("owner.$ref"));
        assertTrue(groupAttributes.getAttributes().contains("owner.display"));
        assertTrue(groupAttributes.getAttributes().contains("members.value"));
        assertTrue(groupAttributes.getAttributes().contains("members.$ref"));
        assertTrue(groupAttributes.getAttributes().contains("members.type"));
        assertTrue(groupAttributes.getAttributes().contains("members.display"));


        groupAttributes.save(getContext());

        GroupAttributes savedAttributes = new GroupAttributes();
        assertFalse(savedAttributes.getAttributes().isEmpty());
        savedAttributes.clear(getContext());

        GroupAttributes nonSavedAttributes = new GroupAttributes();
        assertTrue(nonSavedAttributes.getAttributes().isEmpty());
    }
}
