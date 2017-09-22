/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

import android.content.Context;

import org.json.JSONObject;

import java.lang.reflect.Constructor;

public class DataSourceFactory {

    public static <K, V> DataSource<K, V> getStorage(Context context, Class<? extends DataSource> c, JSONObject param, DataConverter converter) {

        try {
            Constructor constructor = c.getConstructor(Context.class, JSONObject.class, DataConverter.class);
            return (DataSource) constructor.newInstance(context, param, converter);
        } catch (Exception e) {
            throw new DataSourceException(e);
        }

    }

    public static <K, V> DataSource<K, V> getStorage(Context context, Class<? extends DataSource> c, JSONObject param) {
        try {
            Constructor constructor = c.getConstructor(Context.class, JSONObject.class, DataConverter.class);
            return (DataSource) constructor.newInstance(context, param);
        } catch (Exception e) {
            throw new DataSourceException(e);
        }
    }

}
