package com.ca.mas.foundation;

import android.util.Log;

import com.ca.mas.core.conf.ConfigurationManager;

public class JWKPreLoadListener implements MASLifecycleListener {

    @Override
    public void onStarted() {

        Log.d(MASLifecycleListener.class.getSimpleName(), "onStarted");
         if(ConfigurationManager.getInstance().getJwks() == null){
             Log.d(MASLifecycleListener.class.getSimpleName(), "onStarted - no keys chached");
             MAS.loadJWKS();
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
