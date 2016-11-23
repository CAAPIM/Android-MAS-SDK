/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.auth.NFCRenderer;
import com.ca.mas.core.auth.NfcResultReceiver;
import com.ca.mas.core.service.MssoIntents;
import com.ca.mas.core.service.Provider;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.notify.Callback;

/**
 * Proximity login with Near Field Communication (NFC).
 */
public class MASProximityLoginNFC extends NFCRenderer implements MASProximityLogin {

    /**
     * Error when NFC is not available
     */
    public static final int NFC_ERR = NFCRenderer.NFC_ERR;
    /**
     * Error when unable to start Bluetooth Service
     */
    public static final int BLUETOOTH_ERR = NFCRenderer.BLUETOOTH_ERR;
    /**
     * Error when failed to start Bluetooth server
     */
    public static final int BLUETOOTH_CONN_ERR = NFCRenderer.BLUETOOTH_CONN_ERR;


    public static void authorize(String authenticateUrl, final MASCallback<Void> callback) {
        MobileSsoFactory.getInstance().authorize(authenticateUrl, new NfcResultReceiver(null) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                super.onReceiveResult(resultCode, resultData);
                if (resultCode != MssoIntents.RESULT_CODE_SUCCESS) {
                    Callback.onError(callback, new MASException("Proximity Login Failed"));
                } else {
                    Callback.onSuccess(callback, null);
                }

            }
        });
    }

    @Override
    public boolean init(Activity activity, long requestId, MASAuthenticationProviders providers) {
        this.requestId = requestId;
        for (MASAuthenticationProvider p : providers.getProviders()) {
            if (p.isProximityLogin()) {
                return super.init(activity, new Provider(p.getIdentifier(), p.getAuthenticationUrl(), p.getPollUrl(), null));
            }
        }
        return false;
    }

    @Override
    public View render() {
        return super.render();
    }

    @Override
    public void start() {
        super.onRenderCompleted();
    }

    @Override
    public void stop() {
        super.close();
    }

    @Override
    public void onError(int errorCode, String m, Exception e) {
    }


}
