/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.testapp.tests.instrumentation.storage;

import com.ca.mas.foundation.MASConstants;

public class MASSecureStorageForUserApplication extends MASSecureStorageTests {

    @Override
    protected int getMode() {
        return MASConstants.MAS_USER | MASConstants.MAS_APPLICATION;
    }
}
