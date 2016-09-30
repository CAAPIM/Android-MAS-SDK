/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.identity.user.ScimUser;

public interface UserRepository {

    /**
     * Retrieve the user profile.
     * @param username The username should be optional, should able to retrieve the user
     *                 profile base on the Access Token.
     * @param result Scim user profile.
     */
    void findByUsername(final String username, final MASCallback<ScimUser> result);

}
