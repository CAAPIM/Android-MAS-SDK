/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthorizationResponse;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;



/**
 * Default Handler activity class for OAuth redirects during Browser Based Login
 * After user has entered his credentials the OAuth redirect will invoke this class.
 * The MASAuthorizationResponse object is extracted from the intent data and used to login the user
 */
public class MASAppAuthRedirectHandlerActivity extends AppCompatActivity {
    private Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = this;

        Uri redirectUri = getIntent().getData();

        OAuthError error = getErrorFromUri(redirectUri);
        if (error != null ) {
            Toast.makeText(this, error.getError_description(), Toast.LENGTH_LONG).show();
            MAS.cancelAllRequests();
            finish();
            return;
        }
        if (redirectUri != null) {
            MASAuthorizationResponse response = MASAuthorizationResponse.fromUri(redirectUri);
            MASUser.login(response, getLoginCallback());
        } else {
            Toast.makeText(this, "No redirect URI detected.", Toast.LENGTH_LONG).show();
            finish();
        }

    }

    private MASCallback<MASUser> getLoginCallback() {
        return new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser result) {
                finish();
            }

            @Override
            public void onError(Throwable e) {
                Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_LONG).show();
                finish();
            }
        };
    }

    /**
     * Method to parse OAuth redirectUri for error details
     */
    private  static OAuthError getErrorFromUri (Uri redirectUri) {

        String error = redirectUri.getQueryParameter("error");
        if (null == error || "".equals(error)) {
            return null;
        }
        OAuthError response = new OAuthError();
        response.setError(error);
        response.setError_description(redirectUri.getQueryParameter("error_description"));
        response.setState(redirectUri.getQueryParameter("state"));
        response.setxCAError(redirectUri.getQueryParameter("x-ca-err"));
        return response ;
    }


    /**
     * Model class to hold OAuth error details
     */
    static class OAuthError {
        private String error;
        private String error_description;
        private String xCAError;
        private String state;

        public OAuthError()
        {
            super();
        }
        public String getError() {
            return error;
        }

        public void setError(String error) {
            this.error = error;
        }

        public String getError_description() {
            return error_description;
        }

        public void setError_description(String error_description) {
            this.error_description = error_description;
        }

        public String getxCAError() {
            return xCAError;
        }

        public void setxCAError(String xCAError) {
            this.xCAError = xCAError;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }
}

