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

public class MASUserTest extends MASLoginTestBase {

    @Test
    public void getCurrentUserTest() throws Exception {
        Assert.assertNotNull(MASUser.getCurrentUser());
        MASUser masUser = MASUser.getCurrentUser();
        assertEquals("admin", masUser.getUserName());
        assertEquals("admin", masUser.getId());
        assertEquals("Admin", masUser.getName().getFamilyName());

    }

    @Ignore("Remove mas-connecta from mas module to test")
    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedMessagingModule() throws Exception {
        MASUser.getCurrentUser().sendMessage((MASMessage)null, MASUser.getCurrentUser(), null);
    }

    @Ignore("Remove mas-identity-management from mas module to test")
    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedIdentityModule() throws Exception {
        MASUser.getCurrentUser().getUserById("test",null);
    }

    @Ignore("Remove mas-identity-management from mas module to test")
    @Test(expected = UnsupportedOperationException.class)
    public void testUnsupportedIdentyModuleForMASGroup() throws Exception {
        MASGroup.newInstance().save(null);
    }



}
