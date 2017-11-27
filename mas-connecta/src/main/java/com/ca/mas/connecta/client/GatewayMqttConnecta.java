/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.connecta.client;

import android.os.Handler;
import android.util.Log;

import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.http.SSLSocketFactoryProvider;
import com.ca.mas.core.store.StorageProvider;
import com.ca.mas.foundation.FoundationConsts;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASInvalidHostException;
import com.ca.mas.foundation.MASResponse;
import com.ca.mas.foundation.MASTokenRequest;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.notify.Callback;

import org.json.JSONObject;

import javax.net.ssl.SSLSocketFactory;

import static android.content.ContentValues.TAG;
import static com.ca.mas.foundation.MAS.DEBUG;

class GatewayMqttConnecta implements MqttConnecta {

    @Override
    public void init(final MASConnectOptions connectOptions, final MASCallback<Void> callback) {

        final MASCallback<MASResponse<JSONObject>> receiver = new MASCallback<MASResponse<JSONObject>>() {
            @Override
            public Handler getHandler() {
                return Callback.getHandler(callback);
            }

            @Override
            public void onSuccess(MASResponse<JSONObject> result) {
                JSONObject jobj = result.getBody().getContent();
                String oauthToken = jobj.optString("access_token");

                try {
                    SSLSocketFactory sslSocketFactory = SSLSocketFactoryProvider.getInstance().getPrimaryGatewaySocketFactory();
                    connectOptions.setSocketFactory(sslSocketFactory);
                } catch (MASInvalidHostException e) {
                    if (DEBUG)
                        Log.e(TAG, "Failed to retrieve the primary gateway SSLSocketFactory.");
                    Callback.onError(callback, e);
                    return;
                }
                String uname = MASUser.getCurrentUser().getUserName();
                connectOptions.setUserName(uname);
                connectOptions.setPassword(oauthToken.toCharArray());

                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(Throwable e) {
                Callback.onError(callback, e);
            }
        };

        if (MASUser.getCurrentUser() != null && MASUser.getCurrentUser().getUserName() != null) {
            MAS.invoke(new MASTokenRequest(), receiver);
        } else {
            MASUser.login(new MASCallback<MASUser>() {
                @Override
                public void onSuccess(MASUser result) {
                    MAS.invoke(new MASTokenRequest(), receiver);
                }

                @Override
                public void onError(Throwable e) {
                    Callback.onError(callback, e);
                }
            });
        }
    }

    @Override
    public String getServerUri() {
        return (ConnectaConsts.SSL_MESSAGING_SCHEME + FoundationConsts.COLON + FoundationConsts.FSLASH + FoundationConsts.FSLASH) +
                ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getTokenHost() +
                FoundationConsts.COLON + ConnectaConsts.SSL_MESSAGING_PORT;

    }

    @Override
    public String getClientId() {

        //<mag_identifier>::<client_id>::<SCIM userID>
        StringBuilder sb = new StringBuilder();
        sb.append(StorageProvider.getInstance().getTokenManager().getMagIdentifier())
                .append(ConnectaConsts.CLIENT_ID_SEP)
                .append(ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getClientId());

        if (MASUser.getCurrentUser() != null) {
            // If user is logged in, get username to put in clientId
            sb.append(ConnectaConsts.CLIENT_ID_SEP)
                    .append(MASUser.getCurrentUser().getUserName());
        }

        return sb.toString();
    }

}
