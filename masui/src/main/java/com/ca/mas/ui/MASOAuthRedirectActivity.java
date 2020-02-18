/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.mas.foundation.MASAuthorizationResponse;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;

import net.openid.appauth.AuthorizationException;

public class MASOAuthRedirectActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appauth_success);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_success);
        mContext = this;

        ImageView imageView = findViewById(R.id.checkmark);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        imageView.startAnimation(fadeIn);

        TextView tv = findViewById(R.id.successText);
        tv.startAnimation(fadeIn);

        ProgressBar pb = findViewById(R.id.progressBar);
        pb.animate();

        //If it was successful, AppAuth will return a state mismatch error at this point.
        //This is expected because the gateway will consume the initial state information.
        //The redirect URL will then be returned by gateway without the initial state information
        //which is why a mismatch is expected.
        AuthorizationException ex = AuthorizationException.fromIntent(getIntent());
        if (ex != null && ex.equals(AuthorizationException.AuthorizationRequestErrors.STATE_MISMATCH)) {
            Uri redirectUri = getIntent().getData();
            if (redirectUri != null) {
                MASAuthorizationResponse response = MASAuthorizationResponse.fromUri(redirectUri);
                MASUser.login(response, getLoginCallback());
            } else {
                Toast.makeText(this, "No redirect URI detected.", Toast.LENGTH_LONG).show();
                delayedFinish();
            }
        } else {
            if (ex != null) {
                Toast.makeText(this, ex.errorDescription, Toast.LENGTH_LONG).show();
                delayedFinish();
            }
        }
    }

    private MASCallback<MASUser> getLoginCallback() {
        return new MASCallback<MASUser>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASUser result) {
                delayedFinish();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                delayedFinish();
            }
        };
    }

    private void delayedFinish() {
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                finish();
            }
        }, 1250);
    }
}
