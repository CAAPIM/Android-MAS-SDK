/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.masusermanagementsample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ca.mas.foundation.MASUser;
import com.ca.mas.masusermanagementsample.R;

import java.util.List;

public class UserRecyclerAdapter extends RecyclerView.Adapter<UserRecyclerAdapter.MASUserViewHolder> {
    private final List<MASUser> mUsers;

    public UserRecyclerAdapter(List<MASUser> items) {
        mUsers = items;
    }

    @Override
    public MASUserViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_content, parent, false);
        return new MASUserViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    @Override
    public void onBindViewHolder(final MASUserViewHolder holder, int position) {
        final MASUser member = mUsers.get(position);
        final String memberName = member.getDisplayName();

        holder.user = member;
        holder.contentView.setText(memberName);
        holder.imageView.setImageBitmap(member.getThumbnailImage());
    }

    public class MASUserViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView contentView;
        public final ImageView imageView;
        public MASUser user;

        public MASUserViewHolder(View view) {
            super(view);
            mView = view;
            contentView = (TextView) view.findViewById(R.id.user_description);
            imageView = (ImageView) view.findViewById(R.id.user_thumbnail);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
