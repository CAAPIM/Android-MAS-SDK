/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.annotation.TargetApi;
import android.content.AsyncTaskLoader;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Handler;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.security.EncryptionProviderLockable;
import com.ca.mas.core.security.SecureLockException;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.core.store.TokenStoreException;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.JWTValidation;
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
import com.ca.mas.identity.user.ScimUserRepository;
import com.ca.mas.identity.user.User;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.user.UserIdentityManager;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.MASMessenger;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.foundation.MASFoundationStrings.SECURE_LOCK_FAILED_TO_DELETE_SECURE_ID_TOKEN;

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
    private static final String SESSION_LOCK_ALIAS = "com.ca.mas.SESSION_LOCK";
    private static List<UserRepository> userRepositories = new ArrayList<>();

    static {
        // For Social Login, the SCIM endpoint should failed and fallback to /userinfo.
        // Try to get the SCIM profile first followed by the userinfo, this order is important.
        userRepositories = new ArrayList<>();
        userRepositories.add(new ScimUserRepository());
        userRepositories.add(new UserInfoRepository());
    }

    protected static final String TAG = MASUser.class.getSimpleName();
    private static MASUser current;

    /**
     * Authenticate a user with username and password.
     *
     * @deprecated Please use {@link #login(String, char[], MASCallback)}
     */
    @Deprecated
    public static void login(@NonNull String userName, @NonNull String password, final MASCallback<MASUser> callback) {
        login(userName, password.toCharArray(), callback);
    }

    /**
     * Authenticate a user with username and password.
     */
    public static void login(@NonNull String userName, @NonNull char[] cPassword, final MASCallback<MASUser> callback) {
        MobileSso mobileSso = FoundationUtil.getMobileSso();

        mobileSso.authenticate(userName, cPassword, new MASResultReceiver<JSONObject>() {
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
     * Authenticate a user with ID Token
     */
    public static void login(MASIdToken idToken, final MASCallback<MASUser> callback) {
        MobileSso mobileSso = FoundationUtil.getMobileSso();

        mobileSso.authenticate(idToken, new MASResultReceiver<JSONObject>() {
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

        final MASUser user = createMASUser();
        user.requestUserInfo(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                current = user;
                Callback.onSuccess(callback, current);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        });
    }

    /**
     * Authenticates a user with authorization code,
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3.1">
     */
    public static void login(@NonNull MASAuthorizationResponse authorizationResponse, final MASCallback<MASUser> callback) {

        MobileSso mobileSso = FoundationUtil.getMobileSso();
        mobileSso.authenticate(authorizationResponse.getAuthorizationCode(),
                authorizationResponse.getState(), new MASResultReceiver<JSONObject>() {
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
     * Retrieves the currently authenticated user.
     *
     * @return The currently authenticated user.
     */
    public static MASUser getCurrentUser() {
        if (current == null) {
            TokenManager tokenManager = StorageProvider.getInstance().getTokenManager();
            if (tokenManager.getUserProfile() != null) {
                current = createMASUser();
            }
        }
        if (current != null &&
                !current.isAuthenticated() &&
                !current.isSessionLocked()) {
            //The user's session has been removed,
            //The Grant flow has been switch from user to client credential
            //Device has been de-registered or resetLocally
            current = null;
        }
        return current;
    }

    private static MASUser createMASUser() {

        return new MASUser() {
            private ScimUser scimUser = getLocalUserProfile();

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
                return scimUser.getEmailList();
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
                throw new UnsupportedOperationException();
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
                        String userId = user.getId();
                        MASTopic masTopic = new MASTopicBuilder()
                                .setUserId(userId)
                                .setCustomTopic(topic)
                                .build();

                        MASConnectaManager.getInstance().publish(masTopic, message, callback);
                    }
                }, callback);
            }

            @Override
            public void startListeningToMyMessages(final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        String userId = current.getId();
                        MASTopic masTopic = new MASTopicBuilder()
                                .setUserId(userId)
                                .setCustomTopic("#")
                                .build();

                        startListeningToTopic(masTopic, callback);
                    }
                }, callback);
            }

            @Override
            public void stopListeningToMyMessages(final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        String userId = current.getId();
                        MASTopic masTopic = new MASTopicBuilder()
                                .setUserId(userId)
                                .setCustomTopic("#")
                                .build();

                        stopListeningToTopic(masTopic, callback);
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
                            if (!isSessionLocked()) {
                                MobileSsoFactory.getInstance().logout(true);
                                Callback.onSuccess(callback, null);
                            } else {
                                Callback.onError(callback, new SecureLockException(MASFoundationStrings.SECURE_LOCK_SESSION_CURRENTLY_LOCKED));
                            }
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

            public void requestUserInfo(MASCallback<Void> callback) {
                LinkedList<UserRepository> repositories = new LinkedList<>(userRepositories);
                fetch(repositories, callback, null);
            }

            private void fetch(final LinkedList<UserRepository> repositories, final MASCallback<Void> callback, Throwable e) {
                UserRepository f;
                try {
                    f = repositories.pop();
                } catch (NoSuchElementException nse) {
                    Callback.onError(callback, e);
                    return;
                }
                f.findByUsername(getUserName(), new MASCallback<ScimUser>() {
                    @Override
                    public void onSuccess(ScimUser result) {
                        scimUser = result;

                        try {
                            JSONObject source = scimUser.getSource();
                            //make sure not to store the password
                            source.remove(IdentityConsts.KEY_PASSWORD);
                            StorageProvider.getInstance()
                                    .getTokenManager().
                                    saveUserProfile(source.toString());
                        } catch (Exception e) {
                            if (DEBUG)
                                Log.w(TAG, "Unable to persist user profile to local storage.", e);
                        }
                        Callback.onSuccess(callback, null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        fetch(repositories, callback, e);
                    }
                });
            }

            private ScimUser getLocalUserProfile() {
                User user = new User();
                try {
                    String userProfile = StorageProvider.getInstance()
                            .getTokenManager()
                            .getUserProfile();
                    if (userProfile != null) {
                        user.populate(new JSONObject(userProfile));
                    }
                } catch (Exception e) {
                    if (DEBUG) Log.w(TAG, "Failed to populate MASUser from local storage.", e);
                }
                return user;
            }

            @Override
            @TargetApi(23)
            public void lockSession(MASCallback<Void> callback) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    MASUser currentUser = MASUser.getCurrentUser();
                    if (currentUser == null) {
                        Callback.onError(callback, new SecureLockException(MASFoundationStrings.USER_NOT_CURRENTLY_AUTHENTICATED));
                    } else if (isSessionLocked()) {
                        Callback.onSuccess(callback, null);
                    } else {

                        // Retrieve the ID token
                        IdToken idToken = StorageProvider.getInstance()
                                .getTokenManager()
                                .getIdToken();

                        if (idToken == null) {
                            Callback.onError(callback, new SecureLockException(MASFoundationStrings.SECURE_LOCK_FAILED_TO_RETRIEVE_ID_TOKEN));
                            return;
                        }

                        // Remove access and refresh tokens
                        StorageProvider.getInstance().getOAuthTokenContainer().clear();

                        // Move the ID token from the Keychain to the fingerprint protected shared Keystore
                        Parcel idTokenParcel = Parcel.obtain();
                        idToken.writeToParcel(idTokenParcel, 0);
                        byte[] idTokenBytes = idTokenParcel.marshall();

                        // Save the encrypted token
                        EncryptionProviderLockable encryptionProviderLockable
                                      = new EncryptionProviderLockable(MAS.getContext(), SESSION_LOCK_ALIAS);
                        // Delete any previously generated key due to improper closure
                        encryptionProviderLockable.clear();
                        // now encrypt the data
                        byte[] encryptedData = encryptionProviderLockable.encrypt(idTokenBytes);
                        try {
                            StorageProvider.getInstance()
                                    .getTokenManager()
                                    .saveSecureIdToken(encryptedData);
                        } catch (TokenStoreException e) {
                            Callback.onError(callback, new SecureLockException(MASFoundationStrings.SECURE_LOCK_FAILED_TO_SAVE_SECURE_ID_TOKEN, e));
                            return;
                        }

                        // Remove the unencrypted token
                        try {
                            StorageProvider.getInstance()
                                    .getTokenManager()
                                    .deleteIdToken();
                        } catch (TokenStoreException e) {
                            Callback.onError(callback, new SecureLockException(MASFoundationStrings.SECURE_LOCK_FAILED_TO_DELETE_ID_TOKEN, e));
                            return;
                        }

                        encryptionProviderLockable.lock();
                        idTokenParcel.recycle();

                        Callback.onSuccess(callback, null);
                    }
                } else {
                    Callback.onError(callback, new IllegalAccessException(MASFoundationStrings.API_TARGET_EXCEPTION));
                }
            }

            @Override
            public void unlockSession(MASSessionUnlockCallback<Void> callback) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (isSessionLocked()) {

                        // Unlock the ID token from the Keystore and places the decrypted ID token back to the Keychain
                        byte[] secureIdToken = StorageProvider.getInstance()
                                .getTokenManager()
                                .getSecureIdToken();

                        EncryptionProviderLockable encryptionProviderLockable 
                                      = new EncryptionProviderLockable(MAS.getContext(), SESSION_LOCK_ALIAS);
                        // Read the decrypted data, reconstruct it as a Parcel, then as an IdToken
                        Parcel parcel = Parcel.obtain();
                        try {
                            // Decrypt the encrypted ID token
                            byte[] decryptedData = encryptionProviderLockable.decrypt(secureIdToken);
                            parcel.unmarshall(decryptedData, 0, decryptedData.length);
                            parcel.setDataPosition(0);

                            IdToken idToken = IdToken.CREATOR.createFromParcel(parcel);
                            try {
                                // Save the unlocked ID token
                                StorageProvider.getInstance()
                                        .getTokenManager()
                                        .saveIdToken(idToken);
                            } catch (TokenStoreException e) {
                                Callback.onError(callback, new SecureLockException(MASFoundationStrings.SECURE_LOCK_FAILED_TO_SAVE_ID_TOKEN, e));
                                return;
                            }

                            try {
                                // Remove the locked ID token
                                StorageProvider.getInstance()
                                        .getTokenManager()
                                        .deleteSecureIdToken();
                            } catch (TokenStoreException e) {
                                Callback.onError(callback, new SecureLockException(SECURE_LOCK_FAILED_TO_DELETE_SECURE_ID_TOKEN, e));
                                return;
                            }

                            // Delete the previously generated key after successfully decrypting
                            encryptionProviderLockable.clear();

                            boolean isTokenExpired = JWTValidation.isIdTokenExpired(idToken);
                            if (!isTokenExpired) {
                                // Indicate the device is unlocked
                                Callback.onSuccess(callback, null);
                            } else {
                                // The ID token must be placed back before calling logout()
                                logout(null);
                                Callback.onError(callback, new SecureLockException(MASFoundationStrings.TOKEN_ID_EXPIRED));
                            }
                        } catch (Exception e) {
                            if (e.getCause() != null && e.getCause() instanceof android.security.keystore.UserNotAuthenticatedException) {
                                // Listener activity to trigger fingerprint
                                if (callback != null) {
                                    callback.onUserAuthenticationRequired();
                                }
                            } else {
                                Callback.onError(callback, e);
                            }
                        }
                    } else {
                        Callback.onSuccess(callback, null);
                    }
                } else {
                    Callback.onError(callback, new IllegalAccessException(MASFoundationStrings.API_TARGET_EXCEPTION));
                }
            }

            @Override
            public boolean isSessionLocked() {
                return StorageProvider.getInstance()
                        .getTokenManager()
                        .getSecureIdToken() != null;
            }

            @Override
            public void removeSessionLock(MASCallback<Void> callback) {
                // Session already unlocked
                if (!isSessionLocked()) {
                    Callback.onSuccess(callback, null);
                } else {
                    try {
                        StorageProvider.getInstance()
                                .getTokenManager()
                                .deleteSecureIdToken();
                        Callback.onSuccess(callback, null);
                    } catch (TokenStoreException e) {
                        Callback.onError(callback, new SecureLockException(SECURE_LOCK_FAILED_TO_DELETE_SECURE_ID_TOKEN, e));
                    }
                }
            }
        };
    }

    /**
     * <p>Logs off an already authenticated user via an asynchronous request.</p>
     * <p/>
     * This will invoke {@link Callback#onSuccess(MASCallback, Object)} upon a successful result.
     *
     * @param callback The Callback that receives the results. On a successful completion, the user
     *                 will be logout from the Application.
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

    /**
     * Requesting userInfo for the MASUser object
     *
     * @param callback The Callback that receives the results. On a successful completion, the user
     *                 available via {@link MASUser#getCurrentUser()} will be updated with the new information.
     */
    public abstract void requestUserInfo(MASCallback<Void> callback);

    /**
     * Locks the current device.
     * This will remove the access and refresh tokens, as well as the user profile.
     * The ID token will then be moved to the fingerprint protected shared KeyStore.
     */
    @TargetApi(23)
    public abstract void lockSession(MASCallback<Void> callback);

    /**
     * Triggers the Android unlock and captures the unlock result.
     * Unlocks the ID_TOKEN from the KeyStore and places it back into the keychain,
     * removes the locked ID_TOKEN, and then indicates if the device is unlocked.
     */
    @TargetApi(23)
    public abstract void unlockSession(MASSessionUnlockCallback<Void> callback);

    /**
     * Checks to see if the device has a locked ID token.
     *
     * @return true if there's a secured ID token
     */
    @TargetApi(23)
    public abstract boolean isSessionLocked();

    /**
     * Removes the secured ID token.
     */
    @TargetApi(23)
    public abstract void removeSessionLock(MASCallback<Void> callback);
}
