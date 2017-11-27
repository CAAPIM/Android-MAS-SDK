/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */


package com.ca.mas.foundation;

import android.net.Uri;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.store.StorageProvider;



public class MASWebLoginDefaults {

    private String clientId = StorageProvider.getInstance().getClientCredentialContainer().getClientId();
    private String display = "template";
    private Uri redirectUri;
    private String responseType = "code";
    private String scope;
    private String state = "state_test";

    public MASWebLoginDefaults (){
       super();
        scope = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.PROP_OAUTH_SCOPE);
        String redirectUrl = ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider().getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
        redirectUri = Uri.parse(redirectUrl);
    }


    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public Uri getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(Uri redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public String getScope() {
        if (MASDevice.getCurrentDevice().isRegistered()) {
            return scope.replace("msso_client_register ", "").replace("msso_register ", "");
        }
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
