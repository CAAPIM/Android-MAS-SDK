/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.masusermanagementsample.adapter;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.group.MASOwner;
import com.ca.mas.masusermanagementsample.R;

import java.util.List;

public class MemberRecyclerAdapter extends RecyclerView.Adapter<MemberRecyclerAdapter.MASMemberViewHolder> {
    private final static String TAG = MemberRecyclerAdapter.class.getSimpleName();
    private final List<MASMember> mMembers;
    private final MASOwner mOwner;
    private Activity mActivity;

    public MemberRecyclerAdapter(Activity activity, List<MASMember> items, MASOwner owner) {
        mMembers = items;
        mActivity = activity;
        mOwner = owner;
    }

    @Override
    public MASMemberViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_list_content, parent, false);
        return new MASMemberViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mMembers.size();
    }

    @Override
    public void onBindViewHolder(final MASMemberViewHolder holder, int position) {
        final MASMember member = mMembers.get(position);
        final String memberName = member.getDisplay();

        holder.member = member;
        holder.contentView.setText(memberName);
        if (memberName.equals(mOwner.getDisplay())) {
            holder.contentView.setTypeface(null, Typeface.BOLD);
        }

        String id = member.getValue();
        MASUser.getCurrentUser().getUserById(id, new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser result) {
                final Bitmap thumbnail = result.getThumbnailImage();

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        holder.mImageView.setImageBitmap(thumbnail);
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Failed to load the user: " + e.toString());
            }
        });
    }

    public class MASMemberViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView contentView;
        public final ImageView mImageView;
        public MASMember member;

        public MASMemberViewHolder(View view) {
            super(view);
            mView = view;
            contentView = (TextView) view.findViewById(R.id.user_description);
            mImageView = (ImageView) view.findViewById(R.id.user_thumbnail);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + contentView.getText() + "'";
        }
    }
}
