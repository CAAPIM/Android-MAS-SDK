/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package ca.com.maspubsubsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.messaging.MASMessageException;
import com.ca.mas.messaging.MASMessage;

public class PubSubActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = PubSubActivity.class.getSimpleName();

    private MessagesFragment messagesFragment;
    private boolean publicBroker;
    View mainView;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_sub);
        Intent i = getIntent();
        if (i != null) {
            publicBroker = i.getBooleanExtra(MainActivity.INTENT_EXTRA_PUBLIC_BROKER, false);
            String host = i.getStringExtra(MainActivity.INTENT_EXTRA_HOST);
            TextView textViewHost = (TextView) findViewById(R.id.activity_pub_sub_text_view_host);
            textViewHost.setText(host);
        }
        mainView = findViewById(R.id.activity_pub_sub_main_view);
        progressBar = (ProgressBar) findViewById(R.id.activity_pub_sub_progress_bar);
        messagesFragment = (MessagesFragment) getSupportFragmentManager().findFragmentById(R.id.activity_pub_sub_fragment_messages);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getAction().equals(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED)) {
                    return;
                }

                try {
                    MASMessage message = MASMessage.newInstance(intent);
                    messagesFragment.onMessageReceived(message);
                } catch (MASMessageException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }, intentFilter);

        findViewById(R.id.activity_pub_sub_fab).setOnClickListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_pub_sub, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_disconnect:
                showProgress();
                MASConnectaManager.getInstance().disconnect(new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Intent i = new Intent(PubSubActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideProgress();
                        Util.showSnackbar("Failed to disconnect");
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    public boolean isPublicBroker() {
        return publicBroker;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.activity_pub_sub_fab:
                PublishDialogFragment publishDialogFragment = new PublishDialogFragment();
                publishDialogFragment.show(getSupportFragmentManager(), null);
                break;
        }
    }
}
