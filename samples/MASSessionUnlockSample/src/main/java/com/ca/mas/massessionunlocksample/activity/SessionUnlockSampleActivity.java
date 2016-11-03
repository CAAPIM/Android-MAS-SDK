/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.massessionunlocksample.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.massessionunlocksample.R;

public class SessionUnlockSampleActivity extends AppCompatActivity {
    private final String TAG = SessionUnlockSampleActivity.class.getSimpleName();
    private Context mContext;
    private FloatingActionButton mLockButton;
    private CheckBox mLockCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_session_unlock);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContext = this;
        mLockButton = (FloatingActionButton) findViewById(R.id.fab);
        mLockCheckBox = (CheckBox) findViewById(R.id.checkbox_lock);

        MAS.start(this, true);
        MASUser.login("username", "password", getUserCallback());
    }

    private MASCallback<MASUser> getUserCallback() {
        return new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                mLockButton.setOnClickListener(getLockButtonListener());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG + " getUserCallback()", e.toString());
            }
        };
    }

    private View.OnClickListener getLockButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MASUser.getCurrentUser().lockSession(getLockCallback());
            }
        };
    }

    private MASCallback<Void> getLockCallback() {
        return new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                if (mLockCheckBox.isChecked()) {
                    launchLockActivity();
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        MASUser currentUser = MASUser.getCurrentUser();
        if (currentUser != null && currentUser.isSessionLocked()) {
            launchLockActivity();
        }
    }

    private void launchLockActivity() {
        Intent i = new Intent("MASUI.intent.action.SessionUnlock");
        startActivity(i);
    }
}
