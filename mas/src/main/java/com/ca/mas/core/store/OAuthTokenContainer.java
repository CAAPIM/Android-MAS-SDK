/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.store;

public interface OAuthTokenContainer {

    void saveAccessToken(String accessToken, String refreshToken, long expiresInSec, String grantedScope);

    String getAccessToken();

    String getRefreshToken();

    String getGrantedScope();

    String takeRefreshToken();

    /**
     * @return expiry date as millis since the epoch, or 0 if not set.
     */
    long getExpiry();

    void clear();

    void clearAll();

}
