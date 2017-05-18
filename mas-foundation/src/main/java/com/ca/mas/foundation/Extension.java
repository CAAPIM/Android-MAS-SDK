package com.ca.mas.foundation;

import android.util.Log;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

public class Extension {

    private Extension() {
    }

    static void inject(Object instance) {

        for (Field field : instance.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(MASExtension.class)) {
                try {
                    Constructor constructor = Class.forName(field.getType().getName() + "Impl").getConstructor();
                    field.setAccessible(true);
                    field.set(instance, constructor.newInstance());
                } catch (Exception ignore) {
                    if (MAS.DEBUG) Log.w(MAS.TAG, "Missing module.", ignore);
                }

            }
        }

    }

}
