/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.singlesignonsample.otp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.mas.core.MobileSso;
import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.auth.otp.OtpUtil;
import com.ca.mas.core.auth.otp.model.OtpResponseHeaders;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.sample.singlesignonsample.ExampleActivity;
import com.ca.mas.sample.singlesignonsample.R;
import com.ca.mas.ui.listener.MASErrorMessageListener;
import com.ca.mas.ui.listener.MASFragmentAttachDismissListener;

import org.json.JSONObject;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Example to show a OTP  protected data.
 */
public class OtpDisplayDataActivity extends FragmentActivity implements View.OnClickListener, MASFragmentAttachDismissListener, MASErrorMessageListener {

    private static final String TAG = "OtpDisplayDataActivity";

    private URI otpProductListDownloadUri = null;
    Activity activity;

    Button btnGetOTPProtectedData, btnHomeScreen;
    TextView tvOtpProtectedData;

    public static final String OTP_PROTECTED_URL = "/otpProtected";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otp_protected_data);
        initUI();
        disableButtons();
        getProtectedData();

    }

    @Override
    protected void onResume() {
        super.onResume();
        enableButtons();
    }

    private void gotoHomeScreen() {
        disableButtons();
        Intent intent = new Intent(this, ExampleActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("classFrom", OtpDisplayDataActivity.class.toString());
        intent.setPackage(getBaseContext().getPackageName());
        startActivity(intent);
        finish();
    }

    private void getProtectedData() {
        disableButtons();
        activity = this;
        MobileSso mobileSso = MobileSsoFactory.getInstance(this);
        otpProductListDownloadUri = mobileSso.getURI(mobileSso.getPrefix() + OTP_PROTECTED_URL);

        final MASRequest request = new MASRequest.MASRequestBuilder(otpProductListDownloadUri).build();

        disableButtons();


        MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(final MASResponse<JSONObject> result) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvOtpProtectedData.setText(OtpUtil.toPrettyFormat(result.getBody().getContent().toString()));
                    }
                });
                enableButtons();
            }

            @Override
            public void onError(Throwable e) {
                if (e instanceof MAGError) {

                    Map otpHeaders = ((TargetApiException) e.getCause()).getResponse().getHeaders();
                    List<String> errorCodeList = (List<String>) otpHeaders.get(OtpConstants.X_CA_ERR);
                    OtpResponseHeaders.X_CA_ERROR errorCode = OtpResponseHeaders.X_CA_ERROR.UNKNOWN;
                    if (errorCodeList != null && errorCodeList.size() > 0) {
                        String xCaError = errorCodeList.get(0);
                        errorCode = OtpUtil.convertOtpErrorCodeToEnum(xCaError);
                    }
                    if (OtpResponseHeaders.X_CA_ERROR.OTP_MAX_RETRY_EXCEEDED.equals(errorCode)) {
                        tvOtpProtectedData.setText(getResources().getString(R.string.otpMaxLimit));
                        return;
                    } else if (errorCode.equals(OtpResponseHeaders.X_CA_ERROR.OTP_INVALID)
                            ) {
                        tvOtpProtectedData.setText(getResources().getString(R.string.otpAuthFailed));
                        return;
                    } else if (errorCode.equals(OtpResponseHeaders.X_CA_ERROR.SUSPENDED)) {
                        tvOtpProtectedData.setText(getResources().getString(R.string.otpUserSuspended));
                    } else if (errorCode.equals(OtpResponseHeaders.X_CA_ERROR.EXPIRED)) {
                        tvOtpProtectedData.setText(getResources().getString(R.string.otpExpired));
                    } else if (errorCode.equals(OtpResponseHeaders.X_CA_ERROR.INVALID_USER_INPUT)) {
                        tvOtpProtectedData.setText(getResources().getString(R.string.otpUnknownError));
                    } else {
                        String errorMsg = getResources().getString(R.string.otpUnknownError);
                        tvOtpProtectedData.setText(errorMsg);
                    }
                } else {
                    String errorMsg = getResources().getString(R.string.otpUnknownError);
                    tvOtpProtectedData.setText(errorMsg);
                }
                enableButtons();
            }
        });
    }

    private void disableButtons() {
        btnGetOTPProtectedData.setEnabled(false);
        btnHomeScreen.setEnabled(false);
    }

    private void enableButtons() {
        btnGetOTPProtectedData.setEnabled(true);
        btnHomeScreen.setEnabled(true);
    }

    private void initUI() {
        btnGetOTPProtectedData = (Button) findViewById(R.id.btnGetOtpProtectedData);
        btnGetOTPProtectedData.setOnClickListener(this);
        btnHomeScreen = (Button) findViewById(R.id.btnhome);
        btnHomeScreen.setOnClickListener(this);

        tvOtpProtectedData = (TextView) findViewById(R.id.tvOtpProtectedData);

    }

    @Override
    public void onClick(View v) {

        tvOtpProtectedData.setText(R.string.processing);
        switch (v.getId()) {
            case R.id.btnGetOtpProtectedData:
                getProtectedData();
                break;
            case R.id.btnhome:
                gotoHomeScreen();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        gotoHomeScreen();

    }

    @Override
    public void onPause() {
        super.onPause();
        disableButtons();
    }

    public void showMessage(final String message, final int toastLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(OtpDisplayDataActivity.this, message, toastLength).show();
            }
        });
    }

    @Override
    public void handleDialogClose(final Activity activity, final boolean flagRequestProcessing) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(!flagRequestProcessing) {
                    ((OtpDisplayDataActivity) activity).enableButtons();
               }
            }
        });
    }

    @Override
    public void handleDialogOpen(final Activity activity) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((OtpDisplayDataActivity) activity).disableButtons();
            }
        });
    }

    @Override
    public void getErrorMessage(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tvOtpProtectedData.setText(message);
                enableButtons();
            }
    });
    }
}