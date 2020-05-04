/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.store;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.datasource.DataSource;
import com.ca.mas.core.datasource.DataSourceException;

import static com.ca.mas.foundation.MAS.TAG;

public class PrivateTokenStorage implements OAuthTokenContainer {

    public enum KEY {
        PREF_ACCESS_TOKEN,
        PREF_REFRESH_TOKEN,
        PREF_EXPIRY_UNIXTIME,
        PREF_GRANTED_SCOPE,
        PREF_ID_TOKEN,
        PREF_ID_TOKEN_TYPE
    }

    private DataSource<String, String> storage;

    public PrivateTokenStorage(@NonNull DataSource storage){
        this.storage = storage;
    }

    @Override
    public void saveAccessToken(String accessToken, String refreshToken, long expiresInSec, String grantedScope) {
        long now = System.currentTimeMillis();
        long expiresInMillis = expiresInSec * 1000L;
        long expiry = now + expiresInMillis;

        storage.put(getKey(KEY.PREF_ACCESS_TOKEN.name()), accessToken);
        storage.put(getKey(KEY.PREF_REFRESH_TOKEN.name()), refreshToken);
        storage.put(getKey(KEY.PREF_EXPIRY_UNIXTIME.name()), Long.toString(expiry));
        storage.put(getKey(KEY.PREF_GRANTED_SCOPE.name()), grantedScope);
    }

    @Override
    public String getAccessToken() {
        try {
            return storage.get(getKey(KEY.PREF_ACCESS_TOKEN.name()));
        } catch (DataSourceException e) {
            return null;
        }
    }

    @Override
    public String getRefreshToken() {
        try {
            return storage.get(getKey(KEY.PREF_REFRESH_TOKEN.name()));
        } catch (DataSourceException e) {
            return null;
        }
    }

    @Override
    public synchronized String takeRefreshToken() {
        String refreshToken = getRefreshToken();
        if (refreshToken != null) {
            storage.remove(getKey(KEY.PREF_REFRESH_TOKEN.name()));
        }
        return refreshToken;
    }

    @Override
    public String getGrantedScope() {
        try {
            return storage.get(getKey(KEY.PREF_GRANTED_SCOPE.name()));
        } catch (DataSourceException e) {
            return null;
        }
    }

    /**
     * @return expiry date as millis since the epoch, or 0 if not set.
     */
    @Override
    public long getExpiry() {
        try {
            String r = storage.get(getKey(KEY.PREF_EXPIRY_UNIXTIME.name()));
            if (r != null) {
                return Long.parseLong(r);
            } else {
                return 0;
            }
        } catch (DataSourceException e) {
            return 0;
        }
    }

    @Override
    public void clear() {
        for (KEY k : KEY.values()) {
            storage.remove(getKey(k.name()));
        }
    }

    @Override
    public void clearAll() {
        Log.d(TAG,"Escalation PrivateTokenStorage clearAll");
        storage.removeAll(null);
    }

    private String getKey(String name) {
        return ConfigurationManager.getInstance().getConnectedGateway().toString() + name;
    }

}
