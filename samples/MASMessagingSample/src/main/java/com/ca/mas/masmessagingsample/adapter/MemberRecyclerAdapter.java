/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.masmessagingsample.adapter;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.group.MASOwner;
import com.ca.mas.masmessagingsample.R;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.util.MessagingConsts;

import java.util.List;

public class MemberRecyclerAdapter extends RecyclerView.Adapter<MemberRecyclerAdapter.MASMemberViewHolder> {
    private final static String TAG = MemberRecyclerAdapter.class.getSimpleName();
    private final List<MASMember> mMembers;
    private final MASOwner mOwner;
    private Activity mActivity;
    private GroupDetailMessageListener mListener;

    public interface GroupDetailMessageListener {
        void showMessage(String text);
    }

    public MemberRecyclerAdapter(GroupDetailMessageListener listener, Activity activity, List<MASMember> items, MASOwner owner) {
        mMembers = items;
        mActivity = activity;
        mOwner = owner;
        mListener = listener;
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
        holder.mContentView.setText(memberName);
        holder.mMessageView.setOnClickListener(getMessageOnClickListener(member.getValue()));

        if (memberName.equals(mOwner.getDisplay())) {
            holder.mContentView.setTypeface(null, Typeface.BOLD);
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
        public final TextView mContentView;
        public final ImageView mImageView;
        public final ImageView mMessageView;
        public MASMember member;

        public MASMemberViewHolder(View view) {
            super(view);
            mView = view;
            mImageView = (ImageView) view.findViewById(R.id.user_thumbnail);
            mContentView = (TextView) view.findViewById(R.id.user_description);
            mMessageView = (ImageView) view.findViewById(R.id.user_send_message);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }

    private View.OnClickListener getMessageOnClickListener(final String id) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                EditText editText = new EditText(mActivity);
                editText.setTag(id);

                int density = mActivity.getResources().getDisplayMetrics().densityDpi;
                int margin = Math.round(16 * density / DisplayMetrics.DENSITY_DEFAULT);

                builder.setTitle(mActivity.getString(R.string.title_send_message));
                builder.setPositiveButton(mActivity.getString(R.string.button_send), sendMessageOnClickListener(editText));
                builder.setView(editText, margin, 0, margin, 0);
                builder.show();
            }
        };
    }

    private DialogInterface.OnClickListener sendMessageOnClickListener(final EditText editText) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String message = editText.getEditableText().toString();
                String id = editText.getTag().toString();

                MASUser.getCurrentUser().getUserById(id, getUserCallback(message));
            }
        };
    }

    private MASCallback<MASUser> getUserCallback(final String text) {
        return new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser result) {
                MASMessage message = MASMessage.newInstance();
                message.setContentType(MessagingConsts.MT_TEXT_PLAIN);
                message.setPayload(text.getBytes());

                MASUser.getCurrentUser().sendMessage(message, result, getSendMessageCallback());
            }

            @Override
            public void onError(Throwable e) {
                mListener.showMessage(e.toString());
            }
        };
    }

    private MASCallback<Void> getSendMessageCallback() {
        return new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                mListener.showMessage("Message successfully sent.");
            }

            @Override
            public void onError(Throwable e) {
                mListener.showMessage(e.toString());
            }
        };
    }
}
