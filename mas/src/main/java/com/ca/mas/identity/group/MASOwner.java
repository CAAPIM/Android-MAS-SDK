/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.group;

import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.user.User;

import org.json.JSONException;

/**
 * <p><b>MASOwner</b> is a thinly wrapped User object that represents the one and only owner of an ad-hoc group.</p>
 * {@code
 * "owner": {
 * "value": "string",
 * "ref": "string",
 * "display": "string"
 * }
 * }
 */
public class MASOwner {

    private String value;
    private String ref;
    private String display;

    /**
     * <b>Description:</b> Convenience constructor to create an MASOwner from a MASUser.
     *
     * @param user the MASUser object.
     */
    public MASOwner(MASUser user) {
        value = user.getId();
        display = user.getDisplayName();
    }

    public MASOwner(String value, String ref, String display) {
        this.value = value;
        this.ref = ref;
        this.display = display;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return String the display name.
     */
    public String getDisplay() {
        return display;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return String the MASOwner reference URL.
     */
    public String getRef() {
        return ref;
    }

    /**
     * <b>Description:</b> Getter.
     *
     * @return String the MASOwner value.
     */
    public String getValue() {
        return value;
    }

}
