/*
 *
 *  * Copyright (c) 2016 CA. All rights reserved.
 *  *
 *  * This software may be modified and distributed under the terms
 *  * of the MIT license.  See the LICENSE file for details.
 *  *
 *
 */

package com.ca.mas.foundation;

import android.net.Uri;

import com.ca.mas.core.MobileSsoConfig;
import com.ca.mas.core.conf.Config;
import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.conf.DefaultConfiguration;
import com.ca.mas.core.context.MssoContext;
import com.ca.mas.core.store.StorageProvider;

/**
 * Created by mujmo02 on 22/10/17.
 */

public class MASWebLoginDefaults {

    private String clientId = StorageProvider.getInstance().getClientCredentialContainer().getClientId();
    private String display = "template";
    private Uri redirectUri = Uri.parse("camssoras://com.ca.ras");//   ConfigurationManager.getInstance().getConnectedGatewayConfigurationProvider()//MssoContext.newContext().getConfigurationProvider().getProperty(MobileSsoConfig.PROP_AUTHORIZE_REDIRECT_URI);
    private String responseType = "code";
    private String scope = "openid msso phone profile address email user_role no-id-token msso_client_register msso_register mas_messaging mas_storage mas_identity mas_identity_retrieve_users mas_identity_create_users mas_identity_update_users mas_identity_delete_users mas_identity_retrieve_groups mas_identity_create_groups mas_identity_update_groups mas_identity_delete_groups";//"scope_test openid msso phone profile address email msso_client_register msso_register mas_messaging mas_storage mas_identity mas_identity_retrieve_users mas_identity_create_users mas_identity_update_users mas_identity_delete_users mas_identity_retrieve_groups mas_identity_create_groups mas_identity_update_groups mas_identity_delete_groups";//MssoContext.newContext().getConfigurationProvider().getClientScope();
    private String state = "state_test";

    //private MssoContext mssoContext;
    public MASWebLoginDefaults (/*MssoContext mssoContext*/){
       super();

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
