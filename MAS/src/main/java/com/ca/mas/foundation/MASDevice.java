/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.foundation;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Parcel;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.ConfigurationProvider;
import com.ca.mas.core.context.DeviceIdentifier;
import com.ca.mas.core.security.DefaultEncryptionProvider;
import com.ca.mas.core.security.FingerprintListener;
import com.ca.mas.core.security.LockableKeyStorageProvider;
import com.ca.mas.core.security.UserNotAuthenticatedException;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;
import com.ca.mas.core.store.TokenStoreException;
import com.ca.mas.core.token.IdToken;
import com.ca.mas.core.token.JWTValidation;
import com.ca.mas.foundation.auth.MASProximityLoginBLEPeripheralListener;
import com.ca.mas.foundation.notify.Callback;
import com.ca.mas.foundation.util.FoundationUtil;

/**
 * <p>The <b>MASDevice</b> class is a local representation of device data.</p>
 */
public abstract class MASDevice implements Device {
    private static String ENCRYPTED_KEY = MASDevice.class.getSimpleName() + "EncryptedKey";
    private static String SUFFIX = MASDevice.class.getSimpleName() + "RandomSuffix";
    private static LockableKeyStorageProvider mKeyStoreProvider;
    private static MASDevice current;

    private MASDevice() {
    }

    public static MASDevice getCurrentDevice() {
        if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            mKeyStoreProvider = new LockableKeyStorageProvider(SUFFIX);
        }

        if (current == null) {
            current = new MASDevice() {
                @Override
                public void deregister(final MASCallback<Void> callback) {
                    final MobileSso mobileSso = MobileSsoFactory.getInstance();
                    if (mobileSso != null && mobileSso.isDeviceRegistered()) {
                        Thread t = new Thread(new Runnable() {
                            public void run() {
                                try {
                                    mobileSso.removeDeviceRegistration();
                                    TokenManager manager = createTokenManager();
                                    if (manager.getSecureIdToken() != null) {
                                        manager.deleteSecureIdToken();
                                    }
                                    Callback.onSuccess(callback, null);
                                } catch (Exception e) {
                                    Callback.onError(callback, e);
                                }
                            }
                        });
                        t.start();
                    } else {
                        Callback.onError(callback, new IllegalStateException("Device is not registered"));
                    }
                }

                @Override
                public boolean isRegistered() {
                    MobileSso mobileSso = FoundationUtil.getMobileSso();
                    return mobileSso.isDeviceRegistered();
                }

                @Override
                public void resetLocally() {
                    FoundationUtil.getMobileSso().destroyAllPersistentTokens();
                }

                @Override
                public String getIdentifier() {
                    return (new DeviceIdentifier(MAS.getContext())).toString();
                }

                @Override
                public void startAsBluetoothPeripheral(MASProximityLoginBLEPeripheralListener listener) {
                    MobileSsoFactory.getInstance().startBleSessionSharing(listener);
                }

                @Override
                public void stopAsBluetoothPeripheral() {
                    MobileSsoFactory.getInstance().stopBleSessionSharing();
                }

                @Override
                @TargetApi(23)
                public void lock(MASCallback<Void> callback) {
                    MASUser currentUser = MASUser.getCurrentUser();
                    if (currentUser == null || !currentUser.isAuthenticated()) {
                        callback.onError(new MASException("No currently authenticated user."));
                    } else if (isLocked()) {
                        callback.onError(new MASException("Device is already locked."));
                    } else {
                        // Remove access and refresh tokens
//                        MssoContext context = MssoContext.newContext();
//                        context.clearAccessToken();
                        TokenManager keyChainManager = createTokenManager();

                        // Remove persisted user profile
//                        try {
//                            keyChainManager.deleteUserProfile();
//                        } catch (TokenStoreException e) {
//                            callback.onError(e);
//                        }

                        // Validate that the ID token is not expired
                        IdToken idToken = keyChainManager.getIdToken();
                        boolean isTokenExpired = JWTValidation.isIdTokenExpired(idToken);
                        if (!isTokenExpired) {
                            // Move the ID token from the Keychain to the fingerprint protected shared Keystore
                            Parcel idTokenParcel = Parcel.obtain();
                            idToken.writeToParcel(idTokenParcel, 0);
                            byte[] idTokenBytes = idTokenParcel.marshall();

                            DefaultEncryptionProvider encryptionProvider = new DefaultEncryptionProvider(MAS.getContext(), mKeyStoreProvider);
                            byte[] encryptedData = encryptionProvider.encrypt(idTokenBytes);
                            // Save the encrypted token
                            try {
                                keyChainManager.saveSecureIdToken(encryptedData);
                            } catch (TokenStoreException e) {
                                callback.onError(new MASException("Could not save encrypted ID token."));
                            }

                            // Remove the unencrypted token
                            try {
                                keyChainManager.deleteIdToken();
                            } catch (TokenStoreException e) {
                                callback.onError(new MASException("Failed to delete ID token."));
                            }

                            mKeyStoreProvider.lock();
                            idTokenParcel.recycle();

                            callback.onSuccess(null);
                        } else {
                            callback.onError(new MASException("ID token is expired."));
                        }
                    }
                }

                @Override
                @TargetApi(23)
                public void unlock(FingerprintListener listener, MASCallback<Void> callback) {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    FingerprintManager manager = context.getSystemService(FingerprintManager.class);
//                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
//                        // Fingerprint permission not provided
//                    } else if (!manager.isHardwareDetected()) {
//                        // Fingerprint hardware not provided
//                    } else if (!manager.hasEnrolledFingerprints()) {
//                        // User hasn't enrolled any fingerprints to authenticate with
//                    } else {
////                            manager.authenticate();
//                    }
//                }
                    if (isLocked()) {
                        TokenManager keyChainManager = createTokenManager();

                        // Unlock the ID token from the Keystore and places the decrypted ID token back to the Keychain
                        byte[] secureIdToken = keyChainManager.getSecureIdToken();

                        DefaultEncryptionProvider encryptionProvider = new DefaultEncryptionProvider(MAS.getContext(), mKeyStoreProvider);
                        // Read the decrypted data, reconstruct it as a Parcel, then as an IdToken
                        Parcel parcel = Parcel.obtain();
                        try {
                            // Decrypt the encrypted ID token
                            byte[] decryptedData = encryptionProvider.decrypt(secureIdToken);
                            parcel.unmarshall(decryptedData, 0, decryptedData.length);
                            parcel.setDataPosition(0);

                            IdToken token = IdToken.CREATOR.createFromParcel(parcel);
                            try {
                                // Save the unlocked ID token
                                keyChainManager.saveIdToken(token);
                            } catch (TokenStoreException e) {
                                callback.onError(new MASException("Failed to save ID token."));
                            }

                            try {
                                // Remove the locked ID token
                                keyChainManager.deleteSecureIdToken();
                            } catch (TokenStoreException e) {
                                callback.onError(new MASException("Failed to delete encrypt ID token."));
                            }

                            // Indicate the device is unlocked
                            callback.onSuccess(null);
                        } catch (Exception e) {
                            if (e instanceof UserNotAuthenticatedException
                                    || e.getCause() instanceof android.security.keystore.UserNotAuthenticatedException) {
                                // Listener activity to trigger fingerprint
                                listener.triggerDeviceUnlock();
                            }
                        }
                    } else {
                        callback.onError(new MASException("Device is already unlocked."));
                    }
                }

                @Override
                @TargetApi(23)
                public boolean isLocked() {
                    TokenManager keyChainManager = createTokenManager();
                    return keyChainManager.getSecureIdToken() != null;
                }
            };
        }
        return current;
    }

    private static TokenManager createTokenManager() {
        ConfigurationProvider configurationProvider = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider();
        StorageProvider storageProvider = new StorageProvider(MAS.getContext(), configurationProvider);
        return storageProvider.createTokenManager();
    }
}
