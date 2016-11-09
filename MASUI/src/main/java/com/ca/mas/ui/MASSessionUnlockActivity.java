/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.ca.mas.foundation.MASSessionUnlockCallback;
import com.ca.mas.foundation.MASUser;

@TargetApi(23)
public class MASSessionUnlockActivity extends AppCompatActivity {

    public final static int SESSION_UNLOCK_CODE = 0x1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                launchKeyguardIntent();
            }
        }, getAuthenticationScreenDelay());

        View container = findViewById(R.id.container);
        container.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchKeyguardIntent();
            }
        });

        TextView emailTextView = (TextView) findViewById(R.id.text_user_email);
        MASUser currentUser = MASUser.getCurrentUser();
        if (emailTextView != null && currentUser != null) {
            emailTextView.setText(currentUser.getUserName());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == getFingerprintRequestCode()) {
            if (resultCode == RESULT_OK) {
                onAuthenticationSuccess();
            } else if (resultCode == RESULT_CANCELED) {
                onAuthenticationCancelled();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public MASSessionUnlockCallback<Void> getUnlockCallback() {
        return new MASSessionUnlockCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                setResult(getResultCode());
                finish();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            @TargetApi(23)
            public void onUserAuthenticationRequired() {
                launchKeyguardIntent();
            }
        };
    }

    private void launchKeyguardIntent() {
        KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        Intent intent = keyguardManager.createConfirmDeviceCredentialIntent(getAuthenticationTitle(),
                getAuthenticationDescription());
        if (intent != null) {
            startActivityForResult(intent, getFingerprintRequestCode());
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }

    /**
     * Layout ID of the screen to be shown before the system authentication screen.
     * @return
     */
    @LayoutRes
    protected int getLayoutId() {
        return R.layout.activity_session_unlock_login;
    }

    /**
     * Value for the authentication screen title.
     * @return
     */
    protected String getAuthenticationTitle() {
        return "Confirm your pattern";
    }

    /**
     * Value for the authentication screen description.
     * @return
     */
    protected String getAuthenticationDescription() {
        return "Please provide your credentials.";
    }

    /**
     * Called when a user successfully authenticates.
     */
    protected void onAuthenticationSuccess() {
        MASUser.getCurrentUser().unlockSession(getUnlockCallback());
    }

    /**
     * Called when a user cancels the authentication screen.
     */
    protected void onAuthenticationCancelled() {

    }

    /**
     * The delay between showing the splash image before the authentication screen appears.
     * @return
     */
    protected int getAuthenticationScreenDelay() {
        return 1000;
    }

    /**
     * The ID for retrieving fingerprint results.
     * @return
     */
    protected int getFingerprintRequestCode() {
        return SESSION_UNLOCK_CODE;
    }

    /**
     * The ID for startActivityForResult() results.
     * @return
     */
    protected int getResultCode() {
        return SESSION_UNLOCK_CODE;
    }
}
