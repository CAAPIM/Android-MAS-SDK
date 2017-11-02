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
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.util.Observable;
import java.util.Observer;

/**
 * Handle Firebase Instance ID token refresh events,
 */
public class MASFirebaseInstanceIdService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {
        //Token is refreshing
        final PushConfig config = new PushConfig(getApplicationContext());
        if (config.isRegisterOnStartUp()) {
            MASPush.getInstance().setTokenRefresh();

            if (MAS.getState(getApplicationContext()) == MASConstants.MAS_STATE_STARTED) {
                MASPush.getInstance().register(config.getGrantType(), null);
            } else {
                //MAS has not been started, push registration will happened after MAS.start is invoked.
                final Observer observer = new Observer() {
                    @Override
                    public void update(Observable observable, Object o) {
                        MASPush.getInstance().register(config.getGrantType(),null);
                        //Detach from the start process after register
                        EventDispatcher.STARTED.deleteObserver(this);

                    }
                };
                EventDispatcher.STARTED.addObserver(observer);
            }
        }
    }
}
