/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.auth;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.ca.mas.core.client.ServerClient;
import com.ca.mas.foundation.MASAuthCredentialsAuthCode;
import com.ca.mas.foundation.MASAuthCredentials;
import com.ca.mas.core.http.MAGHttpClient;
import com.ca.mas.core.http.MAGRequest;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.http.MAGResponseBody;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.service.MssoService;
import com.ca.mas.core.service.Provider;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.core.store.TokenManager;

import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.URL;

public abstract class AuthRenderer {

    //Error when retrieving the Authorization Code
    public static final int AUTH_CODE_ERR = 1;

    protected Context context;
    protected Provider provider;
    protected long requestId;

    /**
     * Initialize the Renderer,
     *
     * @param context  The application context
     * @param provider The Authorize Provider returned from gateway
     * @return True if init success, False when failed to init the renderer, the renderer will not be
     * added to as One of the Touch less login.
     */
    public boolean init(Context context, Provider provider) {
        this.context = context;
        this.provider = provider;
        return true;

    }

    /**
     * Retrieve the ID of the Auth Provider, the auth provider should be one of the provider returned from the authorize
     * endpoint {@link com.ca.mas.core.MobileSsoConfig#PROP_TOKEN_URL_SUFFIX_AUTHORIZE}.
     *
     * @return Return the id of auth provider
     */
    public String getId() {
        return "qrcode";
    }

    /**
     * Render the view for the login, the view can be a image, button or null for no view.
     * The view will put to the login dialog as one of the login provider.
     *
     * @return The login view to represent the login action.
     */
    public abstract View render();

    /**
     * Perform action after rendering is completed.
     */
    public abstract void onRenderCompleted();

    /**
     * Notify when any error occur.
     *
     * @param code    Error code
     * @param message Error message
     * @param e       Exception
     */
    protected abstract void onError(int code, String message, Exception e);

    /**
     * Perform clean up for the renderer.
     */
    public abstract void close();


    /**
     * Proceed the authorization process and retrieve the authorized Authorization Code.
     */
    protected synchronized void proceed() {


        try {
            MAGRequest request = new MAGRequest.MAGRequestBuilder(new URL(provider.getPollUrl()))
                    .responseBody(MAGResponseBody.jsonBody())
                    .build();
            MAGHttpClient httpClient = new MAGHttpClient() {
                @Override
                protected void onConnectionObtained(HttpURLConnection connection) {
                    super.onConnectionObtained(connection);
                    TokenManager tokenManager = StorageProvider.getInstance().getTokenManager();
                    String magIdentifier = tokenManager.getMagIdentifier();
                    if (magIdentifier != null) {
                        connection.setRequestProperty(ServerClient.MAG_IDENTIFIER, magIdentifier);
                    }
                }
            };
            MAGResponse<JSONObject> response = httpClient.execute(request);
            if (HttpURLConnection.HTTP_OK == response.getResponseCode()) {
                if (response.getBody() != null) {
                    JSONObject json = response.getBody().getContent();
                    if (json != null) {
                        String code = json.getString("code");
                        String state = json.optString("state");
                        if (code != null && code.length() > 0) {
                            sendCredentialsIntent(new MASAuthCredentialsAuthCode(code, state));
                            onAuthCodeReceived(code);
                        }
                    }
                }
            } else {
                String msg = "Session Polling error.";
                if (response.getBody() != null) {
                    msg = new String(response.getBody().getRawContent());
                }
                onError(AUTH_CODE_ERR, msg, null);
            }
        } catch (Exception e) {
            onError(AUTH_CODE_ERR, e.getMessage(), e);
        }
    }

    /**
     * Once the authorization code is obtained, use this method to send intent to the Mobile SSO Module to proceed
     * the logon process.
     *
     * @param credentials The Credential retrieved by the social login platform.
     */
    public void sendCredentialsIntent(MASAuthCredentials credentials) {
        Intent intent = new Intent(MssoIntents.ACTION_CREDENTIALS_OBTAINED, null, context, MssoService.class);
        intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
        intent.putExtra(MssoIntents.EXTRA_CREDENTIALS, credentials);
        context.startService(intent);
    }

    /**
     * Notify the Authenticate Renderer that Authorization code has been retrieved.
     *
     * @param code Authorization Code
     */
    protected abstract void onAuthCodeReceived(String code);

}
