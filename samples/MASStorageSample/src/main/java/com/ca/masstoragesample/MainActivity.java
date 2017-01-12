/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.masstoragesample;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthenticationListener;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.storage.MASSecureLocalStorage;
import com.ca.mas.storage.MASStorage;
import com.ca.mas.ui.MASLoginActivity;

public class MainActivity extends AppCompatActivity {

    private EditText title;
    private EditText content;
    private Button save;
    private Button open;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);
        save = (Button) findViewById(R.id.save);
        open = (Button) findViewById(R.id.open);

        MAS.start(this);

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                Intent loginIntent = new Intent(context, MASLoginActivity.class);
                loginIntent.putExtra(MssoIntents.EXTRA_AUTH_PROVIDERS, providers);
                loginIntent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
                startActivity(loginIntent);
            }

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {
                //Ignore for now
            }
        });

        final MASStorage storage = new MASSecureLocalStorage();

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storage.save(
                        title.getText().toString(),
                        content.getText().toString(),
                        MASConstants.MAS_USER | MASConstants.MAS_APPLICATION,
                        new MASCallback<Void>() {

                            @Override
                            public void onSuccess(Void result) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        content.setText("");
                                    }
                                });
                            }

                            @Override
                            public void onError(Throwable e) {
                            }
                        });
            }
        });

        open.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                storage.findByKey(title.getText().toString(),
                        MASConstants.MAS_USER | MASConstants.MAS_APPLICATION,

                        new MASCallback() {
                            @Override
                            public Handler getHandler() {
                                return new Handler(Looper.getMainLooper());
                            }

                            @Override
                            public void onSuccess(Object result) {
                                content.setText((String) result);
                            }

                            @Override
                            public void onError(Throwable e) {
                            }
                        });
            }
        });


    }
}
