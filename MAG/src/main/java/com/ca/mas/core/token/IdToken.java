/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

public class IdToken {
    public static final String JWT_DEFAULT =  "urn:ietf:params:oauth:grant-type:jwt-bearer";

    private String value;
    private String type;

    public IdToken(String value, String type) {
        this.value = value;
        this.type = type;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type == null ? JWT_DEFAULT : type;
    }

}
