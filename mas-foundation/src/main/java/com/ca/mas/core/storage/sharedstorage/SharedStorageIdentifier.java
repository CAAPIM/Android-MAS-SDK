/*
 * Copyright (c) 2017 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.storage.sharedstorage;

import android.content.Context;

import com.ca.mas.core.context.UniqueIdentifier;
import com.ca.mas.core.security.KeyStoreException;

import java.security.NoSuchAlgorithmException;

class SharedStorageIdentifier extends UniqueIdentifier {

    /**
     * Generates a set of asymmetric keys in the Android keystore for use with the AccountManager.
     * Apps built with the same sharedUserId value in AndroidManifest.xml will reuse the same identifier.
     * @param context
     */
    SharedStorageIdentifier(Context context) throws KeyStoreException, NoSuchAlgorithmException {
        super(context);
    }

    @Override
    protected String getIdentifierKey() {
        return "com.ca.mas.core.storage.sharedstorage.DEVICE_IDENTIFIER";
    }

}
