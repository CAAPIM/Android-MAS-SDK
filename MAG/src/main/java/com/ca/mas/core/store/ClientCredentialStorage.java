/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.store;

import android.support.annotation.NonNull;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.token.ClientCredentials;

public class ClientCredentialStorage implements ClientCredentialContainer {

    enum KEY {
        PREF_CLIENT_ID,
        PREF_CLIENT_SECRET,
        PREF_CLIENT_EXPIRE_TIME
    }

    private DataSource<String, String> storage;

    public ClientCredentialStorage(@NonNull DataSource storage){
        this.storage = storage;
    }

    @Override
    public void saveClientCredentials(ClientCredentials clientCredentials) {
        if (clientCredentials != null) {
            storage.put(getKey(KEY.PREF_CLIENT_ID.name()), clientCredentials.getClientId());
            storage.put(getKey(KEY.PREF_CLIENT_SECRET.name()), clientCredentials.getClientSecret());
            storage.put(getKey(KEY.PREF_CLIENT_EXPIRE_TIME.name()), Long.toString(clientCredentials.getClientExpiration()));
        }
    }

    @Override
    public Long getClientExpiration() {
        String r = storage.get(getKey(KEY.PREF_CLIENT_EXPIRE_TIME.name()));
        if (r != null) {
            return Long.parseLong(r);
        } else {
            return -1L;
        }
    }

    @Override
    public String getClientId() {
        return storage.get(getKey(KEY.PREF_CLIENT_ID.name()));
    }

    @Override
    public String getClientSecret() {
        return storage.get(getKey(KEY.PREF_CLIENT_SECRET.name()));
    }

    @Override
    public void clear() {
        for (KEY k : KEY.values()) {
            storage.remove(getKey(k.name()));
        }
    }

    @Override
    public void clearAll() {
        storage.removeAll(null);
    }


    private String getKey(String name) {
        return ConfigurationManager.getInstance().getConnectedGateway().toString() + name;
    }

}
