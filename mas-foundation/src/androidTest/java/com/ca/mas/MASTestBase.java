/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assume;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@RunWith(AndroidJUnit4.class)
public abstract class MASTestBase {

    protected boolean isSkipped;

    //- Classes created under AndroidTest are not sharable with other depended modules.
    //- Module which depends on mas-foundation are required to use the *TestBase defined under AndroidTest.
    //- Using common module to store common test classes does not work, it has circular dependency mas-test -> mas-foundation -> mas-test
    //- Using sourceSets to include common test classes, however when we run jacocoTestReport on submodule, it executes all test cases in the sourceSets.
    //- The includeTest is to control which test module to run, if only want to generate coverage report for identity, please comment out other modules
    //in the list.

    private String[] includeTest = {
            "com.ca.mas.core",
            "com.ca.mas.foundation",
            "com.ca.mas.storage",
            "com.ca.mas.identity",
            "com.ca.mas.connecta",
            "com.ca.mas.messaging",
    };

    @Before
    public void check() throws Exception {
        isSkipped = true;
        for (String t: includeTest) {
            if (this.getClass().getName().startsWith(t)) {
                isSkipped = false;
                return;
            }
        }
        Assume.assumeTrue(false);

    }

    protected Context getContext() {
        return InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    protected <T> T getValue(Object instance, String attribute, Class<T> returnType) {
        Field field = null;
        try {
            field = instance.getClass().getDeclaredField(attribute);
            field.setAccessible(true);
            return returnType.cast(field.get(instance));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected <T> T invoke(Object instance, String methodName, Class<?>[] parameterTypes, Object[] args, Class<T> returnType) {
        Method method = null;
        try {
            method = instance.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return returnType.cast(method.invoke(instance, args));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}


