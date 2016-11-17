package ca.com.maspubsubsample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.ca.mas.connecta.client.MASConnectOptions;
import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinnerQos = (AppCompatSpinner) findViewById(R.id.activity_main_spinner_qos);
        spinnerQos.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                getResources().getStringArray(R.array.qos_options)));

        MAS.start(this, true);
    }

    public void onClickMag(View v){
        MASConnectaManager.getInstance().connect(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.i(TAG, "Connected to MAG");

                Intent i = new Intent(MainActivity.this, PubSubActivity.class);
                startActivity(i);
                finish();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "Connecting to MAG failed: " + e.getMessage());

                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Failed to connect")
                        .setMessage(e.getMessage())
                        .show();
            }
        });
    }

    public void onClickPublicBroker(View v){
        String host = ((EditText) findViewById(R.id.activity_main_edit_text_host)).getText().toString();
        Integer keepAlive = Integer.parseInt(((EditText) findViewById(R.id.activity_main_edit_text_keep_alive)).getText().toString());
        boolean cleanSession = ((AppCompatCheckBox) findViewById(R.id.activity_main_check_box_clean_session)).isChecked();
        String username = ((EditText) findViewById(R.id.activity_main_edit_text_username)).getText().toString();
        String password = ((EditText) findViewById(R.id.activity_main_edit_text_password)).getText().toString();
        String clientId = ((EditText) findViewById(R.id.activity_main_edit_text_client_id)).getText().toString();
        String willTopic = ((EditText) findViewById(R.id.activity_main_edit_text_will_topic)).getText().toString();
        String willMessage = ((EditText) findViewById(R.id.activity_main_edit_text_will_message)).getText().toString();
        Integer willQos = Integer.parseInt(((AppCompatSpinner) findViewById(R.id.activity_main_spinner_qos)).getSelectedItem().toString());
        boolean willRetained = ((AppCompatCheckBox) findViewById(R.id.activity_main_check_box_will_retain)).isChecked();

        MASConnectOptions masConnectOptions = new MASConnectOptions();
        if( !TextUtils.isEmpty(username) ) masConnectOptions.setUserName(username);
        if( !TextUtils.isEmpty(password) ) masConnectOptions.setUserName(password);
        if( !TextUtils.isEmpty(clientId) ) masConnectOptions.setUserName(clientId);
        if( !TextUtils.isEmpty(willTopic) && !TextUtils.isEmpty(willMessage)) masConnectOptions.setWill(willTopic, willMessage.getBytes(), willQos, willRetained);
        masConnectOptions.setCleanSession(cleanSession);
        masConnectOptions.setKeepAliveInterval(keepAlive);

        try {
            masConnectOptions.setServerURIs(new String[]{host});
        } catch (Exception e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
