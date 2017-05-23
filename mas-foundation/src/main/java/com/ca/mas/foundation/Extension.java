/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class Extension {

    private static <T> T getDefaultImpl(Class<T> tClass) {
        return (T) java.lang.reflect.Proxy.newProxyInstance(
                tClass.getClassLoader(),
                new java.lang.Class[]{tClass},
                new java.lang.reflect.InvocationHandler() {

                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        throw new UnsupportedOperationException("Stub! Please provide module dependency.");
                    }
                });
    }

    private Extension() {
    }

    static void inject(Object instance) {

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(MASExtension.class)) {
                field.setAccessible(true);
                try {
                    Constructor constructor = Class.forName(field.getType().getName() + "Impl").getConstructor();
                    field.set(instance, constructor.newInstance());
                } catch (Exception e) {
                    try {
                        field.set(instance, getDefaultImpl(field.getType()));
                    } catch (IllegalAccessException ignore) {
                        if (MAS.DEBUG)
                            Log.w(MAS.TAG, "Failed to inject module implementation.", ignore);
                    }
                }
            }
        }

    }

}
