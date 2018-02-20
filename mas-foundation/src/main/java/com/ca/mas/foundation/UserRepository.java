/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;


public interface UserRepository {

    /**
     * Retrieve the user profile.
     * @param result Scim user profile.
     */
    void getCurrentUser(final MASCallback<MASUser> result) throws Exception;

}
