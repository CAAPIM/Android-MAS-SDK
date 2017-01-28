/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyProperties;
import android.security.keystore.KeyProtection;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.ca.mas.core.security.DefaultKeySymmetricManager;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import javax.crypto.SecretKey;
import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import static android.R.attr.key;
import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

@RequiresApi(api = Build.VERSION_CODES.M)
public class LockableKeyStorageProvider implements KeyStorageProvider {

    // will be generating symmetric keys protected by lock screen
    protected DefaultKeySymmetricManager keyMgr = null;
    protected SecretKey secretKey;


    /**
     * Retrieve the SecretKey from Storage
     * @param alias : The alias to find the Key
     * @return The SecretKey
     */
    @Override
    public SecretKey getKey(String alias)
    {
        if (keyMgr == null)
            keyMgr = new DefaultKeySymmetricManager("AES", 256, true, 5);

        if (secretKey == null) {
            SecretKey secretKey = keyMgr.retrieveKey(alias);

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
            keyMgr = new DefaultKeySymmetricManager("AES", 256, true, 5);

        keyMgr.deleteKey(alias);
        secretKey = null;
        return true;
    }


    public void lock() {
        if (secretKey != null && secretKey instanceof Destroyable) {
            Destroyable destroyable = (Destroyable) secretKey;
            try {
                destroyable.destroy();
            } catch (DestroyFailedException e) {
                if (DEBUG) Log.e(TAG, "Could not destroy key");
            }
        }
        secretKey = null;
    }
}
