/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.token.IdToken;

/**
 * ID token as described in <a href="https://tools.ietf.org/html/draft-jones-oauth-jwt-bearer-03">ID Token</a>
 * By default, the type would set to urn:ietf:params:oauth:grant-type:jwt-bearer
 */
public class MASIdToken extends IdToken {

    private MASIdToken(String value, String type) {
        super(value, type);
    }

    public static class Builder {
        private String value;
        private String type = IdToken.JWT_DEFAULT;

        public Builder value(String value) {
            this.value = value;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public MASIdToken build() {
            if (value == null) {
                throw new NullPointerException("Token value cannot be null.");
            }
            return new MASIdToken(value, type);
        }
    }
}
