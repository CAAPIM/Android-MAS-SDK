/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.identity.user.User;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Retrieve user profile based on openid /userinfo endpoint.
 * @see <a href="http://openid.net/specs/openid-connect-core-1_0.html">Open Id</a>
 */
class UserInfoRepository implements UserRepository {

    @Override
    public void getCurrentUser(final MASCallback<MASUser> result) {

        final MASRequest request = new MASRequest.MASRequestBuilder(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUserInfoUri())
                .password()
                .build();

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                //Transform the userinfo to scim result
                try {
                    MASUser user = transform(response.getBody().getContent());
                    Callback.onSuccess(result, user);
                } catch (JSONException e) {
                    Callback.onError(result, e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(result, e);
            }
        });
    }

    private MASUser transform(JSONObject jsonObject) throws JSONException {
        User user =  new User();
        user.populate(jsonObject);
        return user;
    }

}

