/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.MASLoginTestBase;
import com.ca.mas.messaging.MASMessage;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

public class MASUserTest extends MASLoginTestBase {

    @Test
    public void getCurrentUserTest() throws Exception {
        Assert.assertNotNull(MASUser.getCurrentUser());
        MASUser masUser = MASUser.getCurrentUser();
        assertEquals("admin", masUser.getUserName());
        assertEquals("admin", masUser.getId());
        assertEquals("Admin", masUser.getName().getFamilyName());
    }

    @Test
    public void getAccessToken() throws Exception {
        MASUser masUser = MASUser.getCurrentUser();
        assertNotNull(masUser.getAccessToken());
    }


}
