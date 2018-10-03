package com.ca.mas.foundation;

import android.content.Context;
import android.content.SharedPreferences;

import com.ca.mas.core.conf.ConfigurationManager;
import com.ca.mas.core.token.JWTRS256Validator;

public class JWKPreLoadListener implements MASLifecycleListener {

    @Override
    public void onStarted() {

        if (JWTRS256Validator.getJwks() == null) {
            SharedPreferences prefs = MAS.getContext().getSharedPreferences(JWTRS256Validator.JWT_KEY_SET_FILE, Context.MODE_PRIVATE);
            String keySet = prefs.getString(ConfigurationManager.getInstance().getConnectedGateway().getHost(), null);

            if (keySet == null) {

                JWTRS256Validator.loadJWKS(null);

            } else {

                JWTRS256Validator.setJwks(keySet);
            }
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
