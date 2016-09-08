/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.masmessagingsample.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import com.ca.mas.foundation.MASGroup;
import com.ca.mas.masmessagingsample.activity.GroupDetailActivity;
import com.ca.mas.masmessagingsample.activity.GroupDetailFragment;

import java.util.List;

public class GroupRecyclerAdapter extends TwoLineRecyclerAdapter<MASGroup> {

    public GroupRecyclerAdapter(List<MASGroup> items) {
        mGroups = items;
    }

    @Override
    public void onBindViewHolder(TwoLineRecyclerAdapter.TwoLineViewHolder holder, int position) {
        final MASGroup group = mGroups.get(position);
        final String groupName = group.getGroupName();

        holder.data = group;
        holder.mContentView.setText(groupName);
        holder.mDetailsView.setText(group.getOwner().getDisplay() + ": " + group.getId());
        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = v.getContext();
                Intent intent = new Intent(context, GroupDetailActivity.class);
                intent.putExtra(GroupDetailFragment.GROUP_NAME, groupName);

                context.startActivity(intent);
            }
        });
    }
}