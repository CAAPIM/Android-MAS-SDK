package com.ca.mas.foundation;

import com.ca.mas.core.token.JWTRS256Validator;



/**
 * Implements listeners for various MAS Life Cycles.
 */
public class JWKPreLoadListener implements MASLifecycleListener {

    /*
    *Called when the SDK is started. Caching of JWKS is done here.
    */
    @Override
    public void onStarted() {

        JWTRS256Validator jwtrs256Validator  = new JWTRS256Validator();
        jwtrs256Validator.loadJWKS(null);
    }

    /**
     * Called when the device gets registered.
     */
    @Override
    public void onDeviceRegistered() {

    }

    /**
     * Called when the User gets authenticated.
     */
    @Override
    public void onAuthenticated() {

    }

    /**
     * Called when the device gets de-registered.
     */
    @Override
    public void onDeRegistered() {

    }
}
