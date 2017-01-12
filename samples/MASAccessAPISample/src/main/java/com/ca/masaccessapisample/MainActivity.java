/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.masaccessapisample;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthenticationListener;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConnectionListener;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASRequestBody;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.ui.MASLoginActivity;
import com.ca.mas.ui.otp.MASOtpDialogFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private static final int FINE_LOCATION_REQUEST = 1;
    private EditText stock;
    private EditText shares;
    private Button buy;
    private Button sell;
    private TextView status;
    private Button clear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        stock = (EditText) findViewById(R.id.stock);
        shares = (EditText) findViewById(R.id.shares);
        buy = (Button) findViewById(R.id.buy);
        sell = (Button) findViewById(R.id.sell);
        status = (TextView) findViewById(R.id.status);
        clear = (Button) findViewById(R.id.clear);

        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                status.setText("");
            }
        });

        int fineLocationCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (fineLocationCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_LOCATION_REQUEST);
        }

        MAS.start(this, true);

        MAS.setAuthenticationListener(new MASAuthenticationListener() {
            @Override
            public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
                Intent loginIntent = new Intent(context, MASLoginActivity.class);
                loginIntent.putExtra(MssoIntents.EXTRA_AUTH_PROVIDERS, providers);
                loginIntent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
                startActivity(loginIntent);}

            @Override
            public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {
                android.app.DialogFragment otpFragment = MASOtpDialogFragment.newInstance(handler);
                otpFragment.show(((Activity) context).getFragmentManager(), "OTPDialog");
            }
        });

        MAS.setConnectionListener(new MASConnectionListener() {
            @Override
            public void onObtained(HttpURLConnection connection) {
            }

            @Override
            public void onConnected(HttpURLConnection connection) {
                Map<String, List<String>> request = connection.getRequestProperties();
                StringBuilder sb = new StringBuilder();
                for (String key : request.keySet()) {
                    List<String> values = request.get(key);
                    if (values != null && !values.isEmpty()) {
                        sb.append("{\"").append(key).append("\":");
                        sb.append("\"").append(values.get(0)).append("\"}");
                    }
                }
                Log.d("AccessAPI", sb.toString());
            }
        });




        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Uri uri = new Uri.Builder().path("/trade").appendQueryParameter("requestCode", UUID.randomUUID().toString()).build();

                JSONObject body = new JSONObject();
                try {
                    body.put("stock", stock.getText().toString());
                    body.put("shares", shares.getText().toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                final MASRequest request = new MASRequest.MASRequestBuilder(uri)
                        .post(MASRequestBody.jsonBody(body))
                        .header("action", "Buy").build();


                MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {

                    @Override
                    public Handler getHandler() {
                        return new Handler(Looper.getMainLooper());
                    }

                    @Override
                    public void onSuccess(MASResponse<JSONObject> result) {
                        try {
                            status.setText(result.getBody().getContent().toString(4));
                        } catch (JSONException e) {
                            status.setText("Error: " + e);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        status.setText(e.getMessage());
                    }
                });
            }
        });


    }
}
