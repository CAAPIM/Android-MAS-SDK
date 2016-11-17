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
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.util.Log;

import com.ca.mas.core.storage.StorageException;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import static com.ca.mas.core.MAG.DEBUG;
import static com.ca.mas.core.MAG.TAG;

/**
 * Manager class that takes care of account creation. This internally uses AccountManager
 * It also sets up the common password for all ELS instances of an app
 */
public class AMSSManager {

    /**
     * The authenticator xml file name.
     */
    private static final String AUTHENTICATOR_FILE_NAME = "authenticator_ca_mas";
    /**
     * The type of the Account that is created. This is retrieved from the @{link AUTHENTICATOR_FILE_NAME}
     */
    private String mAccountType;

    /**
     * The name of the Account that is created. This is hard coded as "MAG"
     */
    private String mAccountName = "CA MAS";

    /**
     * The application context
     */
    private Context mContext;



    private static AMSSManager ourInstance;

    public static AMSSManager getInstance(Context ctx) throws StorageException{
        if(ourInstance==null){
            ourInstance = new AMSSManager(ctx);
        }
        return ourInstance;
    }

    private AMSSManager(Context ctx) throws StorageException{
        mContext = ctx;
        mAccountType = getAccountType(AUTHENTICATOR_FILE_NAME);
        if (mAccountType == null) {
            if (DEBUG) Log.e(TAG, String.format("Missing/malformed %s xml file in application resource.", AUTHENTICATOR_FILE_NAME));
            throw new StorageException(String.format("Missing/malformed %s xml file in application resource.", AUTHENTICATOR_FILE_NAME),null,StorageException.INVALID_INPUT);
        }

        if (!addAccount(mContext, mAccountName, mAccountType)) {
            throw new StorageException(StorageException.INSTANTIATION_ERROR);
        }
    }

    /**
     * Grabs the AccountType form the authenticator xml.
     *
     * @param fileName The name of the authenticator xml file
     * @return The type of the Account or null if account type retrial failed for any reason
     */
    private String getAccountType(String fileName) {


        String acc_type = null;

        try {

            int authenticator_id = mContext.getResources().getIdentifier(fileName, "xml", mContext.getPackageName());
            if (authenticator_id == 0) {
                if (DEBUG) Log.e(TAG, "authenticator_ca_mas file could not be found");
                return acc_type;
            }
            XmlResourceParser xrp = mContext.getResources().getXml(authenticator_id);
            while (xrp.getEventType() != XmlResourceParser.END_DOCUMENT) {
                if (xrp.getEventType() == XmlResourceParser.START_TAG) {
                    String s = xrp.getName();
                    if (s.equals("account-authenticator")) {
                        acc_type = xrp.getAttributeValue("http://schemas.android.com/apk/res/android", "accountType");
                        break;
                    }
                }
                xrp.next();
            }
        } catch (XmlPullParserException | IOException e) {
            if (DEBUG) Log.e(TAG,"getAccountType failed to read from " + fileName + ", reason: " + e);
        }
        return acc_type;
    }
    /**
     * Adds an account to the device.
     *
     * @param ctx
     * @param accountName Account name
     * @param accountType Account type
     * @return
     * @throws StorageException
     */
    private boolean addAccount(Context ctx, String accountName, String accountType)throws StorageException {
        AccountManager am = AccountManager.get(ctx);
        if (!isAccountPresent(ctx, accountName, accountType)) {
            Account account = new Account(accountName, accountType);
            boolean created = am.addAccountExplicitly(account, getPassword(ctx), null);
            return created;
        } else {
            if (DEBUG) Log.i(TAG, "Account already present");
            try {
                String password = am.getPassword(getAccount());
                if(password!=null && !getPassword(ctx).equals(password)){
                    throw new StorageException("Can't access Account",null,StorageException.INSTANTIATION_ERROR_UNAUTHORIZED);
                }
            } catch (Exception e) {
                throw new StorageException("Can't access Account",e,StorageException.INSTANTIATION_ERROR_UNAUTHORIZED);
            }
            return true;
        }
    }

    private boolean isAccountPresent(Context ctx, String accountName, String accountType) {
        AccountManager am = AccountManager.get(ctx);
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
     * @param ctx
     * @return
     */
    private String getPassword(Context ctx){
        String packageName = ctx.getApplicationContext().getPackageName();
        String sharedUserId = null;
        try {
            sharedUserId = ctx.getPackageManager().getPackageInfo(packageName,0).sharedUserId;
        } catch (PackageManager.NameNotFoundException e) {
        }
        return sharedUserId!=null?sharedUserId:packageName;
    }

    public Account getAccount() throws Exception{
        AccountManager am = AccountManager.get(mContext);
        try {
            return am.getAccountsByType(mAccountType)[0];
        } catch (Exception e) {
            if (DEBUG) Log.e(TAG, String.format("Account of type %s doesn't exist ", mAccountType));
            throw new Exception(String.format("Account of type %s doesn't exist ", mAccountType));
        }
    }


    public void reset(){
        ourInstance = null;
    }
}
