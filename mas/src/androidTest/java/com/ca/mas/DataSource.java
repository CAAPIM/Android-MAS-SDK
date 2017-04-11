/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas;

import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by andy on 2017-03-13.
 */

public class DataSource {

    private Map<String, Device> devices = new HashMap<>();

    private static DataSource instance = new DataSource();

    private DataSource() {
    }

    public static DataSource getInstance() {
        return instance;
    }

    public void store(String key, Device value) {
        devices.put(key, value);
    }

    public Device getDevice(String key) {
        return devices.get(key);
    }

    public static class Device {
        public PublicKey registeredPublicKey;

        public Device(PublicKey registeredPublicKey) {
            this.registeredPublicKey = registeredPublicKey;
        }

        public PublicKey getRegisteredPublicKey() {
            return registeredPublicKey;
        }
    }
}
