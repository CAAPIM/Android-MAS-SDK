
/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.datasource;

public class LocalStoreKey {

    private String key;
    private Integer segment;
    private String createdBy;

    public LocalStoreKey(String key, Integer segment, String createdBy) {
        this.key = key;
        this.segment = segment;
        this.createdBy = createdBy;
    }

    public Integer getSegment() {
        return segment;
    }

    public String getKey() {
        return key;
    }

    public String getCreatedBy() {
        return createdBy;
    }
}

