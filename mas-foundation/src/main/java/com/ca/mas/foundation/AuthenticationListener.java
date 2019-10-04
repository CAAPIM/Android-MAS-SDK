/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.foundation;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ca.mas.core.MobileSsoListener;
import com.ca.mas.core.service.AuthenticationProvider;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.foundation.auth.MASAuthenticationProviders;

import java.lang.reflect.Constructor;

import static com.ca.mas.foundation.MAS.DEBUG;
import static com.ca.mas.foundation.MAS.TAG;

class AuthenticationListener implements MobileSsoListener {
    private Context mAppContext;

    AuthenticationListener(Context context) {
        mAppContext = context;
    }

    @Override
    public void onAuthenticateRequest(long requestId, final AuthenticationProvider provider) {

        if (MAS.getAuthenticationListener() == null) {
            //Use MASUI component
            if (MAS.isBrowserBasedAuthenticationEnabled()) {
                MASAuthorizationRequest authReq = new MASAuthorizationRequest.MASAuthorizationRequestBuilder().buildDefault();
                MASAuthorizationRequestHandler handler = getAuthorizationRequestHandler();
                if (handler != null) {
                    MASUser.login(authReq, handler);
                    return;
                }
            }

            Class<Activity> loginActivity = getLoginActivity();
            if (loginActivity != null) {
                if (mAppContext != null) {
                    Intent intent = new Intent(mAppContext, loginActivity);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(MssoIntents.EXTRA_REQUEST_ID, requestId);
                    intent.putExtra(MssoIntents.EXTRA_AUTH_PROVIDERS, new MASAuthenticationProviders(provider));
                    mAppContext.startActivity(intent);
                }
            } else {
                if (DEBUG)
                    Log.w(TAG, MASAuthenticationListener.class.getSimpleName() + " is required for user authentication.");
            }
        } else {
            MAS.getAuthenticationListener().onAuthenticateRequest(MAS.getCurrentActivity(), requestId, new MASAuthenticationProviders(provider));
        }
    }

    @Override
    public void onOtpAuthenticationRequest(MASOtpAuthenticationHandler otpAuthenticationHandler) {
        // Deprecated
    }

    /**
     * Return the MASLoginActivity from MASUI components if MASUI library is included in the classpath.
     *
     * @return A LoginActivity to capture the user credentials or null if error.
     */
    private Class<Activity> getLoginActivity() {

        try {
            return (Class<Activity>) Class.forName("com.ca.mas.ui.MASLoginActivity");
        } catch (Exception e) {
            return null;
        }
    }

    private MASAuthorizationRequestHandler getAuthorizationRequestHandler() {

        try {
            Class<MASAuthorizationRequestHandler> c = (Class<MASAuthorizationRequestHandler>) Class.forName("com.ca.mas.ui.MASAppAuthAuthorizationRequestHandler");
            Constructor constructor = c.getConstructor(Context.class/*, Intent.class, Intent.class*/);

            return (MASAuthorizationRequestHandler) constructor.newInstance(MAS.getCurrentActivity());

        } catch (Exception e) {
            return null;
        }
    }

}
