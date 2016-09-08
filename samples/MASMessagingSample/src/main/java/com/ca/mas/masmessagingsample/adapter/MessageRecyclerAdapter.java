/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masmessagingsample.adapter;

import com.ca.mas.messaging.MASMessage;

import java.util.List;

public class MessageRecyclerAdapter extends TwoLineRecyclerAdapter<MASMessage> {

    public MessageRecyclerAdapter(List<MASMessage> items) {
        mGroups = items;
    }

    @Override
    public void onBindViewHolder(TwoLineRecyclerAdapter.TwoLineViewHolder holder, int position) {
        final MASMessage message = mGroups.get(position);
        final String sender = message.getDisplayName();

        byte[] messageData = message.getPayload();
        String messageText = new String(messageData);

        holder.data = message;
        holder.mContentView.setText(sender);
        holder.mDetailsView.setText(messageText);
    }
}