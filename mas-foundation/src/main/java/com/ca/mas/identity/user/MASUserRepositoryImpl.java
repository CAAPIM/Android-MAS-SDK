/*
 *  Copyright (c) 2016 CA. All rights reserved.
 *
 *  This software may be modified and distributed under the terms
 *  of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.identity.user;

import android.net.Uri;
import android.support.annotation.Keep;
import android.text.TextUtils;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.foundation.FoundationConsts;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASResponseBody;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.identity.util.IdentityUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p><b>UserIdentityManager</b> handles all of the interaction between the client and the SCIM provider. This class is a
 * representation of a SCIM user, listens for WebService callbacks from SCIM requests.
 * <p>This class listens for MASRequest response through the {@link MAS#invoke(MASRequest, MASCallback)} method.
 * Before responding to the caller this class will do the following;</p>
 * <p><b>onSuccess</b>
 * <ol>
 * <li>Check to see if the response contains a single or multiple SCIM users.</li>
 * <li>If the response contains a single SCIM user, then set that user as the MASUser.mCurrentUser.</li>
 * <li>If the response contains multiple SCIM users, then add those users to the List<MASUser>.mUserList.</li>
 * </ol>
 * A multiple user response that contains a single user is treated the same as a multiple user response that contains more than one user.</p>
 * <p><b>onFail</b>
 * </p>
 */
@Keep
public class MASUserRepositoryImpl implements MASUserRepository {

    @Override
    public void getUsersByFilter(final MASFilteredRequest filteredRequest, final MASCallback<List<MASUser>> callback) {
        Uri uri = filteredRequest.createUri(MAS.getContext());
        MASRequest masRequest = new MASRequest.MASRequestBuilder(uri)
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MASResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    Callback.onSuccess(callback, parse(result.getBody().getContent()));
                } catch (JSONException e) {
                    Callback.onError(callback, e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    @Override
    public void getUserById(String id, final MASCallback<MASUser> callback) {
        Uri.Builder builder = new Uri.Builder();
        String path = IdentityUtil.getUserPath();
        builder.appendEncodedPath(path.startsWith(IdentityConsts.FSLASH) ? path.substring(1) : path);
        builder.appendPath(id);
        MASRequest masRequest = new MASRequest.MASRequestBuilder(builder.build())
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MASResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    Callback.onSuccess(callback, processUserById(result.getBody().getContent()));
                } catch (JSONException e) {
                    Callback.onError(callback, e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    @Override
    public void me(final MASCallback<MASUser> callback) {
        final MASRequest request = new MASRequest.MASRequestBuilder(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getUserInfoUri())
                .password()
                .build();

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public void onSuccess(MASResponse<JSONObject> response) {

                try {
                    String u = response.getBody().getContent().getString(IdentityConsts.KEY_MY_PREF_UNAME);
                    getUserById(u, new MASCallback<MASUser>() {

                        @Override
                        public void onSuccess(MASUser user) {
                            Callback.onSuccess(callback, user);
                        }

                        @Override
                        public void onError(Throwable e) {
                            Callback.onError(callback, e);
                        }
                    });

                } catch (JSONException e) {
                    Callback.onError(callback, e);
                }

            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    // --------- META-DATA ----------------------------------------------------
    @Override
    public void getUserMetaData(final MASCallback<UserAttributes> callback) {
        String schemaPath = IdentityUtil.getSchemasPath() + FoundationConsts.FSLASH;
        MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(schemaPath + IdentityConsts.SCHEMA_USER))
                .responseBody(MASResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    JSONObject jsonObject = result.getBody().getContent();
                    UserAttributes userAttributes = getAttributes(jsonObject);
                    Callback.onSuccess(callback, userAttributes);
                } catch (JSONException e) {
                    Callback.onError(callback, e);
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    /*
    Helper method for processing users.
     */
    private MASUser processUserById(JSONObject jsonObject) throws JSONException {
        // should only be 1 user
        // get the array 'Resources'
        User user = new User();
        if (jsonObject.has(IdentityConsts.KEY_RESOURCES)) {
            JSONArray jsonArray = jsonObject.getJSONArray(IdentityConsts.KEY_RESOURCES);

            // <i>setTotalResults</i> can be called repeatedly. Calling it with
            // the same value that it is currently set does not alter the functionality.
            int totalResults = jsonObject.optInt(IdentityConsts.KEY_TOTAL_RESULTS);
            if (totalResults != 1) {
                throw new IllegalStateException("Should not return more than 1 user");
            }
            // extract the object from the JSONArray - verified that there is only 1 by this point
            JSONObject arrElem = jsonArray.getJSONObject(0);
            user.populate(arrElem);
        } else {
            user.populate(jsonObject);
        }
        return user;
    }

    /*
    Helper method for populating attributes.
     */
    private UserAttributes getAttributes(JSONObject jsonObject) throws JSONException {
        String id = jsonObject.optString(IdentityConsts.KEY_ID);
        if (TextUtils.isEmpty(id)) {
            throw new IllegalArgumentException("The ID cannot be null!");
        }

        if (id.equals(IdentityConsts.SCHEMA_USER)) {
            UserAttributes userAttributes = new UserAttributes();
            userAttributes.populate(jsonObject);
            return userAttributes;
        } else {
            return null;
        }
    }

    /*
    Helper method for processing users.
     */
    private List<MASUser> parse(JSONObject jsonObject) throws JSONException {

        List<MASUser> result = new ArrayList<>();
        // get the array 'Resources'
        if (jsonObject.has(IdentityConsts.KEY_RESOURCES)) {
            JSONArray jsonArray = jsonObject.getJSONArray(IdentityConsts.KEY_RESOURCES);
            if (jsonArray.length() > 0) {
                // iterate through the array, creating a user for each entry
                for (int i = 0; i < jsonArray.length(); i++) {
                    User ident = new User();
                    JSONObject arrElem = jsonArray.getJSONObject(i);
                    ident.populate(arrElem);
                    result.add(ident);
                }
            }
        }
        return result;
    }
}
