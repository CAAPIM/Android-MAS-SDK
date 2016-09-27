/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masusermanagementsample.mas;

import com.ca.mas.foundation.MASGroup;

import java.util.Map;

public enum GroupsManager {
    INSTANCE;
    private Map<String, MASGroup> mGroups;

    public void setGroups(Map<String, MASGroup> groups) {
        mGroups = groups;
    }

    public MASGroup getGroupById(String id) {
        return mGroups.get(id);
    }

}
