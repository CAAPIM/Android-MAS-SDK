/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import javax.crypto.SecretKey;

import static com.ca.mas.core.MAG.TAG;

/**
 * This class supports key storage.  It will require a screen lock,
 *   but the screen lock can change.  For Android pre-N, this is 
 *   managed entirely by checking KeyguardManager.isDeviceSecure()
 *   to determine if there is still a screen lock.  If the screen
 *   lock, pin/swipe/password, is removed entirely, the keys are
 *   deleted.  For Android.N, the keys are also protected inside
 *   the AndroidKeyStore requiring a screen lock.
 */

class KeyStorageScreenLockCanChange extends SharedPreferencesKeyStorageProvider {

    public static final String PREFS_NAME = "SECRET_PREFS";
    private SharedPreferences sharedpreferences;

    /**
     * Constructor to KeyStorageProvider
     *
     * @param ctx requires context of the calling application
     */
    public KeyStorageScreenLockCanChange(@NonNull Context ctx) {
        super(ctx);

        // Symmetric Key Manager creates symmetric keys,
        //   stored inside AndroidKeyStore for Android.M+
        keyMgr = new DefaultKeySymmetricManager("AES", 256, false, false, 100000, false);
    }


    /**
     * Determine if there is a screen lock
     */
    protected boolean deviceHasScreenLock()
    {
        try {
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if ( km.isDeviceSecure() )
                    return true;
                else
                    return false;
            } else {
                if ( km.isKeyguardSecure() )
                    return true;
                else
                    return false;
            }
        } catch (Exception x) {
            Log.e(TAG, "Exception determining if screen has a lock (pin/swipe/password), will be assuming it does not", x);
            return false;
        }
    }


    /**
     * Retrieve the SecretKey from Storage
     *
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    @Override
    public SecretKey getKey(String alias) {

        // if there is no screen lock, then delete the key and return nothing!!!
        if (! deviceHasScreenLock()) {
            Log.w(TAG, "KeyStorageScreenLockCanChange getKey there is no screen lock (pin/swipe/password), so the key will be deleted");
            removeKey(alias);
            throw new RuntimeException("KeyStorageScreenLockCanChange getKey there is no screen lock (pin/swipe/password), so the encryption key has been deleted");
        }

        return super.getKey(alias);
    }


}
