/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.group;

import com.ca.mas.foundation.MASUser;

/**
 * <p><b>MASMember</b> is a thinly wrapped User that contains the type, value, ref, and display specific to a MASMember.</p>
 */
public class MASMember {

    private String type;
    private String value;
    private String ref;
    private String display;

    /**
     * <b>Description:</b> Convenience constructor to create a MASMember from a MASUser.
     * @param user the MASUser object.
     */
    public MASMember(MASUser user) {
        value = user.getId();
        display = user.getDisplayName();
        ref = user.getId();
    }

    /**
     * <b>Description:</b> Convenience constructor to create a MASMember from the IdentityBase attributes.
     * @param type the MASMember type.
     * @param value the IdentityBase value.
     * @param ref the reference to the MASMember.
     * @param display the MASMember's display name.
     */
    public MASMember(String type, String value, String ref, String display) {
        this.type = type;
        this.value = value;
        this.ref = ref;
        this.display = display;
    }

    /**
     * <b>Description:</b> Getter.
     * @return String the type.
     */
    public String getType() {
        return type;
    }

    /**
     * <b>Description:</b> Setter.
     * @param type the MASMember type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * <b>Description:</b> Getter.
     * @return String the display name.
     */
    public String getDisplay() {
        return display;
    }

    /**
     * <b>Description:</b> Setter
     * @param display the MASMember display name.
     */
    public void setDisplay(String display) {
        this.display = display;
    }

    /**
     * <b>Description:</b> Getter.
     * @return String the MASMember reference URL.
     */
    public String getRef() {
        return ref;
    }

    /**
     * <b>Description:</b> Setter.
     * @param ref the MASMember reference URL.
     */
    public void setRef(String ref) {
        this.ref = ref;
    }

    /**
     * <b>Description:</b> Getter.
     * @return String the member value.
     */
    public String getValue() {
        return value;
    }

    /**
     * <b>Description:</b> Setter.
     * @param value the MASMember value.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
