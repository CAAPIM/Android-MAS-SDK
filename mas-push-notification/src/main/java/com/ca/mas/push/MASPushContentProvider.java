/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.push;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.ca.mas.core.EventDispatcher;

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
        //Bind the device to the registration token
        EventDispatcher.REGISTERED.addObserver(MASPush.PUSH_BINDING_OBSERVER);
        //Bind the user to the registration token
        EventDispatcher.AFTER_LOGIN.addObserver(MASPush.PUSH_BINDING_OBSERVER);

        //For SSO Scenario, the device has been registered and user already been login.
        //Trigger push registration after it started
        EventDispatcher.STARTED.addObserver(MASPush.STARTED_OBSERVER);

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
