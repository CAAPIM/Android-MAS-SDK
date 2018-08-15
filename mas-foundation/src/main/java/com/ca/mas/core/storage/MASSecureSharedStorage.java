package com.ca.mas.core.storage;

import android.support.annotation.NonNull;
import android.util.Base64;

import com.ca.mas.core.security.DefaultEncryptionProvider;
import com.ca.mas.core.security.EncryptionProvider;
import com.ca.mas.core.security.MASSecretKeyProvider;
import com.ca.mas.core.storage.sharedstorage.MASSharedStorage;
import com.ca.mas.foundation.MAS;

import java.nio.charset.Charset;

import static com.ca.mas.core.client.ServerClient.UTF_8;

public class MASSecureSharedStorage extends MASSharedStorage {

    private boolean secureMode;
    private MASSecretKeyProvider secretKeyProvider;

    /**
     * Creates or retrieves a MASSecureSharedStorage with the specified name.
     * Ensure that this does not conflict with any existing accountName on the device.
     *
     * @param accountName the name of the account to be created in the AccountManager
     */
    public MASSecureSharedStorage(@NonNull String accountName, boolean activeSecureMode, boolean sharedMode, boolean storageMode) {
        super(accountName, sharedMode, storageMode);
        secureMode = activeSecureMode;
        secretKeyProvider = new MASSecretKeyProvider(MAS.getContext(), accountName);
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
    public String getString(String key) {
        preconditionCheck(key);

        String retValue = super.getString(key);

        if (retValue != null) {
            Charset charSet = Charset.forName(UTF_8);
            try {
                byte[] encodedbytes = getEncryptionProvider().decrypt(Base64.decode(retValue, Base64.NO_WRAP));
                retValue = new String(encodedbytes, charSet);
            } catch (Exception e) {
                delete(key);
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
            try {
                retValue = getEncryptionProvider().decrypt(retValue);
            } catch (Exception e) {
                delete(key);
                retValue = null;
            }
        }

        return retValue;
    }

    @Override
    protected EncryptionProvider getEncryptionProvider () {
        EncryptionProvider encProvider;

        if (secureMode) {
            encProvider = new DefaultEncryptionProvider(MAS.getContext(), secretKeyProvider) {
                @Override
                protected String getKeyAlias() {
                    return "com.ca.mas.ACCOUNT_MANAGER_SECRET";
                }
            };
        } else {
            encProvider = super.getEncryptionProvider();
        }
        return encProvider;
    }

}
