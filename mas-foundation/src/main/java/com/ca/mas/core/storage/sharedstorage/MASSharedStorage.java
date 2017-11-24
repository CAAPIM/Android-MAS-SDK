/*
 * Copyright (c) 2017 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.core.storage.sharedstorage;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.foundation.MAS;

import org.xmlpull.v1.XmlPullParser;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashSet;
import java.util.Set;

import static com.ca.mas.foundation.MAS.TAG;
import static com.ca.mas.foundation.MAS.getContext;

/**
 * MASSharedStorage provides the ability to share data between apps signed with the same key.
 * <p>
 * Requires the android.permission.AUTHENTICATE_ACCOUNTS and
 * android:name="android.permission.MANAGE_ACCOUNTS" permissions
 * in your application's AndroidManifest.xml.
 * <p>
 * The underlying implementation of this class uses the AccountManager APIs.
 * By default, the massharedauthenticator.xml file will be used to create the accounts,
 * but can be overridden in AndroidManifest.xml with your own xml file for another account type.
 */
@SuppressWarnings({"MissingPermission"})
public class MASSharedStorage {

    private static final Object mutex = new Object();
    private static final String KEYINDEX_COLUMN_NAME = "mas_shared_storage_lookup_index";
    private AccountManager mAccountManager;
    private Account mAccount;
    private AccountIndexFormatter mAccountIndexFormatter;

    /**
     * Creates or retrieves a MASSharedStorage with the specified name and account type.
     * Ensure that this does not conflict with any existing accountType on the device.
     *
     * @param accountName
     */
    public MASSharedStorage(@NonNull String accountName) {
        if (accountName == null || accountName.isEmpty()) {
            throw new IllegalArgumentException("Account name cannot be null.");
        }

        // Gets the account type from the manifest
        String accountType = getAccountType(MAS.getContext());
        if (accountType == null || accountType.isEmpty()) {
            throw new IllegalArgumentException("Account type cannot be null.");
        }

        try {
            MASSharedStorageIdentifier identifier = new MASSharedStorageIdentifier(getContext());

            mAccountManager = AccountManager.get(MAS.getContext());
            //Attempt to retrieve the account
            Account[] accounts = mAccountManager.getAccountsByType(accountType);
            for (Account account : accounts) {
                if (accountName.equals(account.name)) {
                    String password = mAccountManager.getPassword(account);
                    String savedPassword = identifier.toString();
                    if (password.equals(savedPassword)) {
                        mAccount = account;
                    } else {
                        throw new IllegalArgumentException("Account signature does not match existing signature.");
                    }
                }
            }

            //Create the account if it wasn't retrieved,
            if (mAccount == null) {
                mAccount = new Account(accountName, accountType);
                mAccountManager.addAccountExplicitly(mAccount, identifier.toString(), null);
            }
        } catch (Exception e) {

        }
    }

    private void preconditionCheck(String key) {
        //If the SDK hasn't been initialized, throw an IllegalStateException
        MobileSsoFactory.getInstance();

        //If the data key is null, throw an exception
        if (key == null || key.isEmpty()) {
            throw new NullPointerException("Data key cannot be null or empty.");
        }
    }

    // Parses the account type for MASAuthenticatorService from the authenticator xml file
    // specified in the application's AndroidManifest.xml.
    private String getAccountType(Context context) {
        try {
            // Get the authenticator XML file from AndroidManifest.xml
            ComponentName cn = new ComponentName(context, MASAuthenticatorService.class);
            ServiceInfo info = context.getPackageManager().getServiceInfo(cn, PackageManager.GET_META_DATA);
            int resourceId = info.metaData.getInt("android.accounts.AccountAuthenticator");

            // Parse the authenticator XML file to get the accountType
            return parseResourceXml(context, resourceId);
        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
            Log.e(TAG, "Failed to load meta-data from the service: " + e.getMessage());
            return null;
        }
    }

    private String parseResourceXml(Context context, int resourceId) {
        try {
            XmlResourceParser xrp = context.getResources().getXml(resourceId);
            xrp.next();
            int eventType = xrp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG || eventType == XmlPullParser.END_TAG) {
                    // Looks through the authenticator XML file and returns the android:accountType value
                    for (int i = 0; i < xrp.getAttributeCount(); i++) {
                        String name = xrp.getAttributeName(i);
                        if ("accountType".equals(name)) {
                            return xrp.getAttributeValue(i);
                        }
                    }
                }
                eventType = xrp.next();
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to load meta-data from the service: " + e.getMessage());
        }
        return null;
    }

    /**
     * Saves a string value to the shared storage with the given key.
     *
     * @param key
     * @param value
     */
    public void save(@NonNull String key, String value) {
        preconditionCheck(key);
        mAccountManager.setUserData(mAccount, key, value == null ? "" : value);
        updateIndex(key, true);
    }

    /**
     * Saves a byte array to the shared storage with the given key.
     *
     * @param key
     * @param value
     */
    public void save(@NonNull String key, byte[] value) {
        preconditionCheck(key);
        mAccountManager.setUserData(mAccount, key, value == null ? "" : new String(value));
        updateIndex(key, true);
    }

    /**
     * Deletes a value in the shared storage assigned to the given key.
     * Functionally the same as calling save(key, null).
     *
     * @param key
     */
    public void delete(@NonNull String key) {
        preconditionCheck(key);
        mAccountManager.setUserData(mAccount, key, null);
        updateIndex(key, false);
    }

    // Updates the key index in the AccountManager by adding or removing the key
    private void updateIndex(@NonNull String key, boolean add) {
        synchronized (mutex) {
            Set<String> keys = keySet();
            if (add) {
                keys.add(key);
            } else {
                keys.remove(key);
            }
            byte[] bytes = marshal(keys);
            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
            mAccountManager.setUserData(mAccount, KEYINDEX_COLUMN_NAME, encoded);
        }
    }

    // Converts a Set<String> into a byte[] configuration
    private byte[] marshal(Set<String> keys) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(keys);
            return bos.toByteArray();
        } catch (IOException e) {
            return new byte[0];
        }
    }

    // Converts a String representation from the AccountManager into a Set<String>
    private Set<String> unmarshal(String encodedKeyString) {
        if (encodedKeyString == null) {
            return new HashSet<>();
        }

        byte[] bytes = Base64.decode(encodedKeyString, Base64.DEFAULT);
        try {
            ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bis);
            return (HashSet<String>) ois.readObject();
        } catch (ClassNotFoundException | IOException e) {
            return new HashSet<>();
        }
    }

    /**
     * Retrieves a string value in the shared storage given by the key.
     *
     * @param key
     * @return value associated with the key
     */
    public String getString(String key) {
        preconditionCheck(key);
        return mAccountManager.getUserData(mAccount, key);
    }

    /**
     * Retrieves a byte array in the shared storage given by the key.
     *
     * @param key
     * @return value associated with the key
     */
    public byte[] getBytes(String key) {
        preconditionCheck(key);
        String byteString = mAccountManager.getUserData(mAccount, key);
        if (byteString != null) {
            return byteString.getBytes();
        }
        return null;
    }

    private Set<String> keySet() {
        String keyString = mAccountManager.getUserData(mAccount, KEYINDEX_COLUMN_NAME);
        return unmarshal(keyString);
    }

    // In charge of key index marshalling and unmarshalling.
    private class AccountIndexFormatter implements Parcelable {

        private byte[] keyBytes;

        AccountIndexFormatter(Parcel in) {
            if (in != null) {
                this.keyBytes = in.createByteArray();
            }
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeByteArray(keyBytes);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public final Parcelable.Creator CREATOR = new Parcelable.Creator<AccountIndexFormatter>() {
            public AccountIndexFormatter createFromParcel(Parcel in) {
                return new AccountIndexFormatter(in);
            }

            public AccountIndexFormatter[] newArray(int size) {
                return new AccountIndexFormatter[size];
            }
        };
    }
}
