/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas;

import android.os.Build;

import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

public class AndroidVersionAwareTestRunner extends BlockJUnit4ClassRunner {

    public AndroidVersionAwareTestRunner(Class testClass) throws InitializationError {
        super(testClass);
    }


    @Override
    protected void runChild(FrameworkMethod method, RunNotifier notifier) {
        MinTargetAPI minCondition = method.getDeclaringClass().getAnnotation(MinTargetAPI.class);
        MaxTargetAPI maxCondition = method.getDeclaringClass().getAnnotation(MaxTargetAPI.class);

        //Class Level
        if (minCondition != null && minCondition.value() > Build.VERSION.SDK_INT) {
            notifier.fireTestIgnored(describeChild(method));
            return;
        }
        if (maxCondition != null && maxCondition.value() < Build.VERSION.SDK_INT) {
            notifier.fireTestIgnored(describeChild(method));
            return;
        }

        //Method Level
        minCondition = method.getAnnotation(MinTargetAPI.class);
        maxCondition = method.getAnnotation(MaxTargetAPI.class);

        if (minCondition != null && minCondition.value() >= Build.VERSION.SDK_INT) {
            notifier.fireTestIgnored(describeChild(method));
            return;
        }
        if (maxCondition != null && maxCondition.value() <= Build.VERSION.SDK_INT) {
            notifier.fireTestIgnored(describeChild(method));
            return;
        }

        super.runChild(method, notifier);

    }
}
