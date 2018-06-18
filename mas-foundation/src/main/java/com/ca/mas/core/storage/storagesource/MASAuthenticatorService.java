/*
 * Copyright (c) 2017 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.core.storage.storagesource;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class MASAuthenticatorService extends Service {

    @Override
    public IBinder onBind(Intent intent) {
        MASAuthenticator authenticator = new MASAuthenticator(this);
        return authenticator.getIBinder();
    }
}
