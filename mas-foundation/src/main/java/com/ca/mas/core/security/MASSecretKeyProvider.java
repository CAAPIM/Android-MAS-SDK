/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.security;

import android.content.Context;
import android.support.annotation.NonNull;
import com.ca.mas.core.storage.sharedstorage.AccountManagerUtil;
import com.ca.mas.foundation.MAS;

public class MASSecretKeyProvider extends KeyStoreKeyStorageProvider {
    private AccountManagerUtil storage;
    private String acName;

    /**
     * Default constructor.
     *
     * @param ctx context
     */
    public MASSecretKeyProvider(@NonNull Context ctx, String accountName) {
        super(ctx);
        this.acName = accountName;
        createStorageInstance();

    }

    @Override
    boolean storeSecretKeyLocally(String alias, byte[] encryptedSecretKey) {
        createStorageInstance();
        storage.save(alias, encryptedSecretKey);
        return true;
    }

    @Override
    byte[] getEncryptedSecretKey(String alias) {
        createStorageInstance();
        return storage.getBytes(alias);
    }

    @Override
    boolean deleteSecretKeyLocally(String alias) {
        createStorageInstance();
        storage.delete(alias);
        return true;
    }

    private void createStorageInstance(){
        if (storage == null) {
            storage = new AccountManagerUtil(MAS.getContext(), acName, true);
        }
    }
}
