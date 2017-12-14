/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import com.ca.mas.messaging.MASMessagingTest;
import com.ca.mas.messaging.MASSubscribePublishMessageTest;

import org.junit.experimental.categories.Categories;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Categories.class)
@Suite.SuiteClasses({
        MASMessagingTest.class,
        MASSubscribePublishMessageTest.class,
})

public class MASMessagingTestSuite {
}
