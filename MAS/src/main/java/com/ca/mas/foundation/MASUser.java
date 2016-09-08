/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.content.AsyncTaskLoader;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.util.Functions;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.foundation.util.FoundationUtil;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.user.MASAddress;
import com.ca.mas.identity.user.MASEmail;
import com.ca.mas.identity.user.MASIms;
import com.ca.mas.identity.user.MASMeta;
import com.ca.mas.identity.user.MASName;
import com.ca.mas.identity.user.MASPhone;
import com.ca.mas.identity.user.MASPhoto;
import com.ca.mas.identity.user.MASUserIdentity;
import com.ca.mas.identity.user.ScimUser;
import com.ca.mas.identity.user.User;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.user.UserIdentityManager;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.MASMessenger;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>The <b>MASUser</b> interface is a core interface used to represent a user in the system,
 * as well as an Identity Management definition used in the interaction with SCIM.</p>
 * <p/>
 * <p>The <b>MASUser</b> interface contains the common attribute <a href="https://tools.ietf.org/html/rfc7643#section-4.1">user</a>
 * which is the container for all of the other SCIM values, whether single or multi-valued.
 * SCIM provides a resource type for "User" resources.  The core schema for "User" is identified using the following schema URI:
 * "urn:ietf:params:scim:schemas:core:2.0:User". See the <a href="https://tools.ietf.org/html/rfc7643#section-8.2">Full User Representation</a> for the complete format.</p>
 */
public abstract class MASUser implements MASTransformable, MASMessenger, MASUserIdentity, ScimUser {
    protected static final String TAG = MASUser.class.getSimpleName();
    private static MASUser current;

    /**
     * Authenticate a user with username and password.
     */
    public static void login(@NonNull String userName, @NonNull String password, final MASCallback<MASUser> callback) {
        MobileSso mobileSso = FoundationUtil.getMobileSso();

        mobileSso.authenticate(userName, password.toCharArray(), new MASResultReceiver<JSONObject>() {
            @Override
            public void onSuccess(MAGResponse<JSONObject> response) {
                login(callback);
            }

            @Override
            public void onError(MAGError error) {
                current = null;
                Callback.onError(callback, error);
            }
        });
    }

    /**
     * Performs an implicit login by calling an endpoint that requires authentication. This results
     * in {@link MASUser#getCurrentUser()} being populated from the endpoint.
     */
    public static void login(final MASCallback<MASUser> callback) {
        final MobileSso mobileSso = FoundationUtil.getMobileSso();
        String s = FoundationUtil.getUserInfo().toString();

        MAGRequest magRequest = new MAGRequest.MAGRequestBuilder(Uri.parse(s))
                .responseBody(MAGResponseBody.jsonBody())
                .password()
                .build();

        mobileSso.processRequest(magRequest, new MASResultReceiver<JSONObject>(Callback.getHandler(callback)) {
            @Override
            public void onSuccess(final MAGResponse<JSONObject> response) {
                try {
                    current = createMASUser();
                    current.populate(response.getBody().getContent());
                    Callback.onSuccess(callback, MASUser.getCurrentUser());
                } catch (JSONException je) {
                    onError(new MAGError(je));
                }
            }

            @Override
            public void onError(MAGError error) {
                current = null;
                Callback.onError(callback, error);
            }
        });
    }

    /**
     * Retrieves the currently authenticated user.
     *
     * @return The currently authenticated user.
     */
    public static MASUser getCurrentUser() {
        if (current == null) {
            if (MobileSsoFactory.getInstance().isLogin()) {
                current = createMASUser();
                // Retrieve the user profile
                MASUser.login(null);
            }
        } else {
            if (!MobileSsoFactory.getInstance().isLogin()) {
                //The user's session has been removed,
                //may perform device de-registration or resetLocally
                current = null;
            }
        }
        return current;
    }

    private static MASUser createMASUser() {
        return new MASUser() {
            private String mSub;
            private String mPrefUserName;
            private String mPicture;
            private String mEmail;
            private String mPhoneNumber;
            private MASAddress address;
            private ScimUser scimUser = new User();

            @Override
            public boolean isAuthenticated() {
                return MobileSsoFactory.getInstance().isLogin();
            }

            @Override
            public boolean isCurrentUser() {
                return true;
            }

            @Override
            public String getUserName() {
                if (scimUser.getUserName() == null) {
                    return mPrefUserName;
                }
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
                if (scimUser == null) {
                    List<MASAddress> list = new ArrayList<>();
                    list.add(address);
                    return list;
                } else {
                    return scimUser.getAddressList();
                }
            }

            @Override
            public List<MASEmail> getEmailList() {
                if (scimUser == null) {
                    List<MASEmail> list = new ArrayList<>();
                    list.add(new MASEmail() {
                        @Override
                        public String getValue() {
                            return mEmail;
                        }
                    });
                    return list;
                } else {
                    return scimUser.getEmailList();
                }
            }

            @Override
            public List<MASPhone> getPhoneList() {
                if (scimUser.getPhoneList() == null) {
                    List<MASPhone> phoneList = new ArrayList<>();
                    phoneList.add(new MASPhone() {
                        @Override
                        public String getValue() {
                            return mPhoneNumber;
                        }
                    });
                    return phoneList;
                } else {
                    return scimUser.getPhoneList();
                }
            }

            @Override
            public List<MASIms> getImsList() {
                return scimUser.getImsList();
            }

            @Override
            public List<MASPhoto> getPhotoList() {
                if (scimUser.getPhotoList() == null) {
                    List<MASPhoto> phoneList = new ArrayList<>();
                    phoneList.add(new MASPhoto() {
                        @Override
                        public String getValue() {
                            return mPicture;
                        }
                    });
                    return phoneList;
                } else {
                    return scimUser.getPhotoList();
                }
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
            public String getId() {
                if (scimUser.getId() == null) {
                    return mPrefUserName;
                }
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
                if (jsonObject.has(IdentityConsts.KEY_MY_SUB)) {
                    mSub = jsonObject.optString(IdentityConsts.KEY_MY_SUB, IdentityConsts.EMPTY);
                    mPrefUserName = jsonObject.optString(IdentityConsts.KEY_MY_PREF_UNAME, IdentityConsts.EMPTY);
                    mPicture = jsonObject.optString(IdentityConsts.KEY_MY_PICTURE, IdentityConsts.EMPTY);
                    mEmail = jsonObject.optString(IdentityConsts.KEY_MY_EMAIL, IdentityConsts.EMPTY);
                    mPhoneNumber = jsonObject.optString(IdentityConsts.KEY_MY_PHONE, IdentityConsts.EMPTY);

                    JSONObject addrObj = jsonObject.optJSONObject(IdentityConsts.KEY_MY_ADDRESS);
                    if (addrObj != null) {
                        String streetAddr = addrObj.optString(IdentityConsts.KEY_MY_STREET_ADDR, IdentityConsts.EMPTY);
                        String locality = addrObj.optString(IdentityConsts.KEY_MY_LOCALITY, IdentityConsts.EMPTY);
                        String region = addrObj.optString(IdentityConsts.KEY_MY_REGION, IdentityConsts.EMPTY);
                        String postalCode = addrObj.optString(IdentityConsts.KEY_MY_POSTAL_CODE, IdentityConsts.EMPTY);
                        String country = addrObj.optString(IdentityConsts.KEY_MY_COUNTRY, IdentityConsts.EMPTY);
                        address = new MASAddress(streetAddr, locality, region, country, postalCode);
                    }

                    // Optional when populated
                    UserIdentityManager.getInstance().getUserById(mPrefUserName, new MASCallback<MASUser>() {
                        @Override
                        public void onSuccess(MASUser object) {
                            scimUser = object;
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.w(TAG, "SCIM Module is not installed on Server, please check your SCIM setting.");
                        }
                    });
                } else {
                    if (jsonObject.has(IdentityConsts.KEY_RESOURCES)) {
                        JSONArray jsonArray = jsonObject.getJSONArray(IdentityConsts.KEY_RESOURCES);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            // Extract the object from the JSONArray
                            JSONObject arrElem = jsonArray.getJSONObject(i);
                            scimUser.populate(arrElem);
                        }
                    }
                }
            }

            @Override
            public JSONObject getAsJSONObject() throws JSONException {
                return scimUser.getAsJSONObject();
            }

            @Override
            public void startListeningToTopic(final MASTopic topic, final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        MASConnectaManager.getInstance().subscribe(topic, callback);
                    }
                }, callback);
            }

            @Override
            public void stopListeningToTopic(final MASTopic topic, final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        MASConnectaManager.getInstance().unsubscribe(topic, callback);
                    }
                }, callback);
            }

            @Override
            public void sendMessage(final MASTopic topic, final MASMessage message, final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        MASConnectaManager.getInstance().publish(topic, message, callback);
                    }
                }, callback);
            }

            @Override
            public void sendMessage(@NonNull final MASMessage message, final MASUser user, final MASCallback<Void> callback) {
                String userId = user.getId();
                sendMessage(message, user, userId, callback);
            }

            @Override
            public void sendMessage(@NonNull final MASMessage message, final MASUser user, final String topic, final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        try {
                            String userId = user.getId();
                            MASTopic masTopic = new MASTopicBuilder()
                                    .setUserId(userId)
                                    .setCustomTopic(topic)
                                    .build();

                            MASConnectaManager.getInstance().publish(masTopic, message, callback);
                        } catch (MASException me) {
                            callback.onError(me);
                        }
                    }
                }, callback);
            }

            @Override
            public void startListeningToMyMessages(final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        try {
                            String userId = current.getId();
                            MASTopic masTopic = new MASTopicBuilder()
                                    .setUserId(userId)
                                    .setCustomTopic(userId)
                                    .build();

                            startListeningToTopic(masTopic, callback);
                        } catch (MASException me) {
                            callback.onError(me);
                        }
                    }
                }, callback);
            }

            @Override
            public void stopListeningToMyMessages(final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        try {
                            String userId = current.getId();
                            MASTopic masTopic = new MASTopicBuilder()
                                    .setUserId(userId)
                                    .setCustomTopic(userId)
                                    .build();

                            stopListeningToTopic(masTopic, callback);
                        } catch (MASException me) {
                            callback.onError(me);
                        }
                    }
                }, callback);
            }

            private void execute(final Functions.NullaryVoid function, final MASCallback<Void> callback) {
                if (getUserName() == null) {
                    //Trigger login to retrieve the username
                    login(new MASCallback<MASUser>() {
                        @Override
                        public Handler getHandler() {
                            return Callback.getHandler(callback);
                        }

                        @Override
                        public void onSuccess(MASUser object) {
                            function.call();
                        }

                        @Override
                        public void onError(Throwable e) {
                            Callback.onError(callback, e);
                        }
                    });
                } else {
                    function.call();
                }
            }

            /**
             * <b>Description:</b> Logout from the server.
             *
             * @param callback
             */
            @Override
            public void logout(final MASCallback<Void> callback) {
                current = null;

                new AsyncTaskLoader<Object>(MAS.getContext()) {
                    @Override
                    protected void onStartLoading() {
                        super.onStartLoading();
                        forceLoad();
                    }

                    @Override
                    public Object loadInBackground() {
                        try {
                            MobileSsoFactory.getInstance().logout(true);
                            Callback.onSuccess(callback, null);
                        } catch (Exception e) {
                            Callback.onError(callback, e);
                        }
                        return null;
                    }
                }.startLoading();
            }

            @Override
            public void getUserById(String id, MASCallback<MASUser> callback) {
                UserIdentityManager.getInstance().getUserById(id, callback);
            }

            @Override
            public void getUsersByFilter(MASFilteredRequest filteredRequest, MASCallback<List<MASUser>> callback) {
                UserIdentityManager.getInstance().getUsersByFilter(filteredRequest, callback);
            }

            @Override
            public void getUserMetaData(MASCallback<UserAttributes> callback) {
                UserIdentityManager.getInstance().getUserMetaData(callback);
            }

            @Override
            public Bitmap getThumbnailImage() {
                return UserIdentityManager.getInstance().getUserThumbnailImage(this);
            }
        };
    }

    /**
     * <p>Logs off an already authenticated user via an asynchronous request.</p>
     * <p/>
     * This will invoke {@link Callback#onSuccess(MASCallback, Object)} upon a successful result.
     *
     * @param callback The Callback that receives the results. On a successful completion, the user
     *                 available via {@link MASUser#getCurrentUser()} will be updated with the new information.
     */
    public abstract void logout(final MASCallback<Void> callback);

    /**
     * Determines if the user is currently authenticated with the MAG server.
     */
    public abstract boolean isAuthenticated();

    /**
     * Determines if this user instance is the current authenticated user.
     */
    public abstract boolean isCurrentUser();
}
