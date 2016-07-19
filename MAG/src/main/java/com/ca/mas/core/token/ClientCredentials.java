/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

public class ClientCredentials {

    String clientId;
    String clientSecret;
    Long clientExpiration;

    public ClientCredentials (String clientId, String clientSecret, Long clientExpiration) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientExpiration = clientExpiration;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Long getClientExpiration() {
        return clientExpiration;
    }

}
