package com.ca.mas.core.storage.securestoragesource;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.ca.mas.core.security.DefaultEncryptionProvider;
import com.ca.mas.core.security.EncryptionProvider;
import com.ca.mas.core.storage.storagesource.MASStorageSource;
import com.ca.mas.foundation.MAS;

import java.nio.charset.Charset;

import static com.ca.mas.core.client.ServerClient.UTF_8;

public class MASSecureStorageSource extends MASStorageSource {

    private EncryptionProvider encProvider = null;
    private boolean secureMode;
    private static final String LOGTAG = "MASSecureSharedStorage";

    /**
     * Creates or retrieves a MASSecureSharedStorage with the specified name.
     * Ensure that this does not conflict with any existing accountName on the device.
     *
     * @param accountName the name of the account to be created in the AccountManager
     */
    public MASSecureStorageSource(@NonNull String accountName, boolean activeSecureMode, boolean sharedMode) {
        super(accountName, sharedMode);
        secureMode = activeSecureMode;
    }

    @Override
    public void save(@NonNull String key, String value) {
        preconditionCheck(key);

        Charset charSet = Charset.forName(UTF_8);
        byte[] encrypted = getEncryptionProvider().encrypt(value.getBytes(charSet));
        super.save(key, Base64.encodeToString(encrypted, Base64.NO_WRAP));
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
            Charset charSet = Charset.forName(UTF_8);
            byte[] encodedbytes = getEncryptionProvider().decrypt(Base64.decode(retValue, Base64.NO_WRAP));
            retValue = new String(encodedbytes, charSet);

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
