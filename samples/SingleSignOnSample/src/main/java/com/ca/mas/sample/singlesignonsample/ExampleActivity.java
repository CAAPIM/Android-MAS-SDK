/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.sample.singlesignonsample;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.error.TargetApiException;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthenticationListener;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;
import com.ca.mas.ui.MASAppAuthAuthorizationRequestHandler;
import com.ca.mas.foundation.MASAuthorizationRequest;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASConnectionListener;
import com.ca.mas.foundation.MASDevice;
import com.ca.mas.foundation.MASRequest;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.auth.MASProximityLoginBLE;
import com.ca.mas.foundation.auth.MASProximityLoginBLEPeripheralListener;
import com.ca.mas.foundation.auth.MASProximityLoginBLEUserConsentHandler;
import com.ca.mas.foundation.auth.MASProximityLoginNFC;
import com.ca.mas.foundation.auth.MASProximityLoginQRCode;
import com.ca.mas.ui.MASEnterpriseBrowserFragment;
import com.ca.mas.ui.MASFinishActivity;
import com.ca.mas.ui.MASLoginActivity;
import com.ca.mas.ui.MASOAuthRedirectActivity;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


import static android.media.CamcorderProfile.get;

public class ExampleActivity extends AppCompatActivity {
    private static final String TAG = "ExampleA";

    private static final int MENU_GROUP_LOGOUT = 66;
    private static final int MENU_ITEM_LOG_OUT = 3;
    private static final int MENU_ITEM_REMOVE_DEVICE_REGISTRATION = 4;
    private static final int MENU_ITEM_DESTROY_TOKEN_STORE = 2;


    ListView itemList;
    ProgressBar progressBar;
    TextView tvOtpProtectedData;
    Activity context;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;

        MAS.setConnectionListener(new MASConnectionListener() {
            @Override
            public void onObtained(HttpURLConnection connection) {
            }

            @Override
            public void onConnected(HttpURLConnection connection) {
                Map<String, List<String>> request = connection.getRequestProperties();
                StringBuilder sb = new StringBuilder();
                sb.append("{").append(connection.getURL()).append("}");
                for (String key : request.keySet()) {
                    List<String> values = request.get(key);
                    if (values != null && !values.isEmpty()) {
                        sb.append("{\"").append(key).append("\":");
                        sb.append("\"").append(values.get(0)).append("\"}");
                    }
                }
                Log.d(TAG, sb.toString());
            }
        });

        try {
            MAS.enableWebLogin();
            MAS.start(this, true);
            setContentView(R.layout.main);
            /*MAS.setAuthenticationListener(new MASAuthenticationListener() {
                @Override
                public void onAuthenticateRequest(Context mAppContext, long requestId, final MASAuthenticationProviders providers) {
                    Class<MASLoginActivity> loginActivity = MASLoginActivity.class;
                    if (mAppContext != null) {
                        Intent intent = new Intent(mAppContext, loginActivity);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
                        //intent.putExtra(MssoIntents.EXTRA_AUTH_PROVIDERS, new MASAuthenticationProviders(providers));
                        mAppContext.startActivity(intent);
                    }
                }

                @Override
                public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {
                }

                @Override
                public void onStepUpAuthenticateRequest(Context context) {

                }
            });*/
        } catch (MASException e) {
            e.printStackTrace();
            showMessage(e.getMessage(),Toast.LENGTH_LONG);
            return;
        }


        tvOtpProtectedData = (TextView) findViewById(R.id.tvOtpProtectedData);
        itemList = (ListView) findViewById(R.id.itemList);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final Button listButton = (Button) findViewById(R.id.listItemsButton);
        listButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearItem();
                //Android M Permission
                checkPermission();

                final MASRequest request = new MASRequest.MASRequestBuilder(getProductListDownloadUri()).build();

                MAS.invoke(request, new MASCallback<MASResponse<JSONObject>>() {
                    @Override
                    public Handler getHandler() {
                        return new Handler(Looper.getMainLooper());
                    }

                    @Override
                    public void onSuccess(MASResponse<JSONObject> result) {
                        setDownloadedJson(result.getBody().getContent());
                        try {
                            List<Object> objects = parseProductListJson(result.getBody().getContent());
                            itemList.setAdapter(new ArrayAdapter<Object>(ExampleActivity.this, R.layout.listitem, objects));

                        } catch (JSONException e) {
                            showMessage("Error: " + e.getMessage(), Toast.LENGTH_LONG);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        if (e.getCause() instanceof TargetApiException) {
                            showMessage(new String(((TargetApiException) e.getCause()).getResponse()
                                    .getBody().getRawContent()), Toast.LENGTH_SHORT);
                        } else {
                            showMessage("Error: " + e.getMessage(), Toast.LENGTH_LONG);
                        }
                    }
                });
            }
        });

        final Button otpProtectedlistButton = (Button) findViewById(R.id.listOtpProtectedItemsButton);
        otpProtectedlistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OtpConstants.ACTION_DISPLAY_OTP_PROTECTED_DATA);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.setPackage(getBaseContext().getPackageName());
                startActivity(intent);
            }
        });

        final Button logOutButton = (Button) findViewById(R.id.logOutButton);
        logOutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doServerLogout();
            }
        });
        registerForContextMenu(logOutButton);
    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},
                        0);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        0);
            }
        }
    }

    private void clearItem() {
        ArrayAdapter a = ((ArrayAdapter) itemList.getAdapter());
        if (a != null) {
            a.clear();
            a.notifyDataSetChanged();
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.add(MENU_GROUP_LOGOUT, MENU_ITEM_LOG_OUT, Menu.NONE, "Log Out");
        menu.add(MENU_GROUP_LOGOUT, MENU_ITEM_REMOVE_DEVICE_REGISTRATION, Menu.NONE, "Unregister Device");
        menu.add(MENU_GROUP_LOGOUT, MENU_ITEM_DESTROY_TOKEN_STORE, Menu.NONE, "Destroy Token Store");
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (66 != item.getGroupId())
            return false;
        switch (item.getItemId()) {
            case MENU_ITEM_DESTROY_TOKEN_STORE:
                MASDevice.getCurrentDevice().resetLocally();
                showMessage("Device Registration Destroyed (client only)", Toast.LENGTH_SHORT);
                return true;
            case MENU_ITEM_LOG_OUT:
                doServerLogout();
                return true;
            case MENU_ITEM_REMOVE_DEVICE_REGISTRATION:
                doServerUnregisterDevice();
                return true;
        }
        return false;
    }

    // Log the user out of all client apps and notify the server to revoke tokens.
    private void doServerLogout() {
        clearItem();
        if (MASUser.getCurrentUser() != null) {
            MASUser.getCurrentUser().logout(new MASCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    showMessage("Successful Logout", Toast.LENGTH_SHORT);
                }

                @Override
                public void onError(Throwable e) {
                    showMessage("Fail Logout", Toast.LENGTH_SHORT);
                }
            });
        }
    }

    // Tell the token server to un-register this device, without affecting the client-side token caches in any way.
    private void doServerUnregisterDevice() {
        MASDevice.getCurrentDevice().deregister(new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showMessage("Server Registration Removed for This Device", Toast.LENGTH_SHORT);
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.getMessage(), e);
                showMessage(e.getMessage(), Toast.LENGTH_SHORT);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MAS.processPendingRequests();

        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(getIntent().getAction())) {
            processIntent(getIntent());
            return;
        }

        Bundle bundle = getIntent().getExtras();

        if (MASUser.getCurrentUser() != null) {
            MASDevice.getCurrentDevice().startAsBluetoothPeripheral(new MASProximityLoginBLEPeripheralListener() {
                @Override
                public void onStatusUpdate(int state) {
                    switch (state) {
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_CONNECTED:
                            Log.d(TAG, "BLE Client Connected");
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_DISCONNECTED:
                            Log.d(TAG, "BLE Client Disconnected");
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_STARTED:
                            Log.d(TAG, "BLE peripheral mode started");
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_STOPPED:
                            Log.d(TAG, "BLE peripheral mode stopped");
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_STATE_SESSION_AUTHORIZED:
                            Log.d(TAG, "BLE session authorized");
                            break;
                    }
                }

                @Override
                public void onError(int errorCode) {
                    String message = null;
                    switch (errorCode) {
                        case MASProximityLoginBLEPeripheralListener.BLE_ERROR_ADVERTISE_FAILED:
                            message = "Advertise failed";
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_ERROR_AUTH_FAILED:
                            message = "Auth failed";
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_ERROR_CENTRAL_UNSUBSCRIBED:
                            message = "Central UnSubscribed";
                            break;
                        case MASProximityLoginBLEPeripheralListener.BLE_ERROR_PERIPHERAL_MODE_NOT_SUPPORTED:
                            message = "Peripheral mode not supported";
                            break;
                        case MASProximityLoginBLE.BLE_ERROR_DISABLED:
                            message = "Bluetooth Disabled";
                            break;
                        case MASProximityLoginBLE.BLE_ERROR_INVALID_UUID:
                            message = "Invalid UUID";
                            break;
                        case MASProximityLoginBLE.BLE_ERROR_NOT_SUPPORTED:
                            message = "Bluetooth not supported";
                            break;
                        case MASProximityLoginBLE.BLE_ERROR_SESSION_SHARING_NOT_SUPPORTED:
                            message = "Session sharing not supported";
                            break;
                        default:
                            message = Integer.toString(errorCode);

                    }
                    showMessage("BLE Error:" + message, Toast.LENGTH_SHORT);
                }

                @Override
                public void onConsentRequested(Context context, final String deviceName, final MASProximityLoginBLEUserConsentHandler handler) {
                    ExampleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            AlertDialog.Builder builder = new AlertDialog.Builder(ExampleActivity.this);
                            builder.setMessage("Do you want to grant session to " + deviceName + "?").
                                    setPositiveButton("Grant", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            handler.proceed();
                                        }
                                    }).setNegativeButton("Reject", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    handler.cancel();
                                }
                            }).show();
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        MASDevice.getCurrentDevice().stopAsBluetoothPeripheral();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void showMessage(final String message, final int toastLength) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ExampleActivity.this, message, toastLength).show();
            }
        });
    }

    public void setDownloadedJson(JSONObject json) {

    }

    private static List<Object> parseProductListJson(JSONObject json) throws JSONException {
        try {
            List<Object> objects = new ArrayList<Object>();
            JSONArray items = json.getJSONArray("products");
            for (int i = 0; i < items.length(); ++i) {
                JSONObject item = (JSONObject) items.get(i);
                Integer id = (Integer) item.get("id");
                String name = (String) item.get("name");
                objects.add(new Pair<Integer, String>(id, name) {
                    @Override
                    public String toString() {
                        return first + "  " + second;
                    }
                });
            }
            return objects;
        } catch (ClassCastException e) {
            throw (JSONException) new JSONException("Response JSON was not in the expected format").initCause(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.entBrowser:
                startEnterpriseBrowser();
                return true;
            case R.id.scanQRCode:
                IntentIntegrator intentIntegrator = new IntentIntegrator(this);
                intentIntegrator.initiateScan();
                break;
            case R.id.primaryGateway:
                MAS.start(this, true);
                setTitle(MASConfiguration.getCurrentConfiguration().getGatewayHostName());
                break;
            case R.id.secondaryGateway:
                MAS.start(this, getConfig("msso_secondary_config.json"));
                setTitle(MASConfiguration.getCurrentConfiguration().getGatewayHostName());
                break;

            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    private void startEnterpriseBrowser() {
        MASEnterpriseBrowserFragment enterpriseBrowser = MASEnterpriseBrowserFragment.newInstance();
        enterpriseBrowser.show(getFragmentManager(), "EnterpriseBrowser");
    }

    private URI getProductListDownloadUri() {
        try {
            return new URI("/protected/resource/products?operation=listProducts");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Process remote login through QRCode
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        //Got the QR Code, perform the remote login request.
        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String r = scanResult.getContents();
            if (r != null) {
                MASProximityLoginQRCode.authorize(r, new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showMessage(e.getMessage(), Toast.LENGTH_LONG);
                    }
                });
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent);
    }

    /**
     * Process remote login through NFC
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    void processIntent(Intent intent) {
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES);
        // only one message sent during the beam
        NdefMessage msg = (NdefMessage) rawMsgs[0];

        // record 0 contains the MIME type, record 1 is the AAR, if present
        String authRequest = new String(msg.getRecords()[0].getPayload());

        //Authorize session request
        MASProximityLoginNFC.authorize(authRequest, new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                showMessage("NFC Proximity Login Success", Toast.LENGTH_LONG);
            }

            @Override
            public void onError(Throwable e) {
                showMessage(e.getMessage(), Toast.LENGTH_LONG);
            }
        });
    }

    private JSONObject getConfig(String filename) {
        InputStream is = null;
        StringBuilder jsonConfig = new StringBuilder();

        try {
            is = this.getAssets().open(filename);
            BufferedReader in =
                    new BufferedReader(new InputStreamReader(is, "UTF-8"));
            String str;
            while ((str = in.readLine()) != null) {
                jsonConfig.append(str);
            }
            return new JSONObject(jsonConfig.toString());
        } catch (IOException | JSONException e) {
            throw new IllegalArgumentException("Unable to read Json Configuration file: " + filename, e);
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    //Ignore
                }
            }
        }
    }
}

