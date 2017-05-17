/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import android.graphics.Bitmap;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;

import java.util.List;

/**
 * The <b>MASUserIdentity</b> interface enables the Identity Management feature for the authenticated user.
 */
public interface MASUserIdentity {

    /**
     * Retrieves a {@link MASUser}' matching the id.
     *
     * @param id       The id used to locate the {@link MASUser}
     * @param callback Callback with either the {@link MASUser} or error.
     */

    void getUserById(String id, MASCallback<MASUser> callback);

    /**
     * Retrieves {@link MASUser} objects that matches the {@link MASFilteredRequest}.
     *
     * @param filteredRequest The {@link MASFilteredRequest} to filter results.
     * @param callback               Callback with either the list of {@link MASUser} objects or error
     */

    void getUsersByFilter(MASFilteredRequest filteredRequest, MASCallback<List<MASUser>> callback);

    /**
     * Retrieves the supported SCIM attribute options.
     *
     * @param callback Callback with either the UserAttributes object or error
     */
    void getUserMetaData(MASCallback<UserAttributes> callback);

}
