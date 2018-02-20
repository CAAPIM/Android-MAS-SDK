/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.IdTokenParser;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONObject;

/**
 * Retrieve user profile from JWT ID_TOKEN.
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html">Open Id</a>
 */
class JWTUserInfoRepository extends UserInfoRepository {

    private static final String USERINFO = "userinfo";

    @Override
    public void getCurrentUser(final MASCallback<MASUser> result) throws Exception {

        IdToken idToken = StorageProvider.getInstance()
                .getTokenManager()
                .getIdToken();

        if (idToken != null) {
            IdTokenParser parser = new IdTokenParser(idToken);
            JSONObject payload = parser.getPayloadAsJSONObject();
            if (payload.has(USERINFO)) {
                MASUser user = transform(payload.getJSONObject(USERINFO));
                Callback.onSuccess(result, user);
                return;
            } else if (payload.has(IdentityConsts.KEY_MY_PREF_UNAME)) {
                //consider the JWT contains the userprofile when preferred_username is defined
                //the payload.
                MASUser user = transform(payload);
                Callback.onSuccess(result, user);
            }
        }
        throw new UserNotFoundException();
    }

    private static class UserNotFoundException extends Exception {
    }
}

