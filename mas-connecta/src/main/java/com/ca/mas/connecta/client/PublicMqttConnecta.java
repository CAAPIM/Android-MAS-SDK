/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.connecta.client;

import com.ca.mas.foundation.MASCallback;

import java.util.UUID;


class PublicMqttConnecta implements MqttConnecta {

    private MASConnectOptions connectOptions;
    private String clientId;

    PublicMqttConnecta(String clientId) {
        // if Client ID was not set, generate a client id
        if (clientId == null) {
            this.clientId = UUID.randomUUID().toString();
        } else {
            this.clientId = clientId;
        }
    }

    @Override
    public void init(MASConnectOptions connectOptions, MASCallback<Void> masCallback) {
        this.connectOptions = connectOptions;
        masCallback.onSuccess(null);
    }

    @Override
    public String getServerUri() {
        return connectOptions.getServerURIs()[0];
    }

    @Override
    public String getClientId() {
        return clientId;
    }
}
