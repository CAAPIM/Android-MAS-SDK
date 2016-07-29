/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.Identity;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.common.MASFilteredRequestBuilder;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.identity.util.IdentityUtil;
import com.ca.mas.messaging.util.MessagingConsts;
import com.ca.mas.sample.testapp.tests.instrumentation.base.MASIntegrationBaseTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;

/**
 * Test Case Suite for MAS Identity User function(s)
 * <br>
 */
@RunWith(AndroidJUnit4.class)
public class MASUserTests extends MASIntegrationBaseTest {

    UserAttributes userAttribs = null;

    /**
     * Gets the user MetaData and stores it in memory for the rest of the tests.
     */
    @Before
    public void getUserMetadata() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
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
                    result[1] = "" + e;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (InterruptedException e) {
            fail("" + e);
        }
    }

    /**
     * Gets all the user(s)containing "s" in the UserName
     */
    @Test
    public void testGetAllUsersWithContainsAttribute() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            MASUser user = MASUser.getCurrentUser();
            MASFilteredRequest filter = createFilter(null, null, 0, 0, null, userAttribs.getAttributes(), "s");
            user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
                @Override
                public void onSuccess(List<MASUser> object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "Error in GetAllUsers " + e;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }

    /**
     * Precondition/assumption: getting user(s) containing "s" in the UserName
     */
    @Test
    public void testGetAllUsersWithSortingAsecAttribute() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            MASUser user = MASUser.getCurrentUser();
            List<String> userAttributes = userAttribs.getAttributes();
            String sortAttrib = (userAttributes == null || userAttributes.size() == 0) ? null : userAttributes.get(0);
            MASFilteredRequest filter = createFilter(MASFilteredRequestBuilder.SortOrder.ascending, sortAttrib, 0, 0, null, userAttributes, "s");
            user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
                @Override
                public void onSuccess(List<MASUser> object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "Error in GetAllUsers " + e;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }

    /**
     * Precondition/assumption: getting user(s) containing "s" in the UserName
     */
    @Test
    public void testGetAllUsersWithSortingDescAttribute() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            MASUser user = MASUser.getCurrentUser();
            List<String> userAttributes = userAttribs.getAttributes();
            String sortAttrib = (userAttributes == null || userAttributes.size() == 0) ? null : userAttributes.get(0);
            MASFilteredRequest filter = createFilter(MASFilteredRequestBuilder.SortOrder.descending, sortAttrib, 0, 0, null, userAttributes, "s");
            user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
                @Override
                public void onSuccess(List<MASUser> object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "Error in GetAllUsers " + e;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }


    /**
     * Precondition/assumption: getting user(s) containing "s" in the UserName
     */
    @Test
    public void testGetAllUsersWithPagination() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            MASUser user = MASUser.getCurrentUser();
            List<String> userAttributes = userAttribs.getAttributes();
            MASFilteredRequest filter = createFilter(null, null, 1, 3, null, userAttributes, "s");
            user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
                @Override
                public void onSuccess(List<MASUser> object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "Error in GetUsers with Pagination " + e;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }

    /**
     * Precondition/assumption: getting user(s) containing "s" in the UserName
     */
    @Test
    public void testGetAllUsersWithInvalidPagination() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            MASUser user = MASUser.getCurrentUser();
            List<String> userAttributes = userAttribs.getAttributes();
            // start index = -10 will automatically be converted to 1
            // Count of -10 implies that , pagination is not there
            MASFilteredRequest filter = createFilter(null, null, -10, -10, null, userAttributes, "s");
            user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
                @Override
                public void onSuccess(List<MASUser> object) {
                    result[0] = true;
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "Error in GetUsers with Pagination " + e;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }


    @Test
    public void testGetUserById() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
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
                    result[1] = "Error in Get Users with id " + user.getId();
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }


    @Test
    public void testGetUserByInvalidId() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            final MASUser user = MASUser.getCurrentUser();
            user.getUserById("invalidId", new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser object) {
                    result[0] = false;
                    result[1] = "Expected exception as the id is invalid ";
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = true;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }

    @Test
    public void testGetUserByUserName() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            final MASUser user = MASUser.getCurrentUser();
            MASFilteredRequest filter = createFilter(null, null, 0, 0, null, userAttribs.getAttributes(), user.getUserName());
            user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
                @Override
                public void onSuccess(List<MASUser> object) {
                    if (object == null) {
                        result[0] = false;
                        result[1] = "Null User";
                    } else if (object.size() != 1) {
                        result[0] = false;
                        result[1] = "More than one user for username! ";
                    } else if (!object.get(0).getUserName().equals(user.getUserName())) {
                        result[0] = false;
                        result[1] = "Returned user doesn not have the expected username";
                    } else {
                        result[0] = true;
                    }
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "Error in testGetUserByUserName " + e;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }

    @Test
    public void testGetUserByInvalidUserName() {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            final Object[] result = {false, "unknown"};
            Context ctx = InstrumentationRegistry.getInstrumentation().getTargetContext();
            final MASUser user = MASUser.getCurrentUser();
            MASFilteredRequest filter = createFilter(null, null, 0, 0, null, userAttribs.getAttributes(), "invalidUser");
            user.getUsersByFilter(filter, new MASCallback<List<MASUser>>() {
                @Override
                public void onSuccess(List<MASUser> object) {
                    if (object == null) {
                        result[0] = false;
                        result[1] = "Null User";
                    } else if (object.size() > 0) {
                        result[0] = false;
                        result[1] = "Expected no users";
                    } else {
                        result[0] = true;
                    }
                    latch.countDown();
                }

                @Override
                public void onError(Throwable e) {
                    result[0] = false;
                    result[1] = "Error: " + e;
                    latch.countDown();
                }
            });
            await(latch);
            if (!(boolean) result[0]) {
                fail("Reason: " + result[1]);
            }
        } catch (Exception e) {
            fail("" + e);
        }
    }

    @Test
    public void testGetUserThumbnail() {
        final MASUser user = MASUser.getCurrentUser();
        Bitmap bitmap = user.getThumbnailImage();
        assertNotNull(bitmap);
    }


    /**
     * Utility method for creating Filter
     *
     * @return
     */
    private MASFilteredRequest createFilter(MASFilteredRequestBuilder.SortOrder sortOrder, String sortAttr, int pageStart, int pageEnd, String op, List<String> userAttributes, String userFilter) {
        String attr = MessagingConsts.KEY_DISPLAY_NAME;
        if (TextUtils.isEmpty(op)) {
            op = "co";
        }
        // add the query filters
        MASFilteredRequest frb = IdentityUtil.createFilter(userAttributes, IdentityConsts.KEY_USER_ATTRIBUTES, userFilter, op, attr);
        // add pagination
        frb.setPagination(pageStart, pageEnd);

        // add sorting
        if (sortOrder != null) {
            frb.setSortOrder(sortOrder, sortAttr);
        }
        return frb;
    }


}
