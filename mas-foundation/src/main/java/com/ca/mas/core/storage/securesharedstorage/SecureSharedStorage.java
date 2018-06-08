package com.ca.mas.core.storage.securesharedstorage;

import android.support.annotation.NonNull;

import com.ca.mas.core.security.DefaultEncryptionProvider;
import com.ca.mas.core.security.EncryptionProvider;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;
import com.ca.mas.foundation.MAS;

import java.nio.charset.Charset;

public class SecureSharedStorage extends MASSharedStorage {

    private EncryptionProvider encProvider;
    private boolean secureMode = false;

    /**
     * Creates or retrieves a MASSharedStorage with the specified name and account type.
     * Ensure that this does not conflict with any existing accountType on the device.
     *
     * @param accountName the name of the account to be created in the AccountManager
     */
    public SecureSharedStorage(@NonNull String accountName, boolean activeSecureMode) {
        super(accountName);

        if (secureMode) {
            encProvider = new DefaultEncryptionProvider(MAS.getContext());
        }
    }

    @Override
    public void save(@NonNull String key, String value) {
        isValidKey(key);

        String retValue = value;

        if (encProvider != null) {
            byte[] encryptedValue = encProvider.encrypt(value.getBytes());
            retValue = new String(encryptedValue, Charset.forName("UTF-8"));
        }

        super.save(key, retValue);
    }

    @Override
    public void save(@NonNull String key, byte[] value) {
        isValidKey(key);

        byte[] retValue = value;

        if (encProvider != null) {
            retValue = encProvider.encrypt(value);
        }

        super.save(key, retValue);
    }

    @Override
    public void delete(@NonNull String key) {
        isValidKey(key);
        super.delete(key);
    }

    @Override
    public String getString(String key) {
        //TODO: what happend if I try to get some data that was stored without encryptation, and now is active the encryptation mode ??
        isValidKey(key);

        String retValue = super.getString(key);

        if (encProvider != null) {
            retValue = new String(encProvider.decrypt(retValue.getBytes()), Charset.forName("UTF-8"));
        }

        return retValue;
    }

    @Override
    public byte[] getBytes(String key) {
        //TODO: what happend if I try to get some data that was stored without encryptation, and now is active the encryptation mode ??
        isValidKey(key);
        return super.getBytes(key);
    }

    private void isValidKey(String key) {
        if (key == null || key.isEmpty()) {return;}
    }
}
