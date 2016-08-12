/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.masusermanagementsample.mas;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;

public enum LoginManager {
    INSTANCE;

    public void login(MASCallback<MASUser> callback) {
        // TODO: replace with your user credentials
        MASUser.login("admin", "7layer", callback);
    }

}
