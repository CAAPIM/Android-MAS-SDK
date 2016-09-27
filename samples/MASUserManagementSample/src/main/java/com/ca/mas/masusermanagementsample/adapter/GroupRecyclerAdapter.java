/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masusermanagementsample.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ca.mas.foundation.MASGroup;
import com.ca.mas.masusermanagementsample.activity.GroupDetailActivity;
import com.ca.mas.masusermanagementsample.activity.GroupDetailFragment;
import com.ca.mas.masusermanagementsample.R;

import java.util.List;

public class GroupRecyclerAdapter extends RecyclerView.Adapter<GroupRecyclerAdapter.MASGroupViewHolder> {
    private final List<MASGroup> mGroups;

    public GroupRecyclerAdapter(List<MASGroup> items) {
        mGroups = items;
    }

    @Override
    public MASGroupViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.group_list_content, parent, false);
        return new MASGroupViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }

    @Override
    public void onBindViewHolder(final MASGroupViewHolder holder, int position) {
        final MASGroup group = mGroups.get(position);
        final String groupName = group.getGroupName();

        holder.group = group;
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

    public class MASGroupViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public final TextView mDetailsView;
        public MASGroup group;

        public MASGroupViewHolder(View view) {
            super(view);
            mView = view;
            mContentView = (TextView) view.findViewById(R.id.group_description);
            mDetailsView = (TextView) view.findViewById(R.id.group_details);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}