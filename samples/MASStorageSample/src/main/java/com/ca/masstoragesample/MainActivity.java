/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.masstoragesample;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.storage.MASSecureLocalStorage;
import com.ca.mas.storage.MASStorage;

public class MainActivity extends AppCompatActivity {

    private EditText title;
    private EditText content;
    private Button save;
    private Button retrieve;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        title = (EditText) findViewById(R.id.title);
        content = (EditText) findViewById(R.id.content);
        save = (Button) findViewById(R.id.save);
        retrieve = (Button) findViewById(R.id.retrieve);

        MAS.start(this);

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

        retrieve.setOnClickListener(new View.OnClickListener() {
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
