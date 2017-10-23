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

import com.ca.mas.core.store.StorageProvider;

/**
 * Created by mujmo02 on 13/10/17.
 */

public class MASAuthorizationRequest {

    private String clientId;
    private String display;
    private Uri redirectUri;
    private String responseType;
    private String scope;
    private String state;



    private MASAuthorizationRequest (MASAuthorizationRequestBuilder builder) {
        this.clientId = builder.clientId;
        this.display = builder.display;
        this.redirectUri = builder.redirectUri;
        this.responseType = builder.responseType;
        this.scope = builder.scope;
        this.state = builder.state;

    }
   /* private MASAuthorizationRequest () {
        super();
    }
*/
    public String getClientId() {
        return clientId;
    }

    public String getDisplay() {
        return display;
    }

    public Uri getRedirectUri() {
        return redirectUri;
    }

    public String getResponseType() {
        return responseType;
    }


    public String getScope() {
        return scope;
    }


    public String getState() {
        return state;
    }

    public static class MASAuthorizationRequestBuilder {
        private String clientId;
        private String display;
        private Uri redirectUri;
        private String responseType;
        private String scope;
        private String state;

        public MASAuthorizationRequestBuilder() {

        }

        public  MASAuthorizationRequest buildDefault() {
           // MASAuthorizationRequest request = new MASAuthorizationRequest();
            MASWebLoginDefaults defaults = new MASWebLoginDefaults();

            this.display = defaults.getDisplay();
            this.clientId = defaults.getClientId();
            this.responseType = defaults.getResponseType();
            this.redirectUri = defaults.getRedirectUri();
            this.scope = defaults.getScope();
            this.state = defaults.getState();


            return new MASAuthorizationRequest(this);

        }
        public MASAuthorizationRequest build () {
            return new MASAuthorizationRequest(this);
        }


        public Uri getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(Uri redirectUri) {
            this.redirectUri = redirectUri;
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
}
