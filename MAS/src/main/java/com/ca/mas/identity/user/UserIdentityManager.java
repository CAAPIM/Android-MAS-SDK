/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.identity.user;

import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASSessionUnlockCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.foundation.util.FoundationConsts;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.identity.util.IdentityUtil;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
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
public class UserIdentityManager {

    private static String TAG = UserIdentityManager.class.getSimpleName();

    private static UserIdentityManager instance = new UserIdentityManager();

    public static UserIdentityManager getInstance() {
        return instance;
    }

    private UserIdentityManager() {
    }

    // -------------------- MASUserIdentity interface -----------------------------

    // -------------------- USERS ---------------------------------------------
    public void getUsersByFilter(final MASFilteredRequest filteredRequest, final MASCallback<List<MASUser>> callback) {
        Uri uri = filteredRequest.createUri(MAS.getContext());
        MASRequest masRequest = new MASRequest.MASRequestBuilder(uri)
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MAGResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    List<MASUser> container = new ArrayList<>();
                    processUsersByFilter(filteredRequest, result.getBody().getContent(), container, callback);
                } catch (Exception je) {
                    onError(new MAGError(je));
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public void getUserById(String id, final MASCallback<MASUser> callback) {
        try {
            MASRequest masRequest = new MASRequest.MASRequestBuilder(new URI(IdentityUtil.getUserPath(MAS.getContext())
                    + FoundationConsts.FSLASH + id))
                    .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                    .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                    .responseBody(MAGResponseBody.jsonBody())
                    .get()
                    .build();

            MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
                @Override
                public void onSuccess(MASResponse<JSONObject> result) {
                    try {
                        Callback.onSuccess(callback, processUserById(result.getBody().getContent()));
                    } catch (Exception je) {
                        onError(new MAGError(je));
                    }
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    // --------- META-DATA ----------------------------------------------------
    public void getUserMetaData(final MASCallback<UserAttributes> callback) {
        String schemaPath = IdentityUtil.getSchemasPath(MAS.getContext()) + FoundationConsts.FSLASH;
        MASRequest masRequest = new MASRequest.MASRequestBuilder(Uri.parse(schemaPath + IdentityConsts.SCHEMA_USER))
                .responseBody(MAGResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    JSONObject jsonObject = result.getBody().getContent();
                    UserAttributes userAttributes = getAttributes(jsonObject);
                    Callback.onSuccess(callback, userAttributes);
                } catch (MASException e) {
                    onError(new MAGError(e));
                }
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    public Bitmap getUserThumbnailImage(MASUser user) {
        return IdentityUtil.getThumbnail(user.getPhotoList());
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
        return createMASUser(user);
    }

    /*
    Helper method for retrieving users when paging is involved.
     */
    private void getUsers(final MASFilteredRequest filteredRequest, final List<MASUser> masUsers, final MASCallback<List<MASUser>> callback) throws MASException {
        Uri uri = filteredRequest.createUri(MAS.getContext());
        MASRequest masRequest = new MASRequest.MASRequestBuilder(uri)
                .header(IdentityConsts.HEADER_KEY_ACCEPT, IdentityConsts.HEADER_VALUE_ACCEPT)
                .header(IdentityConsts.HEADER_KEY_CONTENT_TYPE, IdentityConsts.HEADER_VALUE_CONTENT_TYPE)
                .responseBody(MAGResponseBody.jsonBody())
                .get()
                .build();

        MAS.invoke(masRequest, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    processUsersByFilter(filteredRequest, result.getBody().getContent(), masUsers, callback);
                } catch (Exception e) {
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
    Helper method for populating attributes.
     */
    private UserAttributes getAttributes(JSONObject jsonObject) throws MASException {
        String id = jsonObject.optString(IdentityConsts.KEY_ID);
        if (TextUtils.isEmpty(id)) {
            throw new MASException("The ID cannot be null!");
        }

        try {
            if (id.equals(IdentityConsts.SCHEMA_USER)) {
                UserAttributes userAttributes = new UserAttributes();
                userAttributes.populate(jsonObject);
                return userAttributes;
            } else {
                return null;
            }
        } catch (JSONException je) {
            throw new MASException(je);
        }
    }

    /*
    Helper method for processing users.
     */
    private void processUsersByFilter(MASFilteredRequest filteredRequest, JSONObject jsonObject, List<MASUser> container, MASCallback<List<MASUser>> callback) throws JSONException, MASException {

        // get the array 'Resources'
        if (jsonObject.has(IdentityConsts.KEY_RESOURCES)) {
            JSONArray jsonArray = jsonObject.getJSONArray(IdentityConsts.KEY_RESOURCES);
            if (jsonArray.length() > 0) {
                // <i>setTotalResults</i> can be called repeatedly. Calling it with
                // the same value that it is currently set does not alter the functionality.
                int totalResults = jsonObject.optInt(IdentityConsts.KEY_TOTAL_RESULTS);
                filteredRequest.setTotalResults(totalResults);

                // iterate through the array, creating a user for each entry
                for (int i = 0; i < jsonArray.length(); i++) {
                    User ident = new User();
                    JSONObject arrElem = jsonArray.getJSONObject(i);
                    ident.populate(arrElem);
                    container.add(createMASUser(ident));
                }

                if (filteredRequest.hasNext()) {
                    getUsers(filteredRequest, container, callback);
                } else {
                    Callback.onSuccess(callback, container);
                }
            } else {
                Callback.onSuccess(callback, container);
            }
        } else {
            int totalResults = jsonObject.optInt(IdentityConsts.KEY_TOTAL_RESULTS);
            if (totalResults == 0) {
                Callback.onSuccess(callback, container);
            }
        }
    }

    private static MASUser createMASUser(final ScimUser scimUser) {

        return new MASUser() {

            @Override
            public boolean isAuthenticated() {
                return false;
            }

            @Override
            public boolean isCurrentUser() {
                return true;
            }

            @Override
            public void requestUserInfo(MASCallback<Void> callback) {
                new UserNotAuthenticatedException();
            }

            @Override
            public String getUserName() {
                return scimUser.getUserName();
            }

            @Override
            public String getNickName() {
                return scimUser.getNickName();
            }

            @Override
            public String getProfileUrl() {
                return scimUser.getProfileUrl();
            }

            @Override
            public String getUserType() {
                return scimUser.getUserType();
            }

            @Override
            public String getTitle() {
                return scimUser.getTitle();
            }

            @Override
            public String getPreferredLanguage() {
                return scimUser.getPreferredLanguage();
            }

            @Override
            public String getLocale() {
                return scimUser.getLocale();
            }

            @Override
            public String getTimeZone() {
                return scimUser.getTimeZone();
            }

            @Override
            public boolean isActive() {
                return scimUser.isActive();
            }

            @Override
            public String getPassword() {
                return scimUser.getPassword();
            }

            @Override
            public List<MASAddress> getAddressList() {
                return scimUser.getAddressList();
            }

            @Override
            public List<MASEmail> getEmailList() {
                return null;
            }

            @Override
            public List<MASPhone> getPhoneList() {
                return scimUser.getPhoneList();
            }

            @Override
            public List<MASIms> getImsList() {
                return scimUser.getImsList();
            }

            @Override
            public List<MASPhoto> getPhotoList() {
                return scimUser.getPhotoList();
            }

            @Override
            public MASMeta getMeta() {
                return scimUser.getMeta();
            }

            @Override
            public List<MASGroup> getGroupList() {
                return scimUser.getGroupList();
            }

            @Override
            public MASName getName() {
                return scimUser.getName();
            }

            @Override
            public JSONObject getSource() {
                return scimUser.getSource();
            }

            @Override
            public String getId() {
                return scimUser.getId();
            }

            @Override
            public String getExternalId() {
                return scimUser.getExternalId();
            }

            @Override
            public String getDisplayName() {
                return scimUser.getDisplayName();
            }

            @Override
            public long getCardinality() {
                return scimUser.getCardinality();
            }

            @Override
            public void populate(@NonNull JSONObject jsonObject) throws JSONException {
                scimUser.populate(jsonObject);
            }

            @Override
            public JSONObject getAsJSONObject() throws JSONException {
                return scimUser.getAsJSONObject();
            }

            @Override
            public void startListeningToTopic(final MASTopic topic, final MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void stopListeningToTopic(final MASTopic topic, final MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void sendMessage(final MASTopic topic, final MASMessage message, final MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void sendMessage(MASMessage message, MASUser user, MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void sendMessage(MASMessage message, MASUser user, String topic, MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void startListeningToMyMessages(MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void stopListeningToMyMessages(MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void logout(final MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void getUserById(String id, MASCallback<MASUser> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void getUsersByFilter(MASFilteredRequest filteredRequest, MASCallback<List<MASUser>> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void getUserMetaData(MASCallback<UserAttributes> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public Bitmap getThumbnailImage() {
                return UserIdentityManager.getInstance().getUserThumbnailImage(this);
            }

            @Override
            public void lockSession(MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public void unlockSession(MASSessionUnlockCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }

            @Override
            public boolean isSessionLocked() {
                return false;
            }

            @Override
            public void removeSessionLock(MASCallback<Void> callback) {
                throw new UserNotAuthenticatedException();
            }
        };
    }

}
