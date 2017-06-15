/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.Identity;

import android.support.test.runner.AndroidJUnit4;

import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASDevice;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.common.MASFilteredRequestBuilder;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.fail;

/**
 * Test Case Suite for MAS Identity User function(s)
 * <br>
 */
@RunWith(AndroidJUnit4.class)
public class MASUserTests extends MASIntegrationBaseTest {

    UserAttributes userAttribs = null;

    @Test
    public void testPersistUserProfile() throws Exception {
        Assert.assertNotNull(MASUser.getCurrentUser());
        TokenManager tm = StorageProvider.getInstance().getTokenManager();
        String storedUserProfile = tm.getUserProfile();
        Assert.assertNotNull(storedUserProfile);
        JSONObject source = new JSONObject(storedUserProfile);
        assertEquals(getUsername(), source.getString("userName"));

        MASUser user = MASUser.getCurrentUser();
        assertEquals(user.getEmailList().size(), 1);
        assertEquals(getUsername(), user.getUserName());

        final CountDownLatch latch = new CountDownLatch(1);
        //Switch user and make sure the persisted user is the new user.
        MASUser.login("admin", "7layer", new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                latch.countDown();

            }

            @Override
            public void onError(Throwable e) {
                latch.countDown();
            }
        });

        try {
            await(latch);
            user = MASUser.getCurrentUser();
            assertEquals("admin", user.getUserName());
            storedUserProfile = tm.getUserProfile();
            Assert.assertNotNull(storedUserProfile);
            JSONObject source2 = new JSONObject(storedUserProfile);
            assertEquals("admin", source2.getString("userName"));

        } catch (InterruptedException e) {
            fail();
        }

        final CountDownLatch latch2 = new CountDownLatch(1);
        //Resume the original login user
        MASUser.login(getUsername(), getPassword(), new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                latch2.countDown();

            }

            @Override
            public void onError(Throwable e) {
                latch2.countDown();
            }
        });

        try {
            await(latch2);
        } catch (InterruptedException e) {
            fail();
        }


    }

    /**
     * Gets the user MetaData and stores it in memory for the rest of the tests.
     */
    @Before
    public void getUserMetadata() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MASUser.getCurrentUser().getUserMetaData(new MASCallback<UserAttributes>() {
            @Override
            public void onSuccess(UserAttributes object) {
                userAttribs = object;
                result[0] = true;
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

    /**
     * Gets all the user(s)containing "s" in the UserName
     */
    @Test
    public void testGetAllUsersWithContainsAttribute() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MASUser user = MASUser.getCurrentUser();
        MASFilteredRequest filter = new MASFilteredRequest(userAttribs.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES);
        filter.contains(IdentityConsts.KEY_USERNAME, "s");
        user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
            @Override
            public void onSuccess(List<MASUser> object) {
                result[0] = true;
                if (object.size() == 0) {
                    result[0] = false;
                }
                for (MASUser u : object) {
                    if (!u.getUserName().contains("s")) {
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

    /**
     * Precondition/assumption: getting user(s) containing "s" in the UserName
     */
    @Test
    public void testGetAllUsersWithSortingAsecAttribute() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MASUser user = MASUser.getCurrentUser();
        MASFilteredRequest filter = new MASFilteredRequest(userAttribs.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES);
        filter.setSortOrder(MASFilteredRequestBuilder.SortOrder.ascending, IdentityConsts.KEY_USERNAME);
        user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
            @Override
            public void onSuccess(List<MASUser> object) {
                result[0] = true;

                MASUser first = object.get(0);
                for (int i = 1; i < object.size(); i++) {
                    if (first.getUserName().compareTo(object.get(i).getUserName()) > 0) {
                        result[0] = false;
                    } else {
                        first = object.get(i);
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

    /**
     * Precondition/assumption: getting user(s) containing "s" in the UserName
     */

    @Test
    public void testGetAllUsersWithSortingDescAttribute() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MASUser user = MASUser.getCurrentUser();
        MASFilteredRequest filter = new MASFilteredRequest(userAttribs.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES);
        filter.setSortOrder(MASFilteredRequestBuilder.SortOrder.descending, IdentityConsts.KEY_USERNAME);
        user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
            @Override
            public void onSuccess(List<MASUser> object) {
                result[0] = true;

                MASUser first = object.get(0);
                for (int i = 1; i < object.size(); i++) {
                    if (first.getUserName().compareTo(object.get(i).getUserName()) < 0) {
                        result[0] = false;
                    } else {
                        first = object.get(i);
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
    public void testGetAllUsersWithPagination() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MASUser user = MASUser.getCurrentUser();
        MASFilteredRequest filter = new MASFilteredRequest(userAttribs.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES);
        //assume there are more than 3 user in the database
        filter.setPagination(1, 3);
        user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
            @Override
            public void onSuccess(List<MASUser> object) {
                if (object.size() == 3) {
                    result[0] = true;
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


    /**
     * Precondition/assumption: getting user(s) containing "s" in the UserName
     */
    @Test
    public void testGetAllUsersWithInvalidPagination() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        MASUser user = MASUser.getCurrentUser();
        List<String> userAttributes = userAttribs.getAttributes();
        // start index = -10 will automatically be converted to 1
        // Count of -10 implies that , pagination is not there
        MASFilteredRequest filter = new MASFilteredRequest(userAttribs.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES);
        filter.setPagination(-10, -10);
        user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
            @Override
            public void onSuccess(List<MASUser> object) {
                result[0] = true;
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
    public void testGetUserById() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        final MASUser user = MASUser.getCurrentUser();
        user.getUserById(user.getId(), new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser object) {
                if (object != null && object.getId().equals(user.getId())) {
                    result[0] = true;
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
    public void testGetUserByInvalidId() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        final MASUser user = MASUser.getCurrentUser();
        user.getUserById("invalidId", new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser object) {
                result[0] = false;
                latch.countDown();
            }

            @Override
            public void onError(Throwable e) {
                result[0] = true;
                latch.countDown();
            }
        });
        await(latch);
        assertTrue(result[0]);
    }

    @Test
    public void testGetUserByUserName() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        final MASUser user = MASUser.getCurrentUser();
        MASFilteredRequest filter = new MASFilteredRequest(userAttribs.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES);
        filter.contains(IdentityConsts.KEY_USERNAME, user.getUserName());

        user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
            @Override
            public void onSuccess(List<MASUser> object) {
                if (object == null) {
                    result[0] = false;
                } else if (object.size() != 1) {
                    result[0] = false;
                } else if (!object.get(0).getUserName().equals(user.getUserName())) {
                    result[0] = false;
                } else {
                    result[0] = true;
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
    public void testGetUserByInvalidUserName() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] result = {false};
        final MASUser user = MASUser.getCurrentUser();
        MASFilteredRequest filter = new MASFilteredRequest(userAttribs.getAttributes(), IdentityConsts.KEY_USER_ATTRIBUTES);
        filter.contains(IdentityConsts.KEY_USERNAME, "notExist");

        user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
            @Override
            public void onSuccess(List<MASUser> object) {
                if (object == null) {
                    result[0] = false;
                } else if (object.size() > 0) {
                    result[0] = false;
                } else {
                    result[0] = true;
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
    public void testUserAttribute() throws Exception {
        MASUser user = MASUser.getCurrentUser();
        JSONObject jsonObject = user.getSource();
        assertNotNull(user.getEmailList().get(0));
        assertNotNull(user.getAsJSONObject());
        assertNotNull(user.getAddressList());
        assertNotNull(user.getLocale());
        assertNotNull(user.getMeta());
        assertNotNull(user.getName());
        //assertNotNull(user.getNickName());
        assertEquals(user.getPassword(), "");
        assertTrue(user.isActive());
        assertNotNull(user.getPhoneList());
        assertNotNull(user.getPhotoList());
        assertNotNull(user.getPreferredLanguage());
        assertNotNull(user.getSource());
        //assertNotNull(user.getThumbnailImage());
        assertNotNull(user.getTimeZone());
        assertNotNull(user.getTitle());
        assertNotNull(user.getUserName());
        assertNotNull(user.getUserType());
        assertNotNull(user.getCardinality());
        assertNotNull(user.getDisplayName());
        assertNotNull(user.getId());
    }

    @Test
    public void testGetAuthCredentialsLoggedIn() {
        String authType = MASUser.getAuthCredentialsType();
        assertNotNull(authType);
        assertNotSame(authType, "");
    }

    @Test
    public void testGetAuthCredentialsNotLoggedInRegistered() {
        MASUser user = MASUser.getCurrentUser();
        user.logout(null);
        String authType = MASUser.getAuthCredentialsType();
        assertNotNull(authType);
        assertNotSame(authType, "");
    }

    @Test
    public void testGetAuthCredentialsNotLoggedInDeregistered() {
        MASUser user = MASUser.getCurrentUser();
        user.logout(null);
        MASDevice.getCurrentDevice().deregister(null);
        String authType = MASUser.getAuthCredentialsType();
        assertNotNull(authType);
        assertEquals(authType, "");
    }

}
