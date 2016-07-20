/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.auth.MASAuthenticationProvider;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.foundation.auth.MASProximityLogin;
import com.ca.mas.foundation.auth.MASProximityLoginBLE;
import com.ca.mas.foundation.auth.MASProximityLoginBLECentralListener;
import com.ca.mas.foundation.auth.MASProximityLoginNFC;
import com.ca.mas.foundation.auth.MASProximityLoginQRCode;

/**
 * Sample to show a login DialogFragment.
 * This sample enables QR code, NFC proximity, BLE proximity, and social login.
 */
public class MASLoginFragment extends DialogFragment {
    private static final String TAG = MASLoginFragment.class.getCanonicalName();
    private static final String REQUEST_ID = "requestID";
    private static final String PROVIDERS = "providers";
    private long requestID;
    private MASAuthenticationProviders providers;
    private MASProximityLogin qrcode;
    private MASProximityLogin nfc;
    private MASProximityLogin ble;
    private EditText mUsernameEdit;
    private EditText mPasswordEdit;

    /**
     * Use this factory method to create a new instance of this fragment.
     *
     * @return A new instance of a MASLogonFragment.
     */
    public static MASLoginFragment newInstance(long requestID, MASAuthenticationProviders providers) {
        MASLoginFragment fragment = new MASLoginFragment();
        Bundle args = new Bundle();
        args.putLong(REQUEST_ID, requestID);
        args.putParcelable(PROVIDERS, providers);
        fragment.setArguments(args);
        return fragment;
    }

    public MASLoginFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            requestID = getArguments().getLong(REQUEST_ID);
            providers = getArguments().getParcelable(PROVIDERS);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_login, container, false);
        Button button = (Button) view.findViewById(R.id.btnlogin);

        mUsernameEdit = (EditText) view.findViewById(R.id.etxtname);
        mPasswordEdit = (EditText) view.findViewById(R.id.etxtpass);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameEdit.getText().toString();
                String password = mPasswordEdit.getText().toString();

                MASUser.login(username, password, new MASCallback<MASUser>() {
                    @Override
                    public Handler getHandler() {
                        return new Handler(Looper.getMainLooper());
                    }

                    @Override
                    public void onSuccess(MASUser result) {
                        dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), "Login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        dismiss();
                    }
                });
            }
        });

        // Proximity Login component
        final LinearLayout qr = (LinearLayout) view.findViewById(R.id.qrcode);

        // QR code Proximity Login
        qrcode = qrCode(qr);
        boolean init = qrcode.init(getActivity(), requestID, providers);
        if (init) {
            qr.addView(qrcode.render());
            qrcode.start();
        }

        // NFC Proximity Login
        nfc = nfc();
        init = nfc.init(getActivity(), requestID, providers);
        if (init) {
            nfc.start();
        }

        // BLE Proximity Login
        ble = ble();
        init = ble.init(getActivity(), requestID, providers);
        if (init) {
            ble.start();
        }

        //Social Login
        GridLayout socialLoginLayout = (GridLayout) view.findViewById(R.id.socialLoginGridLayout);

        for (final MASAuthenticationProvider p : providers.getProviders()) {
            if (!p.isProximityLogin()) {
                ImageButton imageButton = new ImageButton(getActivity());
                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.setMargins(8, 8, 8, 8);
                imageButton.setLayoutParams(params);
                String identifier = p.getIdentifier();
                imageButton.setBackgroundResource(getActivity().getResources().
                        getIdentifier("drawable/" + identifier, null, getActivity().getPackageName()));

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
                            MASSocialLoginFragment.newInstance(requestID, p).show(getActivity().getFragmentManager(), "logonDialog");
                            dismiss();
                        }
                    });
                } else {
                    imageButton.setClickable(false);
                    imageButton.setEnabled(false);
                }
                socialLoginLayout.addView(imageButton);
            }
        }
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        if (dialog != null) {
            Window window = dialog.getWindow();
            if (window != null) {
                window.requestFeature(Window.FEATURE_NO_TITLE);
            }
        }

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        stopProximity();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        // Remove the request from the queue when cancelling the login
        MAS.cancelRequest(requestID);
    }

    private MASProximityLoginQRCode qrCode(final LinearLayout qrContainer) {
        return new MASProximityLoginQRCode() {
            @Override
            public void onError(int errorCode, final String m, Exception e) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        qrContainer.removeAllViews();
                    }
                });
            }

            @Override
            protected void onAuthCodeReceived(String code) {
                super.onAuthCodeReceived(code);
                dismiss();
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
                dismiss();
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
                dismiss();
            }
        };
    }

    private void stopProximity() {
        if (qrcode != null) {
            qrcode.stop();
        }
        if (nfc != null) {
            nfc.stop();
        }
        if (ble != null) {
            ble.stop();
        }
    }
}
