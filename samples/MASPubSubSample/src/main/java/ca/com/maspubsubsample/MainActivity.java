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
import android.widget.ScrollView;

import com.ca.mas.connecta.client.MASConnectOptions;
import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    public static final String INTENT_EXTRA_PUBLIC_BROKER = "ca.com.maspubsubsample.MainActivity.INTENT_EXTRA_PUBLIC_BROKER";
    public static final String INTENT_EXTRA_HOST = "ca.com.maspubsubsample.MainActivity.INTENT_EXTRA_HOST";

    TextInputEditText editTextUri, editTextKeepAlive, editTextUsername, editTextPassword, editTextClientId,
            editTextWillTopic, editTextWillMessage;
    SelectQosView selectQosView;
    AppCompatCheckBox checkBoxRetain, checkBoxCleanSession;

    private boolean publicBroker;
    private String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ScrollView scrollView = (ScrollView) findViewById(R.id.activity_main);
        scrollView.fullScroll(ScrollView.FOCUS_UP);

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

        MAS.start(this, true);
    }

    public void onClickMag(View v){
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
                showConnectionErrorMessage(e);
            }
        });
    }

    public void onClickPublicBroker(View v){
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
            showConnectionErrorMessage(e);
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
                showConnectionErrorMessage(e);
            }
        });
    }

    public void onClickEnterDetails(View v){
        editTextUri.setText("tcp://broker.hivemq.com:1883");
    }

    private void startPubSubActivity(){
        Intent i = new Intent(MainActivity.this, PubSubActivity.class);
        i.putExtra(INTENT_EXTRA_PUBLIC_BROKER, publicBroker);
        i.putExtra(INTENT_EXTRA_HOST, host);
        startActivity(i);
        finish();
    }

    private void showConnectionErrorMessage(Throwable e){
        showConnectionErrorMessage(e.getMessage());
    }

    private void showConnectionErrorMessage(String errorMessage){
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Failed to connect")
                .setMessage(errorMessage)
                .show();
    }
}
