/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.MASLoginTestBase;

import junit.framework.Assert;

import org.json.JSONObject;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

public class MASUserTest extends MASLoginTestBase {

    @Test
    public void getCurrentUserTest() throws Exception {
        Assert.assertNotNull(MASUser.getCurrentUser());
        MASUser masUser = MASUser.getCurrentUser();
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
}
