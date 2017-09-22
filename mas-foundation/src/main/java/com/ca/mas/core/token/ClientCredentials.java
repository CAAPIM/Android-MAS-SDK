/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.token;

public class ClientCredentials {

    private String masterClientId;
    private String clientId;
    private String clientSecret;
    private Long clientExpiration;

    public ClientCredentials (String masterClientId, String clientId, String clientSecret, Long clientExpiration) {
        this.masterClientId = masterClientId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.clientExpiration = clientExpiration;
    }

    public String getMasterClientId() {
        return masterClientId;
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
