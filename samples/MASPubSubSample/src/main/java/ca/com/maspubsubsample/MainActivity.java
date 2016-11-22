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

    EditText editTextUri, editTextKeepAlive, editTextUsername, editTextPassword, editTextClientId,
            editTextWillTopic, editTextWillMessage;
    QosSpinner qosSpinner;
    AppCompatCheckBox checkBoxRetain, checkBoxCleanSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextUri = (EditText) findViewById(R.id.activity_main_edit_text_host);
        editTextClientId = (EditText) findViewById(R.id.activity_main_edit_text_client_id);
        editTextKeepAlive = (EditText) findViewById(R.id.activity_main_edit_text_keep_alive);
        editTextUsername = (EditText) findViewById(R.id.activity_main_edit_text_username);
        editTextPassword = (EditText) findViewById(R.id.activity_main_edit_text_password);
        editTextWillTopic = (EditText) findViewById(R.id.activity_main_edit_text_will_topic);
        editTextWillMessage = (EditText) findViewById(R.id.activity_main_edit_text_will_message);
        checkBoxCleanSession = (AppCompatCheckBox) findViewById(R.id.activity_main_check_box_clean_session);
        checkBoxRetain = (AppCompatCheckBox) findViewById(R.id.activity_main_check_box_will_retain);
        qosSpinner = (QosSpinner) findViewById(R.id.activity_main_spinner_qos);


        MAS.start(this, true);
    }

    public void onClickMag(View v){
        MASConnectaManager.getInstance().connect(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Log.i(TAG, "Connected to MAG");
                startPubSubActivity();
            }

            @Override
            public void onError(Throwable e) {
                Log.d(TAG, "Failed to connect: " + e.getMessage());
                showConnectionErrorMessage(e);
            }
        });
    }

    public void onClickPublicBroker(View v){
        String host = editTextUri.getText().toString();
        Integer keepAlive = Integer.parseInt((editTextKeepAlive).getText().toString());
        boolean cleanSession = checkBoxCleanSession.isChecked();
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        String clientId = editTextClientId.getText().toString();
        String willTopic = editTextWillTopic.getText().toString();
        String willMessage = editTextWillMessage.getText().toString();
        Integer willQos = qosSpinner.getSelectedQos();
        boolean willRetained = checkBoxRetain.isChecked();

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
                showConnectionErrorMessage(e);
            }
        });
    }

    public void onClickEnterDetails(View v){
        editTextUri.setText("tcp://broker.hivemq.com:1883");
    }

    private void startPubSubActivity(){
        Intent i = new Intent(MainActivity.this, PubSubActivity.class);
        startActivity(i);
        finish();
    }

    private void showConnectionErrorMessage(Throwable e){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Failed to connect")
                .setMessage(e.getMessage())
                .show();

    }
}
