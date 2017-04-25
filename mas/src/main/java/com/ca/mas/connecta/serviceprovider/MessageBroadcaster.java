/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.connecta.serviceprovider;

import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.messaging.MASMessageException;
import com.ca.mas.foundation.util.FoundationConsts;
import com.ca.mas.messaging.MASMessage;

/**
 * <p>The <b>MessageBroadcaster</b> class is used to turn a MASMessage received from Mqtt into an Intent object that can
 * be broadcast to registered listeners. The payload portion of the message is represented as a String format of the
 * JSONDocument that adheres to the message format as specified in the MAS architecture.</p>
 */
public class MessageBroadcaster {

    private Context mContext;

    /**
     * <b>Description:</b> Constructor.
     * @param context the Android runtime environment.
     */
    public MessageBroadcaster(Context context) {
        mContext = context;
    }

    /**
     * <b>Description:</b> Transform the MASMessage into an Android Intent and broadcast it.
     * @param masMessage the MASMessage received from the Mqtt broker.
     * @throws MASMessageException if the transformation fails for any reason during the call to
     * 'createJSONStringFromMASMessage()'.
     */
    public void broadcastMessage(MASMessage masMessage) throws MASMessageException {
        Intent intent = new Intent();
        intent.setAction(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED);
        intent.putExtra(FoundationConsts.KEY_EVENT_TYPE, FoundationConsts.KEY_ON_MESSAGE);
        intent.putExtra(FoundationConsts.KEY_MESSAGE, masMessage);
        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }
}
