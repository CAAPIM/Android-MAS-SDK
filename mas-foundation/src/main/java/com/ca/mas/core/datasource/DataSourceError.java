/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

class DataSourceError extends Error {

    DataSourceError(Throwable throwable) {
        super(throwable);
    }
}
