/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

/**
 * Registered claim names.
 */
public final class MASClaimsConstants {
    private MASClaimsConstants() {
        throw new IllegalAccessError("Strings class");
    }

    static final String ISSUER = "iss";
    static final String SUBJECT = "sub";
    static final String AUDIENCE = "aud";
    static final String EXPIRATION = "exp";
    static final String NOT_BEFORE = "nbf";
    static final String ISSUED_AT = "iat";
    static final String JWT_ID = "jti";
    static final String CONTENT = "content";
    static final String CONTENT_TYPE = "content-type";
}
