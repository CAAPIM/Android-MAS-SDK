/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.masmessagingsample.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ca.mas.masmessagingsample.R;

import java.util.List;

public abstract class TwoLineRecyclerAdapter<Data> extends RecyclerView.Adapter<TwoLineRecyclerAdapter.TwoLineViewHolder> {
    protected List<Data> mGroups;

    public class TwoLineViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mContentView;
        public final TextView mDetailsView;
        protected Data data;

        public TwoLineViewHolder(View view) {
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

    @Override
    public TwoLineViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.group_list_content, parent, false);

        return new TwoLineViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mGroups.size();
    }
}