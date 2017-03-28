/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.ca.mas.core.util.KeyUtilsSymmetric;

import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

@RequiresApi(api = Build.VERSION_CODES.M)
public class LockableKeyStorageProvider implements KeyStorageProvider {

    /**
     * Save the secret key so we don't have to get it out of the AndroidKeyStore.
     *   This way, we can generate the key in memory and use it without authentication,
     *   but then require authentication when using for unlock.
     */
    protected ConcurrentHashMap<String, SecretKey> secretKeys = new ConcurrentHashMap<String, SecretKey>();


    /**
     * Retrieve the SecretKey from Storage
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    @Override
    public SecretKey getKey(String alias)
    {
        SecretKey secretKey = secretKeys.get(alias);
        if (secretKey == null) {
            secretKey = KeyUtilsSymmetric.retrieveKey(alias);
            if (secretKey == null) {
                secretKey = KeyUtilsSymmetric.generateKey(alias, "AES", 256,
                                     false, true, 10, false);
                secretKeys.put(alias, secretKey);
            }
        }

        return secretKey;
    }

    /**
     * Remove the key
     * @param alias the alias of the key to remove
     */
    @Override
    public boolean removeKey(String alias)
    {
        secretKeys.remove(alias);
        KeyUtilsSymmetric.deleteKey(alias);
        return true;
    }


    public void lock(String alias) {
        SecretKey secretKey = secretKeys.get(alias);
        if (secretKey != null && secretKey instanceof Destroyable) {
            Destroyable destroyable = (Destroyable) secretKey;
            try {
                destroyable.destroy();
            } catch (DestroyFailedException e) {
                if (DEBUG) Log.e(TAG, "Could not destroy key");
            }
        }
    }
}
