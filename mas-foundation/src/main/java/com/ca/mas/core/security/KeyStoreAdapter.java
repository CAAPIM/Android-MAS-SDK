/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.security;

import android.util.Log;

import java.lang.reflect.Method;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

/**
 * Adapter that provides a common interface {@link KeyStore} for various
 * Android KeyStore implementations from API level 16 and up
 *
 * This class uses reflection to access the internal Android Keystore interface, this is not recommended to
 * use android internal interface, will consider to rewrite this component using
 * Android AccountManager.
 */
public class KeyStoreAdapter implements KeyStore {

    private static KeyStoreAdapter sInstance = null;

    private Object mAndroidKeyStore;
    private Method mContains;
    private Method mDelete;
    private Method mPut;
    private Method mGet;
    private Method mGetLastError;
    private Method mList;
    private Method mState;
    private Object[] mStateEnumConstants;//{ UNLOCKED, LOCKED, UNINITIALIZED }

    private KeyStoreAdapter() {
        try {
            Class<?> smClass = Class.forName("android.security.KeyStore");
            Method getInstance = smClass.getMethod("getInstance");

            mStateEnumConstants = Class.forName("android.security.KeyStore$State").getEnumConstants();
            mState = smClass.getMethod("state");

            mContains = smClass.getMethod("contains", String.class);
            mDelete = smClass.getMethod("delete", String.class);


            try {
                mPut = smClass.getMethod("put", String.class, byte[].class, int.class, int.class);
            } catch (NoSuchMethodException e) {
                //put in to support API level 16 & 17
                mPut = smClass.getMethod("put", String.class, byte[].class);
            }

            mGet = smClass.getMethod("get", String.class);
            mGetLastError = smClass.getMethod("getLastError");

            try {
                mList = smClass.getMethod("list", String.class);
            } catch (NoSuchMethodException e) {
                //To support pre API level 23
                mList = smClass.getMethod("saw", String.class);
            }

            mAndroidKeyStore = getInstance.invoke(null);
        } catch (Exception e) {
            if (DEBUG) Log.w(TAG,"Unable to create adapter for KeyStore access ",e);
            throw new RuntimeException(e);
        }
    }

    public synchronized static KeyStore getKeyStore() {
        if (sInstance == null) {
            sInstance = new KeyStoreAdapter();
        }
        return sInstance;
    }

    public boolean isUnlocked() {
        try {
            return mState.invoke(mAndroidKeyStore) == mStateEnumConstants[0];
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean contains(String key) {
        try {
            return (boolean) mContains.invoke(mAndroidKeyStore, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(String key) {
        try {
            return (boolean) mDelete.invoke(mAndroidKeyStore, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public boolean put(String key, byte[] value) {
        try {
            if(mPut.getParameterTypes().length!=2){
                return (boolean) mPut.invoke(mAndroidKeyStore, key, value, -1, 1);
            }else{
                return (boolean) mPut.invoke(mAndroidKeyStore, key, value);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] get(String key) {
        try {
            return (byte[]) mGet.invoke(mAndroidKeyStore, key);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public int getLastError() {
        try {
            return (int) mGetLastError.invoke(mAndroidKeyStore);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String[] saw(String prefix) {
        try {
            return (String[]) mList.invoke(mAndroidKeyStore, prefix);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
