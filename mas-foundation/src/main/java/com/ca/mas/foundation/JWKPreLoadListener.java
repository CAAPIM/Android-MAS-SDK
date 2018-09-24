package com.ca.mas.foundation;

import android.util.Log;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.token.JwtRS256;

public class JWKPreLoadListener implements MASLifecycleListener {

    @Override
    public void onStarted() {

        Log.d("START", "onStarted");
         if(ConfigurationManager.getInstance().getJwks() == null){
             Log.d(MASLifecycleListener.class.getSimpleName(), "onStarted - no keys chached");
             JwtRS256.loadJWKS();
         }
    }

    @Override
    public void onDeviceRegistered() {

    }

    @Override
    public void onAuthenticated() {

    }

    @Override
    public void onDeRegistered() {

    }
}
