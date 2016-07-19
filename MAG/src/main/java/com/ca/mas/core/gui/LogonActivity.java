/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.gui;

import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.util.List;

/**
 * Activity that collects credentials from the user.
 * <p/>
 * This activity should ensure that it always sends an intent when it finishes (even with the back button)
 * so that initiators of the request(s) that triggered the logon activity are not left hanging.
 */
@Deprecated
public class LogonActivity extends AbstractLogonActivity {

    private static final String USERNAME_HINT = "username";
    private static final String PASSWORD_HINT = "password";
    private static final String LOGON_TEXT = "Log On";
    private static final String CANCEL_TEXT = "Cancel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(buildLayout());

        final EditText passwordField = (EditText) findViewById(ID.PASSWORD.ordinal());
        final EditText usernameField = (EditText) findViewById(ID.USERNAME.ordinal());
        final Button logonButton = (Button) findViewById(ID.LOGON.ordinal());
        final Button cancelButton = (Button) findViewById(ID.CANCEL.ordinal());

        usernameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                logonButton.setEnabled(s != null && s.toString().trim().length() > 1);
            }
        });
        logonButton.setEnabled(usernameField.getText().toString().trim().length() > 1);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendCancelIntent();

                setResult(RESULT_CANCELED);
                finish();
            }
        });

        logonButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String username = usernameField.getText().toString();
                final String password = passwordField.getText().toString();

                sendCredentialsIntent(username, password);

                setResult(RESULT_OK);
                finish();
            }
        });

        if (savedInstanceState == null) {
            usernameField.requestFocus();
        }
    }


    private static enum ID {
        NONE,
        USERNAME,
        PASSWORD,
        LOGON,
        CANCEL
    }

    // Convert DP to PX
    private int dp(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private View buildLayout() {
        final LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ll.setLayoutParams(llp);

        EditText usernameField = new EditText(this);
        usernameField.setId(ID.USERNAME.ordinal());
        usernameField.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        usernameField.setEms(8);
        usernameField.setHint(USERNAME_HINT);
        usernameField.setCursorVisible(true);
        ll.addView(usernameField);

        EditText passwordField = new EditText(this);
        passwordField.setId(ID.PASSWORD.ordinal());
        passwordField.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48)));
        passwordField.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        passwordField.setEms(8);
        passwordField.setHint(PASSWORD_HINT);
        usernameField.setFocusableInTouchMode(true);
        ll.addView(passwordField);

        LinearLayout hl = new LinearLayout(this);
        hl.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams hlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dp(48));
        hl.setLayoutParams(hlp);
        hl.setWeightSum(1.0f);

        Button cancelButton = new Button(this);
        cancelButton.setId(ID.CANCEL.ordinal());
        cancelButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.5f));
        cancelButton.setText(CANCEL_TEXT);
        hl.addView(cancelButton);

        Button logonButton = new Button(this);
        logonButton.setId(ID.LOGON.ordinal());
        logonButton.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT, 0.5f));
        logonButton.setText(LOGON_TEXT);
        hl.addView(logonButton);

        if (!isEnterpriseLoginEnabled()) {
            usernameField.setEnabled(false);
            passwordField.setEnabled(false);
            logonButton.setEnabled(false);
        }

        ll.addView(hl);

        //Retrieve the social Login providers.
        List<View> providers = getProviders();

        LinearLayout qrCode = new LinearLayout(LogonActivity.this);
        qrCode.setOrientation(LinearLayout.HORIZONTAL);
        qrCode.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        qrCode.setGravity(Gravity.CENTER_HORIZONTAL);

        if (!providers.isEmpty()) {
            GridLayout gridLayout = new GridLayout(LogonActivity.this);
            ll.addView(gridLayout);
            ll.addView(qrCode);

            for (final View provider : providers) {
                if (provider instanceof ImageButton) {
                    gridLayout.addView(provider);
                } else {
                    qrCode.addView(provider);
                }
            }
        }
        return ll;
    }
}

