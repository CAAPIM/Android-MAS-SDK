/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.UserRepository;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.foundation.util.FoundationUtil;
import com.ca.mas.identity.util.IdentityConsts;

import org.json.JSONException;
import org.json.JSONObject;

/**
 ** Retrieve user profile based on SCIM.
 * @see <a href="https://tools.ietf.org/html/rfc7643">SCIM</a>
 */
public class ScimUserRepository implements UserRepository {

    @Override
    public void findByUsername(final String username, final MASCallback<ScimUser> result) {

        //We should use /me instead of making 2 calls to retrieve the username and user profile.
        //The server will provide /me API in the near future.
        if (username == null) {
            final MASRequest request = new MASRequest.MASRequestBuilder(FoundationUtil.getUserInfo())
                    .password()
                    .build();

            MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

                @Override
                public void onSuccess(MASResponse<JSONObject> response) {

                    try {
                        String u = response.getBody().getContent().getString(IdentityConsts.KEY_MY_PREF_UNAME);
                        UserIdentityManager.getInstance().getUserById(u, new MASCallback<MASUser>() {

                            @Override
                            public void onSuccess(MASUser user) {
                                Callback.onSuccess(result, user);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Callback.onError(result, e);
                            }
                        });

                    } catch (JSONException e) {
                        onError(e);
                    }

                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(result, e);
                }
            });

        } else {
            UserIdentityManager.getInstance().getUserById(username, new MASCallback<MASUser>() {

                @Override
                public void onSuccess(MASUser object) {
                    Callback.onSuccess(result, object);
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(result, e);
                }
            });
        }
    }

}

