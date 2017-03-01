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

import org.junit.Test;

public class MASUserTest extends MASLoginTestBase {

    @Test
    public void getCurrentUserTest() throws Exception {
        Assert.assertNotNull(MASUser.getCurrentUser());
    }


}
