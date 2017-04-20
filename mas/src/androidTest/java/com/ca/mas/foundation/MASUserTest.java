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
        MASUser user = MASUser.getCurrentUser();
        JSONObject jsonObject = user.getSource();
        assertNotNull(user.getEmailList().get(0));
        assertNotNull(user.getAsJSONObject());
        assertNotNull(user.getAddressList());
        //assertNotNull(user.getLocale());
        //assertNotNull(user.getMeta());
        assertNotNull(user.getName());
        //assertNotNull(user.getNickName());
        //assertEquals(user.getPassword(), "");
        //assertTrue(user.isActive());
        assertNotNull(user.getPhoneList());
        assertNotNull(user.getPhotoList());
        //assertNotNull(user.getPreferredLanguage());
        assertNotNull(user.getSource());
        //assertNotNull(user.getThumbnailImage());
        //assertNotNull(user.getTimeZone());
        //assertNotNull(user.getTitle());
        //assertNotNull(user.getUserName());
        //assertNotNull(user.getUserType());
        //assertNotNull(user.getCardinality());
        //assertNotNull(user.getDisplayName());
        assertNotNull(user.getId());
    }
}
