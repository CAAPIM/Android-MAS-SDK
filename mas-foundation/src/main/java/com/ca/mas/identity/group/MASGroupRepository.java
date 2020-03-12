/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.group;

import androidx.annotation.Keep;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.identity.common.MASFilteredRequest;

import java.util.List;

/**
 * This interface enables Identity Management features for the MASGroup object.
 */
@Keep
public interface MASGroupRepository extends MASGroupIdentity {

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

    void save(MASGroup group, MASCallback<MASGroup> callback);

    void delete(MASGroup group, MASCallback<Void> callback);

}
