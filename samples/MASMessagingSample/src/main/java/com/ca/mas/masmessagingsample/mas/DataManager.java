/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masmessagingsample.mas;

import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.MASMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public enum DataManager {
    INSTANCE;
    private Map<String, MASGroup> mGroups;
    private Map<String, MASUser> mUsers;
    private List<MASMessage> mMessages = new ArrayList<>();

    public void setGroups(Map<String, MASGroup> groups) {
        mGroups = groups;
    }

    public void addGroups(List<MASGroup> groups) {
        for (MASGroup grp : groups) {
            if (grp != null) {
                mGroups.put(grp.getGroupName(), grp);
            }
        }
    }

    public void setUsers(Map<String, MASUser> users) {
        mUsers = users;
    }

    public MASGroup getGroupById(String id) {
        return mGroups.get(id);
    }

    public MASUser getUserByMember(String id) {
        return mUsers.get(id);
    }

    public void saveUser(MASUser user) {
        mUsers.put(user.getId(), user);
    }

    public void saveMessage(MASMessage message) {
        mMessages.add(message);
    }

    public List<MASMessage> getMessages() {
        return mMessages;
    }

}
