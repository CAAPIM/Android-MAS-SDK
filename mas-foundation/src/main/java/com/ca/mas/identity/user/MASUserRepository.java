/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.UserRepository;
import com.ca.mas.identity.common.MASFilteredRequest;

/**
 * The <b>MASUserIdentity</b> interface enables the Identity Management feature for the authenticated user.
 */
public interface MASUserRepository extends MASUserIdentity, UserRepository {

    /**
     * Retrieve the user profile from the /me endpoint
     * Currently, the server does not support /me endpoint, an extra request to the /userinfo and
     * {@link MASUserIdentity#getUserById(String, MASCallback)} )} is required
     *
     * @param callback Callback to receive notification
     */
    void me(MASCallback<MASUser> callback);

}
