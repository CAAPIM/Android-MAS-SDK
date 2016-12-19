/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package ca.com.maspubsubsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.ca.mas.connecta.client.MASConnectOptions;
import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASConnectionListener;

import java.net.HttpURLConnection;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String INTENT_EXTRA_PUBLIC_BROKER = "ca.com.maspubsubsample.MainActivity.INTENT_EXTRA_PUBLIC_BROKER";
    public static final String INTENT_EXTRA_HOST = "ca.com.maspubsubsample.MainActivity.INTENT_EXTRA_HOST";

    TextInputEditText editTextUri, editTextKeepAlive, editTextUsername, editTextPassword, editTextClientId,
            editTextWillTopic, editTextWillMessage;
    SelectQosView selectQosView;
    AppCompatCheckBox checkBoxRetain, checkBoxCleanSession;
    View mainView;
    ProgressBar progressBar;

    private boolean publicBroker;
    private String host;
    private EmptyFieldTextWatcher textWatcher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainView = findViewById(R.id.activity_main);
        progressBar = (ProgressBar) findViewById(R.id.activity_main_progress_bar);
        editTextUri = (TextInputEditText) findViewById(R.id.activity_main_edit_text_host);
        editTextClientId = (TextInputEditText) findViewById(R.id.activity_main_edit_text_client_id);
        editTextKeepAlive = (TextInputEditText) findViewById(R.id.activity_main_edit_text_keep_alive);
        editTextUsername = (TextInputEditText) findViewById(R.id.activity_main_edit_text_username);
        editTextPassword = (TextInputEditText) findViewById(R.id.activity_main_edit_text_password);
        editTextWillTopic = (TextInputEditText) findViewById(R.id.activity_main_edit_text_will_topic);
        editTextWillMessage = (TextInputEditText) findViewById(R.id.activity_main_edit_text_will_message);
        checkBoxCleanSession = (AppCompatCheckBox) findViewById(R.id.activity_main_check_box_clean_session);
        checkBoxRetain = (AppCompatCheckBox) findViewById(R.id.activity_main_check_box_will_retain);
        selectQosView = (SelectQosView) findViewById(R.id.activity_main_select_qos);

        Button buttonPublicBroker = (Button) findViewById(R.id.activity_main_button_public_broker);
        textWatcher = new EmptyFieldTextWatcher(new View[]{buttonPublicBroker}, new EditText[]{editTextUri});
        MAS.start(this, true);
        MAS.setConnectionListener(new MASConnectionListener() {
            @Override
            public void onObtained(HttpURLConnection connection) {
                connection.setReadTimeout(5000);
            }

            @Override
            public void onConnected(HttpURLConnection connection) {

            }
        });

        Button buttonMag = (Button) findViewById(R.id.activity_main_button_connect_mag);
        buttonMag.requestFocus();
    }

    public void onClickMag(View v) {
        showProgress();
        publicBroker = false;
        host = MASConfiguration.getCurrentConfiguration().getGatewayUrl().toString();
        MASConnectaManager.getInstance().connect(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.i(TAG, "Connected to MAG");
                startPubSubActivity();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "Failed to connect: " + e.getMessage());
                hideProgress();
                showConnectionErrorMessage(e);
            }
        });
    }

    public void onClickPublicBroker(View v) {
        showProgress();
        publicBroker = true;
        host = editTextUri.getText().toString();
        Integer keepAlive = Integer.parseInt((editTextKeepAlive).getText().toString());
        boolean cleanSession = checkBoxCleanSession.isChecked();
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        String clientId = editTextClientId.getText().toString();
        String willTopic = editTextWillTopic.getText().toString();
        String willMessage = editTextWillMessage.getText().toString();
        Integer willQos = selectQosView.getSelectedQos();
        boolean willRetained = checkBoxRetain.isChecked();

        MASConnectOptions masConnectOptions = new MASConnectOptions();
        if (!TextUtils.isEmpty(username)) masConnectOptions.setUserName(username);
        if (!TextUtils.isEmpty(password)) masConnectOptions.setUserName(password);
        if (!TextUtils.isEmpty(clientId)) masConnectOptions.setUserName(clientId);
        if (!TextUtils.isEmpty(willTopic) && !TextUtils.isEmpty(willMessage))
            masConnectOptions.setWill(willTopic, willMessage.getBytes(), willQos, willRetained);
        masConnectOptions.setCleanSession(cleanSession);
        masConnectOptions.setKeepAliveInterval(keepAlive);

        try {
            masConnectOptions.setServerURIs(new String[]{host});
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
            showConnectionErrorMessage(e);
            hideProgress();
            return;
        }

        MASConnectaManager.getInstance().setConnectOptions(masConnectOptions);
        MASConnectaManager.getInstance().connect(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.i(TAG, "Connected to public broker");
                startPubSubActivity();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "Failed to connect: " + e.getMessage());
                hideProgress();
                showConnectionErrorMessage(e);
            }
        });
    }

    private void startPubSubActivity() {
        Intent i = new Intent(MainActivity.this, PubSubActivity.class);
        i.putExtra(INTENT_EXTRA_PUBLIC_BROKER, publicBroker);
        i.putExtra(INTENT_EXTRA_HOST, host);
        startActivity(i);
        finish();
    }

    private void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);

            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);
                mainView.setVisibility(View.VISIBLE);

            }
        });
    }

    private void showConnectionErrorMessage(Throwable e) {
        showConnectionErrorMessage(e.getMessage());
    }

    private void showConnectionErrorMessage(final String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Failed to connect")
                        .setMessage(errorMessage)
                        .show();

            }
        });
    }
}
