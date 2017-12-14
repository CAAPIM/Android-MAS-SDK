/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.connecta.client;

import com.ca.mas.foundation.MASCallback;

interface MqttConnecta {

    /**
     * Populated required attributes for the provided connect options
     */
    void init(MASConnectOptions connectOptions, MASCallback<Void> masCallback);

    /**
     * @return The broker uri
     */
    String getServerUri();

    /**
     * @return The client id to connect to the broker.
     */
    String getClientId();

}
