package com.ca.mas.foundation;

import android.content.Context;
import android.content.SharedPreferences;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.token.JWTRS256Validator;

public class JWKPreLoadListener implements MASLifecycleListener {

    @Override
    public void onStarted() {

        JWTRS256Validator jwtrs256Validator  = new JWTRS256Validator();
        jwtrs256Validator.loadJWKS(null);
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
