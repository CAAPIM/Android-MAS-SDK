package com.ca.mas.core.storage.securesharedstorage;

import android.support.annotation.NonNull;
import android.util.Log;

import com.ca.mas.core.security.DefaultEncryptionProvider;
import com.ca.mas.core.security.EncryptionProvider;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;
import com.ca.mas.foundation.MAS;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class MASSecureSharedStorage extends MASSharedStorage {

    private EncryptionProvider encProvider = null;
    private boolean secureMode;
    private static final String LOGTAG = "MASSecureSharedStorage";

    /**
     * Creates or retrieves a MASSecureSharedStorage with the specified name.
     * Ensure that this does not conflict with any existing accountName on the device.
     *
     * @param accountName the name of the account to be created in the AccountManager
     */
    public MASSecureSharedStorage(@NonNull String accountName, boolean activeSecureMode) {
        super(accountName);
        secureMode = activeSecureMode;
    }

    @Override
    public void save(@NonNull String key, String value) {
        preconditionCheck(key);

        String retValue = value;

        try {
            byte[] encryptedValue = getEncryptionProvider().encrypt(value.getBytes());
            retValue = new String(encryptedValue, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            Log.e(LOGTAG, e.getMessage());
        }


        super.save(key, retValue);
    }

    @Override
    public void save(@NonNull String key, byte[] value) {
        preconditionCheck(key);

        super.save(key, getEncryptionProvider().encrypt(value));
    }

    @Override
    public void delete(@NonNull String key) {
        super.delete(key);
    }

    @Override
    public String getString(String key) {
        preconditionCheck(key);

        String retValue = super.getString(key);

        if (retValue != null) {
            try {
                byte[] encodedbytes = getEncryptionProvider().decrypt(retValue.getBytes("ISO-8859-1"));
                retValue = new String(encodedbytes);
            } catch (UnsupportedEncodingException e) {
                Log.e(LOGTAG, e.getMessage());
                retValue = null;
            }
        }

        return retValue;
    }

    @Override
    public byte[] getBytes(String key) {
        preconditionCheck(key);

        byte[] retValue = super.getBytes(key);

        if (retValue != null) {
            retValue = getEncryptionProvider().decrypt(retValue);
        }

        return retValue;
    }

    @Override
    protected EncryptionProvider getEncryptionProvider () {
        if (secureMode) {
            encProvider = new DefaultEncryptionProvider(MAS.getContext());
        } else {
            encProvider = super.getEncryptionProvider();
        }
        return encProvider;
    }

}
