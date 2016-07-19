/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.identity.group;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;

import java.util.List;

/**
 * This interface enables Identity Management features for the MASGroup object.
 */
public interface MASGroupIdentity {

    /**
     * Retrieves all {@link MASGroup} objects that match the specified owner user ID.
     *
     * @param owner    The owner user id to be used in the search.
     * @param callback Callback  with either the list of {@link MASGroup} or an error.
     */
    void getAllGroups(String owner, MASCallback<List<MASGroup>> callback);

    /**
     * <p>Retrieves all {@link MASGroup} objects that match the specified group name.</p>
     * WARNING: this is not an 'equalTo' match on the groupName, rather it is a 'contains' match.
     *
     * @param groupName The group name to be used in the search.
     * @param callback  Callback  with either the list of {@link MASGroup} or an error.
     */
    void getGroupByGroupName(String groupName, MASCallback<List<MASGroup>> callback);

    /**
     * Retrieves all {@link MASGroup} objects that have the specified MASUser as a member.
     *
     * @param member   The group name to be used in the search.
     * @param callback Callback with either the {@link MASGroup} or an error.
     */
    void getGroupByMember(MASUser member, MASCallback<List<MASGroup>> callback);

    /**
     * Retrieves a {@link MASGroup} object matching the objectId with specific attributes.
     *
     * @param id       The id used to locate the 'MASGroup'.
     * @param callback Callback with either the {@link MASGroup} or an error.
     */
    void getGroupById(String id, MASCallback<MASGroup> callback);

    /**
     * Retrieves {@link MASGroup} objects that match the {@link MASFilteredRequest}.
     *
     * @param filteredRequest The {@link MASFilteredRequest} to filter results.
     * @param callback        Callback with either the List of {@link MASGroup} objects or an error.
     */
    void getGroupsByFilter(MASFilteredRequest filteredRequest, MASCallback<List<MASGroup>> callback);

    /**
     * Retrieves the supported SCIM attribute options.
     *
     * @param callback Callback with either the {@link GroupAttributes} object or an error.
     */
    void getGroupMetaData(MASCallback<GroupAttributes> callback);
}
