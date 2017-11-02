/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.push;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.core.service.Provider;
import com.ca.mas.foundation.MASConstants;

import java.util.Observable;
import java.util.Observer;

public class MASPushContentProvider extends ContentProvider {
    public MASPushContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
        PushConfig config = new PushConfig(getContext());

        if (config.isRegisterOnStartUp()) {
            final int finalGrantType = config.getGrantType();
            Observer observer = new Observer() {
                @Override
                public void update(Observable observable, Object o) {
                    //Do not register push if token is refreshing and already been registered
                    if (!MASPush.getInstance().isTokenRefresh() && !MASPush.getInstance().isRegistered()) {
                        MASPush.getInstance().register(finalGrantType, null);
                    }
                }
            };
            EventDispatcher.STARTED.addObserver(observer);
        }
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
