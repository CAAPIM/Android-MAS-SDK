
/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.foundation.auth;

import android.app.Activity;

import com.ca.mas.core.MobileSsoFactory;
import com.ca.mas.core.auth.QRCodeRenderer;
import com.ca.mas.core.error.MAGError;
import com.ca.mas.core.http.MAGResponse;
import com.ca.mas.core.service.Provider;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASResultReceiver;
import com.ca.mas.foundation.notify.Callback;

/**
 * Proximity login with Quick Response (QR) Code.
 */
public class MASProximityLoginQRCode extends QRCodeRenderer implements MASProximityLogin {

    public static final int QRCODE_ERROR = QRCodeRenderer.QRCODE_ERROR;

    public static void authorize(String authenticateUrl, final MASCallback<Void> callback) {
        MobileSsoFactory.getInstance().authorize(authenticateUrl, new MASResultReceiver(null) {
            @Override
            public void onSuccess(MAGResponse response) {
                Callback.onSuccess(callback, null);
            }

            @Override
            public void onError(MAGError error) {
                Callback.onError(callback, error);
            }
        });
    }

    @Override
    public boolean init(Activity activity, long requestId, MASAuthenticationProviders providers) {
        this.requestId = requestId;
        for (MASAuthenticationProvider p: providers.getProviders()) {
            if (p.isProximityLogin()) {
                return super.init(activity,new Provider(p.getIdentifier(), p.getAuthenticationUrl(), p.getPollUrl(), null));
            }
        }
        return false;
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
