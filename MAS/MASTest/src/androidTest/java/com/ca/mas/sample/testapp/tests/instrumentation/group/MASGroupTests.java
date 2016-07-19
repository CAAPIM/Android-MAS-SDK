/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.group;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.group.GroupAttributes;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

@RunWith(AndroidJUnit4.class)
public class MASGroupTests extends MASIntegrationBaseTest {

    public static final String MY_GROUP_NAME = UUID.randomUUID().toString();
    public static final String OTHER_USER_WITH_GROUP = "spock";
    private MASGroup masGroup;
    private MASGroup temp;

    @Before
    public void before() throws Exception {
        masGroup = MASGroup.newInstance();
        createGroup();
    }

    @After
    public void after() throws Exception {
        //Delete the created group
        if (temp != null) {
            final CountDownLatch latch = new CountDownLatch(1);
            temp.delete(new MASCallback<Void>() {
                @Override
                public void onSuccess(Void object) {
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    latch.countDown();
                }
            });

            await(latch);
        } else {
            temp = null;
        }

    }

    @Test
    public void testCreateGroup() throws Exception {
        assertNotNull(temp);
    }

    @Test
    public void testRetrieveAllGroups() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};

        MASFilteredRequest builder = new MASFilteredRequest(getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES);

        masGroup.getGroupsByFilter(builder, new MASCallback<List<MASGroup>>() {
            @Override
            public void onSuccess(List<MASGroup> groupList) {
                for (MASGroup group : groupList) {
                    if (group.getGroupName().equals(MY_GROUP_NAME)) {
                        if (group.getOwner().getValue().equals(MASUser.getCurrentUser().getUserName())) {
                            result[0] = true;
                        }
                        break;
                    }
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);

        assertTrue(result[0]);

    }


    @Test
    public void testRetrieveAllGroupsWithPagination() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};

        MASFilteredRequest builder = new MASFilteredRequest(getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES);
        builder.setPagination(1,10);

        masGroup.getGroupsByFilter(builder, new MASCallback<List<MASGroup>>() {
            @Override
            public void onSuccess(List<MASGroup> groupList) {
                result[0]=true;
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);

        assertTrue(result[0]);

    }

    @Test
    public void testRetrieveAllGroupsWithInvalidPagination() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};

        MASFilteredRequest builder = new MASFilteredRequest(getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES);
        builder.setPagination(-1,10);

        masGroup.getGroupsByFilter(builder, new MASCallback<List<MASGroup>>() {
            @Override
            public void onSuccess(List<MASGroup> groupList) {
                result[0]=true;
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);

        assertTrue(result[0]);

    }

    @Test
    public void testAddMemberToGroup() throws Exception {
        final MASUser[] spock = {null};
        final CountDownLatch getUserLatch = new CountDownLatch(1);
        MASUser.getCurrentUser().getUserById(OTHER_USER_WITH_GROUP, new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser object) {
                spock[0] = object;
                getUserLatch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                getUserLatch.countDown();
            }
        });
        await(getUserLatch);

        temp.addMember(new MASMember(spock[0]));
        temp.addMember(new MASMember(MASUser.getCurrentUser()));

        final boolean[] result = new boolean[1];

        final CountDownLatch latch = new CountDownLatch(1);
        temp.save(new MASCallback<MASGroup>() {

            @Override
            public void onSuccess(MASGroup object) {
                for (MASMember m : object.getMembers()) {
                    if (m.getValue().equals(spock[0].getUserName())) {
                        result[0] = true;
                    }
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);

        assertTrue(result[0]);

    }


    @Test
    public void testGetAttributes() throws Exception {
        List<String> result = getAttributes();
        assertTrue(result.size() > 1);
    }


    @Test
    public void testRetrieveGroupById() throws Exception {

        final boolean[] result = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(1);
        masGroup.getGroupById(temp.getId(), new MASCallback<MASGroup>() {

            @Override
            public void onSuccess(MASGroup object) {
                result[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);
        assertTrue(result[0]);
    }

    /** Pending for MCT 185 */
    @Test()
    public void testRemoveMemberFromGroup() throws Exception {
        final boolean[] result = new boolean[1];
        result[0] = true;
        final CountDownLatch latch = new CountDownLatch(1);
        temp.removeMember(new MASMember(MASUser.getCurrentUser()));
        temp.save(new MASCallback<MASGroup>() {

            @Override
            public void onSuccess(MASGroup object) {
                for (MASMember m : object.getMembers()) {
                    if (m.getValue().equals(getUsername())) {
                        result[0] = false;
                    }
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                result[0] = false;
                latch.countDown();
            }
        });

        await(latch);
        assertTrue(result[0]);
    }

    @Test
    public void testRetrieveGroupByName() throws Exception {

        final boolean[] result = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(1);
        masGroup.getGroupByGroupName(MY_GROUP_NAME, new MASCallback<List<MASGroup>>() {

            @Override
            public void onSuccess(List<MASGroup> groupList) {
                for (MASGroup group : groupList) {
                    if (group.getGroupName().equals(MY_GROUP_NAME)) {
                        result[0] = true;
                        break;
                    }
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);
        assertTrue(result[0]);
    }

    @Test
    public void testRetrieveGroupsbyFilteredRequest() throws Exception {
        MASFilteredRequest builder = new MASFilteredRequest(getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES);
        builder.isEqualTo("displayName", MY_GROUP_NAME);
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = new boolean[1];
        masGroup.getGroupsByFilter(builder, new MASCallback<List<MASGroup>>() {
            @Override
            public void onSuccess(List<MASGroup> groupList) {
                for (MASGroup group : groupList) {
                    if (group.getGroupName().equals(MY_GROUP_NAME)) {
                        result[0] = true;
                        break;
                    }
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(result[0]);

    }

    @Test
    public void removeMembersFromGroupWhenNotTheOwnerOfGroup() throws Exception {
        final CountDownLatch latch = new CountDownLatch(2);
        final boolean[] result = new boolean[1];

        MASFilteredRequest builder = new MASFilteredRequest(getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES);
        builder.isEqualTo("owner.value", OTHER_USER_WITH_GROUP);
        masGroup.getGroupsByFilter(builder, new MASCallback<List<MASGroup>>() {

            @Override
            public void onSuccess(List<MASGroup> groupList) {
                if (groupList.isEmpty()) {
                    latch.countDown();
                } else {
                    for (MASGroup group : groupList) {
                        group.delete(new MASCallback<Void>() {
                            @Override
                            public void onSuccess(Void object) {
                                latch.countDown();
                            }

                            @Override
                            public void onError(Throwable e) {
                                if (((TargetApiException) e.getCause()).getResponse().getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                                    result[0] = true;
                                }
                                latch.countDown();
                            }
                        });
                        break;
                    }
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();

            }
        });
        await(latch);
        assertTrue(result[0]);

    }

    @Test
    public void testRetrievesGroupsByMember() throws Exception {
        testAddMemberToGroup();
        final boolean[] result = new boolean[1];
        final CountDownLatch latch = new CountDownLatch(1);
        MASFilteredRequest builder = new MASFilteredRequest(getAttributes(), IdentityConsts.KEY_GROUP_ATTRIBUTES);
        builder.isEqualTo("members.value", getUsername());
        masGroup.getGroupsByFilter(builder, new MASCallback<List<MASGroup>>() {

            @Override
            public void onSuccess(List<MASGroup> groupList) {
                for (MASGroup group : groupList) {
                    if (group.getGroupName().equals(MY_GROUP_NAME)) {
                        result[0] = true;
                        break;
                    }
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(result[0]);
    }

    @Test
    public void testSaveAttributes() throws Exception {

        final CountDownLatch latch = new CountDownLatch(1);

        masGroup.getGroupMetaData(new MASCallback<GroupAttributes>() {
            @Override
            public void onSuccess(GroupAttributes object) {
                object.save(InstrumentationRegistry.getInstrumentation().getTargetContext());
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);
        GroupAttributes ga = new GroupAttributes();
        assertTrue(ga.getAttributes().size() > 1);
        ga.clear(InstrumentationRegistry.getInstrumentation().getTargetContext());

    }

    private void createGroup() throws InterruptedException {
        temp = MASGroup.newInstance();
        temp.setGroupName(MY_GROUP_NAME);

        final CountDownLatch latch = new CountDownLatch(1);

        temp.save(new MASCallback<MASGroup>() {
            @Override
            public void onSuccess(MASGroup object) {
                if (object.getId() != null) {
                    temp = object;
                }
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);

    }

    private List<String> getAttributes() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final GroupAttributes[] attributes = new GroupAttributes[1];

        masGroup.getGroupMetaData(new MASCallback<GroupAttributes>() {
            @Override
            public void onSuccess(GroupAttributes object) {
                attributes[0] = object;
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        await(latch);
        return attributes[0].getAttributes();
    }


}
