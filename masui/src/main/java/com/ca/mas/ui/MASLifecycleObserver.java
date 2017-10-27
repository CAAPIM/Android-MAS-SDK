/*
 *
 *  * Copyright (c) 2016 CA. All rights reserved.
 *  *
 *  * This software may be modified and distributed under the terms
 *  * of the MIT license.  See the LICENSE file for details.
 *  *
 *
 */

package com.ca.mas.ui;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASAuthenticationListener;
import com.ca.mas.foundation.MASAuthorizationRequest;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by mujmo02 on 20/10/17.
 */

public class MASLifecycleObserver {



 /*   static {

       MAS.setAuthenticationListener(new MASAuthenticationListener() {
           @Override
           public void onAuthenticateRequest(Context context, long requestId, MASAuthenticationProviders providers) {
               Intent postAuthIntent = new Intent(context, MASOAuthRedirectActivity.class);
               Intent authCanceledIntent = new Intent(context, MASFinishActivity.class);
               MASAppAuthAuthorizationRequestHandler handler = new MASAppAuthAuthorizationRequestHandler(context, postAuthIntent, authCanceledIntent);
               MASAuthorizationRequest authReq = new MASAuthorizationRequest.MASAuthorizationRequestBuilder().buildDefault();
               MASUser.login(authReq, handler);
           }

           @Override
           public void onOtpAuthenticateRequest(Context context, MASOtpAuthenticationHandler handler) {

           }
       });

    }*/
}
