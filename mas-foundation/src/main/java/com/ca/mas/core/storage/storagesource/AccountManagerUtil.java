package com.ca.mas.core.storage.storagesource;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.content.res.XmlResourceParser;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;

import com.ca.mas.core.storage.StorageActions;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASFoundationStrings;
import com.ca.mas.foundation.MASSharedStorageException;

import org.xmlpull.v1.XmlPullParser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.ca.mas.foundation.MAS.TAG;

public class AccountManagerUtil implements StorageActions {

    private static final Object mutex = new Object();
    private static final String KEYINDEX_COLUMN_NAME = "mas_shared_storage_lookup_index";
    private AccountManager mAccountManager;

    private Account mAccount;

    public AccountManagerUtil(Context context, String accountName){

        // Gets the account type from the manifest
        String accountType = getAccountType(context);
        if (accountType == null || accountType.isEmpty() || !checkACMPermissions()) {
            throw new IllegalArgumentException(MASFoundationStrings.SHARED_STORAGE_NULL_ACCOUNT_TYPE);
        }

        try {
            SharedStorageIdentifier identifier = new SharedStorageIdentifier();

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
            throw new MASSharedStorageException(e.getMessage(), e);
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
                    // Looks through the authenticator XML file attributes and returns
                    // the value for android:accountType
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

    // Updates the key index in the AccountManager by adding or removing the key
    private void updateIndex(@NonNull String key, boolean add) {
        synchronized (mutex) {
            Set<String> keys = getKeySet();
            if (add) {
                keys.add(key);
            } else {
                keys.remove(key);
            }
            byte[] bytes = marshall(keys);
            String encoded = Base64.encodeToString(bytes, Base64.DEFAULT);
            mAccountManager.setUserData(mAccount, KEYINDEX_COLUMN_NAME, encoded);
        }
    }

    // Converts a Set<String> into a byte[] configuration
    private byte[] marshall(Set<String> keys) {
        Parcel p = Parcel.obtain();
        List<String> strings = new ArrayList<>(keys);
        p.writeStringList(strings);
        byte[] bytes = p.marshall();
        p.recycle();
        return bytes;
    }

    // Converts a String representation from the AccountManager into a Set<String>
    private List<String> unmarshall(String encodedKeyString) {
        List<String> result = new ArrayList<>();
        if (encodedKeyString == null) {
            return result;
        }

        byte[] bytes = Base64.decode(encodedKeyString, Base64.DEFAULT);

        Parcel p = Parcel.obtain();
        p.unmarshall(bytes, 0, bytes.length);
        p.setDataPosition(0);
        result = p.createStringArrayList();
        p.recycle();
        return result;
    }

    private Set<String> getKeySet() {
        String keyString = mAccountManager.getUserData(mAccount, KEYINDEX_COLUMN_NAME);
        return new HashSet<>(unmarshall(keyString));
    }


    @Override
    public void save(@NonNull String key, String value) {
        mAccountManager.setUserData(mAccount, key,value == null ? "" : value);
        updateIndex(key, true);
    }

    @Override
    public void save(@NonNull String key, byte[] value) {
        mAccountManager.setUserData(mAccount, key, value == null ? "" : Base64.encodeToString(value, Base64.DEFAULT));
        updateIndex(key, true);
    }

    @Override
    public void delete(@NonNull String key) {
        mAccountManager.setUserData(mAccount, key, null);
        updateIndex(key, false);
    }

    @Override
    public String getString(@NonNull String key) {
        return  mAccountManager.getUserData(mAccount, key);
    }

    @Override
    public byte[] getBytes(@NonNull String key) {
        String byteString = mAccountManager.getUserData(mAccount, key);
        if (byteString != null) {
            return Base64.decode(byteString, Base64.DEFAULT);
        }
        return null;
    }

    private boolean checkACMPermissions() {
        String oAuthAccounts = "android.permission.AUTHENTICATE_ACCOUNTS";
        String getAccounts = Manifest.permission.GET_ACCOUNTS;

        return checkPermission(oAuthAccounts) && checkPermission(getAccounts);
    }

    private boolean checkPermission(String permission) {
        boolean retValue = false;
        PackageInfo info = null;

        String packageName = MAS.getContext().getPackageName();

        try {
            info = MAS.getContext().getPackageManager().getPackageInfo(packageName, PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (info!= null && info.requestedPermissions != null) {
            for (String p : info.requestedPermissions) {
                if (p.equals(permission)) {
                    retValue = true;
                }
            }
        }

        return retValue;
    }
}
