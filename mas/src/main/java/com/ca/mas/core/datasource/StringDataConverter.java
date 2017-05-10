/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

public class StringDataConverter implements DataConverter <String, String>{

    @Override
    public String convert(String key, byte[] value) {
        return new String(value);
    }
}
