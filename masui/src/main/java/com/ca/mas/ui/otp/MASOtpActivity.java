/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui.otp;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.ui.R;
import com.ca.mas.ui.listener.MASFragmentAttachDismissListener;

/**
 * A sample dialog to select OTP delivery channels, send an OTP over these channels,
 * and verify its contents over the gateway.
 */
public class MASOtpActivity extends AppCompatActivity implements MASFragmentAttachDismissListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_masotp);

        Intent intent = getIntent();
        if (intent != null) {
            MASOtpAuthenticationHandler handler = intent.getParcelableExtra(MssoIntents.EXTRA_OTP_HANDLER);
            MASOtpDialogFragment.newInstance(handler).show(this.getFragmentManager(), "OTPDialog");
        }
    }

    @Override
    public void handleDialogClose(Activity activity, boolean flagRequestProcessing) {
        finish();
    }

    @Override
    public void handleDialogOpen(Activity activity) {
    }
}
