/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.massecureapisample.activity;

import android.annotation.TargetApi;
import android.app.KeyguardManager;
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
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ca.mas.core.util.KeyUtils;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConnectionListener;
import com.ca.mas.foundation.MASDevice;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.massecureapisample.R;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URI;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

@TargetApi(23)
public class EnrollmentActivity extends AppCompatActivity {
    private final String TAG = EnrollmentActivity.class.getSimpleName();
    private RelativeLayout mContainer;
    private Button mLoginButton;
    private Button mEndpointButton;
    private TextInputLayout mUsernameInputLayout;
    private TextInputEditText mUsernameEditText;
    private TextInputLayout mPasswordInputLayout;
    private TextInputEditText mPasswordEditText;
    private TextView mDataTextView;
    private int RESULT_CODE = 0x1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_secure_api_sample);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContainer = (RelativeLayout) findViewById(R.id.container);
        mUsernameEditText = (TextInputEditText) findViewById(R.id.edit_text_username);
        mUsernameInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout_username);
        mPasswordEditText = (TextInputEditText) findViewById(R.id.edit_text_password);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout_password);
        mLoginButton = (Button) findViewById(R.id.login_button);
        mEndpointButton = (Button) findViewById(R.id.endpoint_button);
        mDataTextView = (TextView) findViewById(R.id.data_text_view);

        mLoginButton.setOnClickListener(getLoginListener());

        MAS.start(this, true);
        MASDevice.getCurrentDevice().deregister(null);
        MAS.setConnectionListener(new MASConnectionListener() {
            @Override
            public void onObtained(HttpURLConnection connection) {
                ((HttpsURLConnection) connection).setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        return true;
                    }
                });
            }

            @Override
            public void onConnected(HttpURLConnection connection) {
                Map<String, List<String>> request = connection.getRequestProperties();
                StringBuilder sb = new StringBuilder();
                sb.append("{").append(connection.getURL()).append("}");
                for (String key : request.keySet()) {
                    List<String> values = request.get(key);
                    if (values != null && !values.isEmpty()) {
                        sb.append("Request method: ").append(connection.getRequestMethod()).append("\n")
                                .append("{\"").append(key).append("\":")
                                .append("\"").append(values.get(0)).append("\"}");
                    }
                }
                Log.d(MASConnectionListener.class.getCanonicalName(), sb.toString());
            }
        });
    }

    private View.OnClickListener getLoginListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameEditText.getEditableText().toString();
                Editable pwEditable = mPasswordEditText.getEditableText();
                int pwLength = pwEditable.length();
                char[] pw = new char[pwLength];
                mPasswordEditText.getEditableText().getChars(0, pwLength, pw, 0);
                MASUser.login(username, pw, getLoginCallback());
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
        mLoginButton.setText("Log out");
        mLoginButton.setOnClickListener(getLogoutListener());

        mUsernameInputLayout.setVisibility(View.GONE);
        mPasswordInputLayout.setVisibility(View.GONE);

        // Enroll
        try {
            String sampleKeyAlias = "SecureAPI";
            KeyUtils.generateRsaPrivateKey(this, 2048, sampleKeyAlias, true);
            PublicKey publicKey = KeyUtils.getRsaPublicKey(sampleKeyAlias);
            if (publicKey != null) {
                JWK publicKeyJWK = new RSAKey.Builder((RSAPublicKey) publicKey)
                        .keyID(UUID.randomUUID().toString())
                        .build();

                MASRequest request = new MASRequest.MASRequestBuilder(new URI("/connect/device/enroll"))
                        .post(MASRequestBody.jsonBody(new JSONObject(publicKeyJWK.toJSONString())))
                        .build();

                MAS.invoke(request, getEnrollCallback());
            }
        } catch (Exception e) {
            Snackbar.make(mContainer, e.toString(), Snackbar.LENGTH_LONG).show();
        }
    }

    private MASCallback<MASResponse<Void>> getEnrollCallback() {
        return new MASCallback<MASResponse<Void>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(MASResponse<Void> result) {
                mEndpointButton.setVisibility(View.VISIBLE);
                mEndpointButton.setOnClickListener(getEndpointClickListener());

                mDataTextView.setText("");
                Snackbar.make(mContainer, getString(R.string.enroll_success), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                // Enrollment failed
                Snackbar.make(mContainer, e.toString(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private View.OnClickListener getEndpointClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                invokeFingerprintEndpoint();
            }
        };
    }

    private void invokeFingerprintEndpoint() {
        // Successfully enrolled, now access the protected endpoint
        String protectedPath = "/protected/resource/fingerprint";
        Uri.Builder uriBuilder = new Uri.Builder().encodedPath(protectedPath);

        MASRequest.MASRequestBuilder requestBuilder = new MASRequest.MASRequestBuilder(uriBuilder.build());
        requestBuilder.post(MASRequestBody.stringBody("Test body")).notifyOnCancel();
        MASRequest request = requestBuilder.fingerprintSign().build();
        MAS.invoke(request, getEndpointCallback());
    }

    private MASCallback<MASResponse<JSONObject>> getEndpointCallback() {
        return new MASCallback<MASResponse<JSONObject>>() {
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

                    mDataTextView.setText(objectString);
                } catch (JSONException e) {
                    Log.e(TAG, e.getMessage());
                }

                Snackbar.make(mContainer, result.getResponseMessage(), Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
                if (e.getCause().getCause() instanceof JOSEException) {
                    KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                    Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("Confirm your pattern",
                            "Please provide your credentials.");
                    if (intent != null) {
                        startActivityForResult(intent, RESULT_CODE);
                    }
                }
            }
        };
    }

    private void onLogout() {
        mLoginButton.setText("Log in");
        mLoginButton.setOnClickListener(getLoginListener());
        mDataTextView.setText(R.string.protected_info);

        mUsernameInputLayout.setVisibility(View.VISIBLE);
        mPasswordInputLayout.setVisibility(View.VISIBLE);
        mEndpointButton.setVisibility(View.GONE);

        mUsernameEditText.setText("");
        mPasswordEditText.setText("");
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_CODE) {
            invokeFingerprintEndpoint();
        }
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
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage());
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menu_standard, menu);
        return true;
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deregister:
                MASDevice.getCurrentDevice().deregister(null);
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
