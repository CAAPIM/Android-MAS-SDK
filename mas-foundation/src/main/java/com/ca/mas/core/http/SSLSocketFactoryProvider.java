package com.ca.mas.core.http;

import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASSecurityConfiguration;

import java.util.Observable;
import java.util.Observer;

import javax.net.ssl.SSLSocketFactory;

//TODO MultiServer new implementation
class SSLSocketFactoryProvider {

    private static SSLSocketFactoryProvider instance = new SSLSocketFactoryProvider();

    private SSLSocketFactoryProvider() {

        MASConfiguration.SECURITY_CONFIGURATION_CHANGED.addObserver(
                new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        String host = (String) arg;
                        //update cache;
                        //rebuild the SSLSocketFactory is required
                    }
                }
        );
        MASConfiguration.SECURITY_CONFIGURATION_RESET.addObserver(
                new Observer() {
                    @Override
                    public void update(Observable o, Object arg) {
                        //clear the cache
                    }
                }
        );
    }

    public static SSLSocketFactoryProvider getInstance() {
        return instance;
    }

    public SSLSocketFactory get(String hostname) {
        //If not found from cache, create one and put to cache
        //return null or exception?
        return null;
    }

    public SSLSocketFactory createSSLSocketFactory(MASSecurityConfiguration configuration) {
        return null;
    }

    public SSLSocketFactory createSSLSocketFactory(String hostname) {
        return null;
    }
}

