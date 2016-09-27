/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masusermanagementsample.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ca.mas.foundation.MASGroup;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.masusermanagementsample.R;
import com.ca.mas.masusermanagementsample.adapter.DividerDecoration;
import com.ca.mas.masusermanagementsample.adapter.MemberRecyclerAdapter;
import com.ca.mas.masusermanagementsample.mas.GroupsManager;

import java.util.List;

public class GroupDetailFragment extends Fragment {
    public static final String GROUP_NAME = "group_name";
    private MASGroup mGroup;
    private MemberRecyclerAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public GroupDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(GROUP_NAME)) {
            String groupName = getArguments().getString(GROUP_NAME);
            mGroup = GroupsManager.INSTANCE.getGroupById(groupName);

            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
            if (appBarLayout != null) {
                appBarLayout.setTitle(groupName);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.group_list, container, false);

        if (mGroup != null) {
            RecyclerView recyclerView = (RecyclerView) rootView.findViewById(R.id.group_list);
            if (recyclerView != null) {
                List<MASMember> members = mGroup.getMembers();
                if (members != null) {
                    Activity activity = getActivity();
                    mAdapter = new MemberRecyclerAdapter(activity, members, mGroup.getOwner());
                    recyclerView.setAdapter(mAdapter);
                    recyclerView.addItemDecoration(new DividerDecoration(activity));
                }
            }
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }
}
