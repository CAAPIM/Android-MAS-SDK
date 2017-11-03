/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.push;

import com.ca.mas.core.EventDispatcher;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASConstants;
import com.ca.mas.foundation.MASDevice;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.Observable;
import java.util.Observer;

/**
 * Handle Firebase Instance ID token refresh events,
 */
public class MASFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        //For Dynamic SDK, all server needs to rebind. So reset all all the bind status.
        MASPush.getInstance().clear(getApplicationContext());

        //If the MAS is started and device already been registered, perform the binding.
        if (MAS.getState(getApplicationContext()) == MASConstants.MAS_STATE_STARTED) {
            MASPush.STARTED_OBSERVER.update(null, null);
        }

    }
}
