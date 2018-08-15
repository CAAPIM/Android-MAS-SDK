/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import com.ca.mas.MASStartTestBase;
import com.ca.mas.core.context.DeviceIdentifier;

import org.junit.Test;

import static junit.framework.Assert.assertNotNull;

public class MASDeviceIdentifierTest extends MASStartTestBase {

    @Test
    public void testDeviceIdentifierGenerationWithoutLogin() throws Exception {
        DeviceIdentifier id = new DeviceIdentifier();
        assertNotNull(id);
    }

}
