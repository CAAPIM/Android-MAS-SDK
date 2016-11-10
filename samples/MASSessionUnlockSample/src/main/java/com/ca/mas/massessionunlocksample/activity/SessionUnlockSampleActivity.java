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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.massessionunlocksample.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class SessionUnlockSampleActivity extends AppCompatActivity {
    private final String TAG = SessionUnlockSampleActivity.class.getSimpleName();
    private Context mContext;
    private RelativeLayout mContainer;
    private Button mLoginButton;
    private TextInputLayout mUsernameInputLayout;
    private TextInputEditText mUsernameEditText;
    private TextInputLayout mPasswordInputLayout;
    private TextInputEditText mPasswordEditText;
    private Switch mLockSwitch;
    private TextView mProtectedContent;
    private int RESULT_CODE = 0x1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Prevents screenshotting of content in Recents
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_session_unlock_sample);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContext = this;
        mContainer = (RelativeLayout) findViewById(R.id.container);
        mUsernameEditText = (TextInputEditText) findViewById(R.id.edit_text_username);
        mUsernameInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout_username);
        mPasswordEditText = (TextInputEditText) findViewById(R.id.edit_text_password);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout_password);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mLockSwitch = (Switch) findViewById(R.id.checkbox_lock);
        mProtectedContent = (TextView) findViewById(R.id.data_text_view);

        mLoginButton.setOnClickListener(getLoginListener());

        MAS.start(this, true);
    }

    private View.OnClickListener getLoginListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameEditText.getEditableText().toString();
                String password = mPasswordEditText.getEditableText().toString();

                MASUser.login(username, password, getLoginCallback());
            }
        };
    }

    private View.OnClickListener getLogoutListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MASUser currentUser = MASUser.getCurrentUser();
                currentUser.logout(getLogoutCallback());
            }
        };
    }

    private MASCallback<MASUser> getLoginCallback() {
        return new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onLogin();
                    }
                });

                invokeApi();
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(mContainer, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private void onLogin() {
        mLockSwitch.setVisibility(View.VISIBLE);
        mLoginButton.setText("Log out");
        mLoginButton.setOnClickListener(getLogoutListener());

        mUsernameInputLayout.setVisibility(View.GONE);
        mPasswordInputLayout.setVisibility(View.GONE);
    }

    private void onLogout() {
        mLockSwitch.setVisibility(View.GONE);
        mLoginButton.setText("Log in");
        mLoginButton.setOnClickListener(getLoginListener());
        mProtectedContent.setText(R.string.protected_info);

        mUsernameInputLayout.setVisibility(View.VISIBLE);
        mPasswordInputLayout.setVisibility(View.VISIBLE);
    }

    private MASCallback<Void> getLogoutCallback() {
        return new MASCallback<Void>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(Void result) {
                onLogout();
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(mContainer, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private void invokeApi() {
        String path = "/protected/resource/products";
        Uri.Builder uriBuilder = new Uri.Builder().encodedPath(path);
        uriBuilder.appendQueryParameter("operation", "listProducts");
        uriBuilder.appendQueryParameter("pName2", "pValue2");

        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(uriBuilder.build());
        requestBuilder.header("hName1", "hValue1");
        requestBuilder.header("hName2", "hValue2");
        MASRequest request = requestBuilder.get().build();

        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                try {
                    List<String> objects = parseProductListJson(result.getBody().getContent());
                    String objectString = "";
                    int size = objects.size();
                    for (int i = 0; i < size; i++) {
                        objectString += objects.get(i);
                        if (i != size - 1) {
                            objectString += "\n";
                        }
                    }

                    mProtectedContent.setText(objectString);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE) {
            Snackbar.make(mContainer, "Session unlocked.", Snackbar.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        if (mLockSwitch.isChecked()) {
            MASUser currentUser = MASUser.getCurrentUser();
            if (currentUser != null) {
                currentUser.lockSession(null);
            }
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MASUser currentUser = MASUser.getCurrentUser();
        if (currentUser != null && currentUser.isSessionLocked()) {
            launchLockActivity();
        } else if (currentUser != null && currentUser.isAuthenticated()) {
            onLogin();
            invokeApi();
        }
    }

    private void launchLockActivity() {
        Intent i = new Intent("MASUI.intent.action.SessionUnlock");
        startActivityForResult(i, RESULT_CODE);
    }

    private static List<String> parseProductListJson(JSONObject json) throws JSONException {
        try {
            List<String> objects = new ArrayList<>();
            JSONArray items = json.getJSONArray("products");
            for (int i = 0; i < items.length(); i++) {
                JSONObject item = (JSONObject) items.get(i);
                Integer id = (Integer) item.get("id");
                String name = (String) item.get("name");
                String price = (String) item.get("price");
                objects.add(id + ": " + name + ", $" + price);
            }
            return objects;
        } catch (ClassCastException e) {
            throw (JSONException) new JSONException("Response JSON was not in the expected format").initCause(e);
        }
    }
}
