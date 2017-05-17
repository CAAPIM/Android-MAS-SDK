/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.MASLoginTestBase;
import com.ca.mas.identity.user.ScimUser;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;

public class MASUserTest extends MASLoginTestBase {

    @Test
    public void getCurrentUserTest() throws Exception {
        Assert.assertNotNull(MASUser.getCurrentUser());
        MASUser masUser = MASUser.getCurrentUser();
        assertEquals("admin", masUser.getUserName());
        assertEquals("admin", masUser.getId());
        assertEquals("Admin", masUser.getName().getFamilyName());

    }

}
