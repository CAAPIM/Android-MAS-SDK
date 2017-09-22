/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

import android.os.Handler;

public interface DataSourceCallback {

    Handler getHandler();

    void onError(DataSourceError e);

    void onSuccess(Object value);

}
