package com.ca.mas.core.storage.securesharedstorage;

import android.support.annotation.NonNull;

import com.ca.mas.core.security.DefaultEncryptionProvider;
import com.ca.mas.core.security.EncryptionProvider;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;
import com.ca.mas.foundation.MAS;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class SecureSharedStorage extends MASSharedStorage {

    private EncryptionProvider encProvider = null;

    /**
     * Creates or retrieves a SecureSharedStorage with the specified name.
     * Ensure that this does not conflict with any existing accountName on the device.
     *
     * @param accountName the name of the account to be created in the AccountManager
     */
    public SecureSharedStorage(@NonNull String accountName, boolean activeSecureMode) {
        super(accountName);

        if (activeSecureMode) {
            encProvider = new DefaultEncryptionProvider(MAS.getContext());
        }
    }

    @Override
    public void save(@NonNull String key, String value){
        isValidKey(key);

        String retValue = value;

        if (encProvider != null) {
            try {
                byte[] encryptedValue = encProvider.encrypt(value.getBytes());
                retValue = new String(encryptedValue, "ISO-8859-1");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
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
        isValidKey(key);

        String retValue = super.getString(key);

        if (encProvider != null && retValue != null) {
            try {
                byte[] encodedbytes = encProvider.decrypt(retValue.getBytes("ISO-8859-1"));
                retValue = new String(encodedbytes, Charset.forName("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        return retValue;
    }

    @Override
    public byte[] getBytes(String key) {
        isValidKey(key);

        byte[] retValue = super.getBytes(key);
        if (encProvider != null && retValue != null) {
            retValue = encProvider.decrypt(retValue);
        }
        return retValue;
    }

    private void isValidKey(String key) {
        if (key == null || key.isEmpty()) {return;}
    }
}
