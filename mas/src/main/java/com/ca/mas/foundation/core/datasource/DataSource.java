/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

import java.util.List;

public interface DataSource<K, T>{

    void put(K key,T value);

    void put(K key, T value, DataSourceCallback callback);

    T get(K key);

    void get(K key, DataSourceCallback callback);

    void remove(K key);

    void remove(K key, DataSourceCallback callback);

    void removeAll(Object filter);

    void removeAll(Object filter, DataSourceCallback callback);

    List<K> getKeys(Object filter);

    void getKeys(Object filter, DataSourceCallback callback);

    boolean isReady();

    void unlock();

}
