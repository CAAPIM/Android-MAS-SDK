/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASSessionUnlockCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.auth.MASAuthenticationProvider;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.foundation.auth.MASProximityLogin;
import com.ca.mas.foundation.auth.MASProximityLoginBLE;
import com.ca.mas.foundation.auth.MASProximityLoginBLECentralListener;
import com.ca.mas.foundation.auth.MASProximityLoginNFC;
import com.ca.mas.foundation.auth.MASProximityLoginQRCode;

public class MASSessionUnlockActivity extends AppCompatActivity {
    private static final String TAG = MASSessionUnlockActivity.class.getCanonicalName();
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String PROVIDER = "PROVIDER";
    private long mRequestId;
    private Context mContext;
    private Activity mActivity;
    private Button mFingerprintButton;
    private Button mCredentialsButton;
    private EditText mUsernameEditText;
    private EditText mPasswordEditText;
    private TextInputLayout mUsernameLayout;
    private TextInputLayout mPasswordLayout;
    private MASAuthenticationProvider mProvider;
    private MASProximityLogin mQRCode;
    private MASProximityLogin mNFC;
    private MASProximityLogin mBLE;
    private final static int FINGERPRINT_REQUEST_CODE = 1000;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        MASUser currentUser = MASUser.getCurrentUser();
//        if (currentUser != null && currentUser.isSessionLocked()) {
//            inflateUserLoginScreen();
//        } else {
        setContentView(R.layout.activity_session_unlock_login);

        mContext = this;
        mActivity = this;
        mFingerprintButton = (Button) findViewById(R.id.buttonFingerprintSignIn);
        mCredentialsButton = (Button) findViewById(R.id.buttonStandardSignIn);

        mFingerprintButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                unlock();
            }
        });
        mCredentialsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inflateUserLoginScreen();
            }
        });
//        }
    }

    private void unlock() {
        MASUser currentUser = MASUser.getCurrentUser();
        if (currentUser != null) {
            currentUser.unlockSession(getUnlockCallback());
        }
    }

    private void inflateUserLoginScreen() {
        setContentView(R.layout.activity_login);

        mUsernameEditText = (TextInputEditText) findViewById(R.id.editTextUsername);
        mPasswordEditText = (TextInputEditText) findViewById(R.id.editTextPassword);
        mUsernameLayout = (TextInputLayout) findViewById(R.id.layoutUsername);
        mPasswordLayout = (TextInputLayout) findViewById(R.id.layoutPassword);

        Button button = (Button) findViewById(R.id.buttonLogin);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameEditText.getText().toString();
                String password = mPasswordEditText.getText().toString();

                MASUser.login(username, password, new MASCallback<MASUser>() {
                    @Override
                    public Handler getHandler() {
                        return new Handler(Looper.getMainLooper());
                    }

                    @Override
                    public void onSuccess(MASUser result) {
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mContext, "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Proximity Login component
        final LinearLayout qr = (LinearLayout) findViewById(R.id.layoutQRCode);

        MASAuthenticationProviders.getAuthenticationProviders(new MASCallback<MASAuthenticationProviders>() {
            @Override
            public void onSuccess(MASAuthenticationProviders providers) {
                // QR code Proximity Login
                mQRCode = generateQRCode(qr);
                boolean init = mQRCode.init(mActivity, mRequestId, providers);
                if (init) {
                    qr.addView(mQRCode.render());
                    mQRCode.start();
                }

                // NFC Proximity Login
                mNFC = nfc();
                init = mNFC.init(mActivity, mRequestId, providers);
                if (init) {
                    mNFC.start();
                }

                // BLE Proximity Login
                mBLE = ble();
                init = mBLE.init(mActivity, mRequestId, providers);
                if (init) {
                    mBLE.start();
                }

                //Social Login
                GridLayout socialLoginLayout = (GridLayout) findViewById(R.id.socialLoginGridLayout);

                for (final MASAuthenticationProvider p : providers.getProviders()) {
                    if (!p.isProximityLogin()) {
                        ImageButton imageButton = new ImageButton(mContext);
                        GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                        params.setMargins(8, 8, 8, 8);
                        imageButton.setLayoutParams(params);
                        String identifier = p.getIdentifier();
                        imageButton.setBackgroundResource(getResources().
                                getIdentifier("drawable/" + identifier, null, getPackageName()));

                        switch (identifier) {
                            case "enterprise":
                                imageButton.setId(R.id.enterpriseIcon);
                                break;
                            case "facebook":
                                imageButton.setId(R.id.facebookIcon);
                                break;
                            case "google":
                                imageButton.setId(R.id.googleIcon);
                                break;
                            case "linkedin":
                                imageButton.setId(R.id.linkedinIcon);
                                break;
                            case "salesforce":
                                imageButton.setId(R.id.salesforceIcon);
                                break;
                        }

                        if (providers.getIdp().equals("all") || providers.getIdp().equalsIgnoreCase(p.getIdentifier())) {
                            imageButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    MASSocialLoginFragment.newInstance(mRequestId, p).show(getFragmentManager(), "logonDialog");
                                    finish();
                                }
                            });
                        } else {
                            imageButton.setClickable(false);
                            imageButton.setEnabled(false);
                        }
                        socialLoginLayout.addView(imageButton);
                    }
                }
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

    private MASProximityLoginQRCode generateQRCode(final LinearLayout qrContainer) {
        return new MASProximityLoginQRCode() {
            @Override
            public void onError(int errorCode, final String m, Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        qrContainer.removeAllViews();
                    }
                });
            }

            @Override
            protected void onAuthCodeReceived(String code) {
                super.onAuthCodeReceived(code);
                finish();
            }
        };
    }

    private MASProximityLoginNFC nfc() {
        return new MASProximityLoginNFC() {
            @Override
            public void onError(int errorCode, final String m, Exception e) {
                Log.i(TAG, m);
            }

            @Override
            protected void onAuthCodeReceived(String code) {
                super.onAuthCodeReceived(code);
                finish();
            }
        };
    }

    private MASProximityLoginBLE ble() {

        // Prepare callback to receive status update
        MASProximityLoginBLECentralListener callback = new MASProximityLoginBLECentralListener() {
            @Override
            public void onStatusUpdate(int state) {
                switch (state) {
                    case MASProximityLoginBLECentralListener.BLE_STATE_SCAN_STARTED:
                        Log.i(TAG, "Scan Started");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_SCAN_STOPPED:
                        Log.i(TAG, "Scan Stopped");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_DEVICE_DETECTED:
                        Log.i(TAG, "Device detected");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_CONNECTED:
                        Log.i(TAG, "Connected to Gatt Server");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_DISCONNECTED:
                        Log.i(TAG, "Disconnected from Gatt Server");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_SERVICE_DISCOVERED:
                        Log.i(TAG, "Service Discovered");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_CHARACTERISTIC_FOUND:
                        Log.i(TAG, "Characteristic Found");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_CHARACTERISTIC_WRITTEN:
                        Log.i(TAG, "Writing data to Characteristic... ");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_AUTH_SUCCEEDED:
                        Log.i(TAG, "Auth Succeeded");
                        break;
                    case MASProximityLoginBLECentralListener.BLE_STATE_AUTH_FAILED:
                        Log.i(TAG, "Auth Failed");
                        break;
                }
            }
        };

        return new MASProximityLoginBLE(callback) {
            @Override
            public void onError(int errorCode, final String m, Exception e) {
                Log.i(TAG, m);
            }

            @Override
            protected void onAuthCodeReceived(String code) {
                super.onAuthCodeReceived(code);
                finish();
            }
        };
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FINGERPRINT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                MASUser.getCurrentUser().unlockSession(getUnlockCallback());
            }
        }
    }

    private MASSessionUnlockCallback<Void> getUnlockCallback() {
        return new MASSessionUnlockCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                finish();
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            @TargetApi(23)
            public void onUserAuthenticationRequired() {
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
                Intent intent = keyguardManager.createConfirmDeviceCredentialIntent("", "Please provide your credentials.");
                if (intent != null) {
                    startActivityForResult(intent, FINGERPRINT_REQUEST_CODE);
                }
            }
        };
    }

    @Override
    protected void onDestroy() {
        stopProximity();
        super.onDestroy();
    }

    private void stopProximity() {
        if (mQRCode != null) {
            mQRCode.stop();
        }
        if (mNFC != null) {
            mNFC.stop();
        }
        if (mBLE != null) {
            mBLE.stop();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return keyCode == KeyEvent.KEYCODE_BACK || super.onKeyDown(keyCode, event);
    }
}
