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
import android.security.keystore.UserNotAuthenticatedException;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.core.MAGResultReceiver;
import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.security.LockableEncryptionProvider;
import com.ca.mas.core.security.SecureLockException;
import com.ca.mas.core.storage.Storage;
import com.ca.mas.core.storage.StorageException;
import com.ca.mas.core.storage.StorageResult;
import com.ca.mas.core.storage.implementation.MASStorageManager;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.core.store.TokenStoreException;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.JWTValidation;
import com.ca.mas.core.util.Functions;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.user.MASUserIdentity;
import com.ca.mas.identity.user.MASUserRepository;
import com.ca.mas.identity.user.ScimUser;
import com.ca.mas.identity.user.User;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.identity.util.IdentityUtil;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.MASMessenger;
import com.ca.mas.messaging.topic.MASTopic;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Observable;
import java.util.Observer;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;
import static com.ca.mas.foundation.MAS.getContext;
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
public abstract class MASUser implements MASMessenger, MASUserIdentity, ScimUser {
    private static final String SESSION_LOCK_ALIAS = "com.ca.mas.SESSION_LOCK";
    private static MASUser current;

    static {
        EventDispatcher.STOP.addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                current = null;
            }
        });
    }

    /**
     * Login with OAuth2 Authorization request
     */
    public static void login(MASAuthorizationRequest request, MASAuthorizationRequestHandler handler) {
        handler.authorize(request);
    }

    /**
     * Authenticates a user with a username and password.
     *
     * @deprecated Please use {@link #login(String, char[], MASCallback)}
     */
    @Deprecated
    public static void login(@NonNull String userName, @NonNull String password, final MASCallback<MASUser> callback) {
        login(userName, password.toCharArray(), callback);
    }

    /**
     * Authenticates a user with a username and password.
     */
    public static void login(@NonNull String userName, @NonNull char[] cPassword, final MASCallback<MASUser> callback) {
        login(new MASAuthCredentialsPassword(userName, cPassword), callback);
    }

    /**
     * Authenticates a user with an ID token.
     */
    public static void login(MASIdToken idToken, final MASCallback<MASUser> callback) {
        login(new MASAuthCredentialsJWT(idToken), callback);
    }

    /**
     * Authenticates a user with a MASAuthCredentials object.
     */
    public static void login(MASAuthCredentials credentials, final MASCallback<MASUser> callback) {
        MobileSso mobileSso = MobileSsoFactory.getInstance();
        mobileSso.authenticate(credentials, new MAGResultReceiver<JSONObject>() {
            @Override
            public void onSuccess(MASResponse<JSONObject> response) {
                login(callback);
            }

            @Override
            public void onError(MAGError error) {
                current = null;
                Callback.onError(callback, error);
                MAS.processPendingRequests();
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
                MAS.processPendingRequests();
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
                MAS.processPendingRequests();
            }
        });
    }

    /**
     * Authenticates a user with an authorization code.
     *
     * @see <a href="https://tools.ietf.org/html/rfc6749#section-1.3.1">
     */
    public static void login(@NonNull MASAuthorizationResponse authorizationResponse, final MASCallback<MASUser> callback) {
        login(new MASAuthCredentialsAuthorizationCode(authorizationResponse.getAuthorizationCode(),
                authorizationResponse.getState()), callback);
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

    /**
     * @return OAuth Access Token
     */
    public abstract String getAccessToken();

    private static MASUser createMASUser() {

        MASUser user = new User() {
            @MASExtension
            private MASMessenger masMessenger;
            @MASExtension
            private MASUserRepository userRepository;

            @Override
            public boolean isAuthenticated() {
                return MobileSsoFactory.getInstance().isLogin();
            }

            @Override
            public boolean isCurrentUser() {
                return true;
            }

            @Override
            public void sendMessage(final MASTopic topic, final MASMessage message, final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        masMessenger.sendMessage(topic, message, callback);
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
                        masMessenger.sendMessage(message, user, topic, callback);
                    }
                }, callback);
            }

            @Override
            public void sendMessage(@NonNull final MASMessage message, final MASGroup group, final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        masMessenger.sendMessage(message, group, callback);
                    }
                }, callback);
            }

            @Override
            public void sendMessage(@NonNull final MASMessage message, final MASGroup group, final String topic, final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        masMessenger.sendMessage(message, group, topic, callback);
                    }
                }, callback);
            }

            @Override
            public void startListeningToMyMessages(final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        masMessenger.startListeningToMyMessages(callback);
                    }
                }, callback);
            }

            @Override
            public void stopListeningToMyMessages(final MASCallback<Void> callback) {
                execute(new Functions.NullaryVoid() {
                    @Override
                    public void call() {
                        masMessenger.stopListeningToMyMessages(callback);
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
                userRepository.getUserById(id, callback);
            }

            @Override
            public void getUsersByFilter(MASFilteredRequest filteredRequest, MASCallback<List<MASUser>> callback) {
                userRepository.getUsersByFilter(filteredRequest, callback);
            }

            @Override
            public void getUserMetaData(MASCallback<UserAttributes> callback) {
                userRepository.getUserMetaData(callback);
            }

            @Override
            public String getAccessToken() {
                long expiry = StorageProvider.getInstance().getOAuthTokenContainer().getExpiry();
                if (expiry <= 0 || System.currentTimeMillis() <= expiry) {
                    return StorageProvider.getInstance().getOAuthTokenContainer().getAccessToken();
                }
                return null;
            }

            @Override
            public Bitmap getThumbnailImage() {
                return IdentityUtil.getThumbnail(getPhotoList());
            }

            @Override
            public void requestUserInfo(MASCallback<Void> callback) {
                // For Social Login, the SCIM endpoint should failed and fallback to /userinfo.
                // Try to get the SCIM profile first followed by the userinfo, this order is important.
                LinkedList<UserRepository> repositories = new LinkedList<>();
                if (userRepository != null) {
                    repositories.add(new UserRepository() {
                        @Override
                        public void getCurrentUser(MASCallback<MASUser> result) {
                            if (getUserName() == null) {
                                userRepository.me(result);
                            } else {
                                userRepository.getUserById(getUserName(), result);
                            }
                        }
                    });
                }
                repositories.add(new UserInfoRepository());
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
                try {
                    f.getCurrentUser(new MASCallback<MASUser>() {
                        @Override
                        public void onSuccess(MASUser result) {

                            try {
                                JSONObject source = result.getSource();
                                source.remove(IdentityConsts.KEY_PASSWORD);
                                populate(source);
                                //make sure not to store the password
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
                } catch (Exception e1) {
                    fetch(repositories, callback, e1);
                }
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

                        try {
                            // Remove access and refresh tokens
                            StorageProvider.getInstance().getOAuthTokenContainer().clear();

                            // Move the ID token from the Keychain to the fingerprint protected shared Keystore
                            Parcel idTokenParcel = Parcel.obtain();
                            idToken.writeToParcel(idTokenParcel, 0);
                            byte[] idTokenBytes = idTokenParcel.marshall();

                            // Save the encrypted token
                            LockableEncryptionProvider lockableEncryptionProvider
                                    = new LockableEncryptionProvider(MAS.getContext(), SESSION_LOCK_ALIAS);
                            // Delete any previously generated key due to improper closure
                            lockableEncryptionProvider.clear();
                            // now encrypt the data
                            byte[] encryptedData = lockableEncryptionProvider.encrypt(idTokenBytes);
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

                            idTokenParcel.recycle();

                            Callback.onSuccess(callback, null);
                        } catch (Exception e) {
                            Callback.onError(callback, new SecureLockException(MASFoundationStrings.SECURE_LOCK_FAILED_TO_SAVE_SECURE_ID_TOKEN, e));
                        }
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

                        LockableEncryptionProvider lockableEncryptionProvider
                                = new LockableEncryptionProvider(MAS.getContext(), SESSION_LOCK_ALIAS);
                        // Read the decrypted data, reconstruct it as a Parcel, then as an IdToken
                        Parcel parcel = Parcel.obtain();
                        try {
                            // Decrypt the encrypted ID token
                            byte[] decryptedData = lockableEncryptionProvider.decrypt(secureIdToken);
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
                            lockableEncryptionProvider.clear();

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
                            Throwable t = e.getCause();
                            if (t != null && (t instanceof android.security.keystore.UserNotAuthenticatedException
                                    || (t.getCause() != null && t.getCause() instanceof UserNotAuthenticatedException))) {
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

        try {
            JSONObject localUserProfile = getLocalUserProfile();
            if (localUserProfile != null) {
                user.populate(localUserProfile);
            }
        } catch (JSONException e) {
            if (DEBUG) Log.w(TAG, "Failed to populate MASUser from local storage.", e);
        }
        Extension.inject(user);
        return user;
    }

    private static JSONObject getLocalUserProfile() throws JSONException {
        JSONObject user = null;
        String userProfile = StorageProvider.getInstance()
                .getTokenManager()
                .getUserProfile();
        if (userProfile != null) {
            user = new JSONObject(userProfile);
        }
        return user;
    }

    /**
     * Returns the last authenticated session's type of auth credentials used.
     */
    public static String getAuthCredentialsType() {
        try {
            Storage accountManager = new MASStorageManager().getStorage(
                    MASStorageManager.MASStorageType.TYPE_AMS,
                    new Object[]{getContext(), false});
            StorageResult result = accountManager.readData(MASAuthCredentials.REGISTRATION_TYPE);
            if (result.getStatus().equals(StorageResult.StorageOperationStatus.SUCCESS)) {
                return new String((byte[]) result.getData());
            }
        } catch (StorageException e) {
            if (DEBUG)
                Log.w(TAG, "Unable to retrieve last authenticated credentials type from local storage.", e);
        }

        return "";
    }

    public abstract Bitmap getThumbnailImage();

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


    @Xamarin("Xarmarin may have a defect binding on method with Generic, " +
            "temporary add below methods to resolve binding error")
    public abstract void getUserById(String id, MASCallback<MASUser> callback);

    public abstract void getUsersByFilter(MASFilteredRequest filteredRequest, MASCallback<List<MASUser>> callback);

    public abstract void getUserMetaData(MASCallback<UserAttributes> callback);

    public abstract void sendMessage(MASTopic topic, MASMessage message, MASCallback<Void> callback);

    public abstract void sendMessage(MASMessage message, MASUser user, MASCallback<Void> callback);

    public abstract void sendMessage(MASMessage message, MASUser user, String topic, MASCallback<Void> callback);

    public abstract void sendMessage(MASMessage message, MASGroup group, MASCallback<Void> callback);

    public abstract void sendMessage(MASMessage message, MASGroup group, String topic, MASCallback<Void> callback);

    public abstract void startListeningToMyMessages(MASCallback<Void> callback);

    public abstract void stopListeningToMyMessages(MASCallback<Void> callback);

}
