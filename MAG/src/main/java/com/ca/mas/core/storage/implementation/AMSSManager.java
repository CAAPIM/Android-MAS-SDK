/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage.implementation;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.util.Log;


import com.ca.mas.core.storage.StorageException;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

/**
 * Manager class that takes care of account creation. This internally uses AccountManager
 * It also sets up the common password for all ELS instances of an app
 */
class AMSSManager {

    private static final String TAG = AMSSManager.class.getCanonicalName();

    private static final String ACCOUNT_NAME = "account.name";
    private static AMSSManager ourInstance;

    /**
     * The name of the Account that is created. This is default as "CA MAS"
     */
    private static final String CA_MAS = "CA MAS";


    private String mAccountName = CA_MAS;

    /**
     * The type of the Account that is created. This is retrieved from the @{link AUTHENTICATOR_FILE_NAME}
     */
    private String mAccountType;

    private Account mAccount;

    private Object mutex = new Object();

    /**
     * The application context
     */
    private Context mContext;

    public static AMSSManager getInstance(Context ctx) throws StorageException {
        if (ourInstance == null) {
            ourInstance = new AMSSManager(ctx.getApplicationContext());
        }
        return ourInstance;
    }


    private AMSSManager(Context ctx) throws StorageException {
        mContext = ctx;
        mAccountType = getAccountType();
        mAccountName = getAccountName();

        if (mAccountType == null) {
            Log.e(TAG, "Missing/malformed android.accounts.AccountAuthenticator xml file in application resource.");
            throw new StorageException("Missing/malformed android.accounts.AccountAuthenticator xml file in application resource.", null, StorageException.INVALID_INPUT);
        }

        if (!addAccount(mAccountName, mAccountType)) {
            throw new StorageException(StorageException.INSTANTIATION_ERROR);
        }
    }


    /**
     * Retrieve the Account Name from meta data
     * * <pre>
     *   &lt;meta-data android:name="account.name"
     *             android:resource="@string/acc_name" /&gt;
     * </pre>
     *
     * @return The Account name or "CA MAS" if account name is not defined.
     */
    private String getAccountName() {
        ComponentName myService = new ComponentName(mContext, AMSAuthenticatorService.class);
        try {
            Bundle data = mContext.getPackageManager().getServiceInfo(myService, PackageManager.GET_META_DATA).metaData;
            int resourceId = data.getInt(ACCOUNT_NAME);
            if (resourceId != 0) {
                return mContext.getResources().getString(resourceId);
            } else {
                return data.getString(ACCOUNT_NAME, CA_MAS);
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.d(TAG, String.format("Account name is not provided, use %S", CA_MAS));
        }
        return CA_MAS;
    }


    /**
     * Grabs the AccountType form the authenticator xml.
     *
     * @return The type of the Account or null if account type retrial failed for any reason
     */
    private String getAccountType() {

        ComponentName myService = new ComponentName(mContext, AMSAuthenticatorService.class);
        try {
            Bundle data = mContext.getPackageManager().getServiceInfo(myService, PackageManager.GET_META_DATA).metaData;
            int resourceId = data.getInt("android.accounts.AccountAuthenticator");
            XmlResourceParser xrp = mContext.getResources().getXml(resourceId);
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("account-authenticator")) {
                        return xrp.getAttributeValue("http://schemas.android.com/apk/res/android", "accountType");
                    }
                }
                xrp.next();
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Missing android.accounts.AccountAuthenticator metadata for " + AMSAuthenticatorService.class.getCanonicalName());
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, "Failed to retrieve account type", e);
        }
        return null;
    }

    /**
     * Adds an account to the device.
     *
     * @param accountName Account name
     * @param accountType Account type
     * @return True if successfully add an new account or able to access the existing account
     * @throws StorageException
     */
    private boolean addAccount(String accountName, String accountType) throws StorageException {
        AccountManager am = AccountManager.get(mContext);
        if (!isAccountPresent(accountName, accountType)) {
            Account account = new Account(accountName, accountType);
            return am.addAccountExplicitly(account, getPassword(), null);
        } else {
            Log.i(TAG, "Account already present");
            //Enforce Apps are using the same SharedID
            try {
                String password = am.getPassword(getAccount());
                if (password != null && !getPassword().equals(password)) {
                    throw new StorageException("Can't access Account", null, StorageException.INSTANTIATION_ERROR_UNAUTHORIZED);
                }
            } catch (Exception e) {
                throw new StorageException("Can't access Account", e, StorageException.INSTANTIATION_ERROR_UNAUTHORIZED);
            }
            return true;

        }
    }

    private boolean isAccountPresent(String accountName, String accountType) {
        AccountManager am = AccountManager.get(mContext);

        Account[] existingAccounts = am.getAccountsByType(accountType);
        if (existingAccounts.length == 0) {
            return false;
        } else {
            for (Account acc : existingAccounts) {
                if (accountName.equals(acc.name)) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * Gets the password with which the Account in Encrypted with
     *
     * @return A password that ensure Apps are defined with same SharedID Group
     */
    private String getPassword() {
        String packageName = mContext.getPackageName();
        String sharedUserId = null;
        try {
            sharedUserId = mContext.getPackageManager().getPackageInfo(packageName, 0).sharedUserId;
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        return sharedUserId != null ? sharedUserId : packageName;
    }


    Account getAccount() throws Exception {
        if (mAccount == null) {
            synchronized (mutex) {
                if (mAccount != null) {
                    return mAccount;
                }
                AccountManager am = AccountManager.get(mContext);
                for (Account a : am.getAccountsByType(mAccountType)) {
                    if (a.name.equals(mAccountName)) {
                        this.mAccount = a;
                        return this.mAccount;
                    }
                }
                Log.e(TAG, String.format("Account of type %s, name %s doesn't exist ", mAccountType, mAccountName));
                throw new Exception(String.format("Account of type %s, name %s doesn't exist ", mAccountType, mAccountName));
            }
        } else {
            return mAccount;
        }
    }


    public void reset() {
        ourInstance = null;
    }
}
