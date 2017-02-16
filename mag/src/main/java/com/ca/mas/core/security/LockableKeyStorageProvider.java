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

import java.util.concurrent.ConcurrentHashMap;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

@RequiresApi(api = Build.VERSION_CODES.M)
public class LockableKeyStorageProvider implements KeyStorageProvider {

    // will be generating symmetric keys protected by lock screen
    protected DefaultKeySymmetricManager keyMgr = null;

    protected ConcurrentHashMap<String, SecretKey> secretKeys = new ConcurrentHashMap<String, SecretKey>();


    /**
     * Retrieve the SecretKey from Storage
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    @Override
    public SecretKey getKey(String alias)
    {
        // Symmetric keys will be generated in memory then stored in AndroidKeyStore
        if (keyMgr == null)
            keyMgr = new DefaultKeySymmetricManager("AES", 256, true, true, 10);

        SecretKey secretKey = secretKeys.get(alias);
        if (secretKey == null) {
            secretKey = keyMgr.retrieveKey(alias);

            // if still no key, generate one
            if (secretKey == null)
                secretKey = keyMgr.generateKey(alias);
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
        if (keyMgr == null)
            keyMgr = new DefaultKeySymmetricManager("AES", 256, true, true, 10);

        secretKeys.remove(alias);
        keyMgr.deleteKey(alias);
        return true;
    }


    public void lock(String alias) {
        SecretKey secretKey = secretKeys.remove(alias);
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
