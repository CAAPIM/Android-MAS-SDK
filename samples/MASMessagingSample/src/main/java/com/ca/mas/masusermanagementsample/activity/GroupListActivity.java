/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */

package com.ca.mas.masusermanagementsample.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;

import com.ca.mas.core.error.MAGError;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.masusermanagementsample.R;
import com.ca.mas.masusermanagementsample.adapter.DividerDecoration;
import com.ca.mas.masusermanagementsample.adapter.GroupRecyclerAdapter;
import com.ca.mas.masusermanagementsample.mas.GroupsManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupListActivity extends AppCompatActivity {

    private final String TAG = GroupListActivity.class.getSimpleName();
    private Context mContext;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContext = this;
        mRecyclerView = (RecyclerView) findViewById(R.id.group_list);
        assert mRecyclerView != null;

        MAS.start(this, true);
        MASUser.login("username", "password", getUserCallback());
    }

    private MASCallback<MASUser> getUserCallback() {
        return new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                MASGroup.newInstance().getAllGroups(user.getId(), getGroupsCallback());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, e.toString());
            }
        };
    }

    private MASCallback<List<MASGroup>> getGroupsCallback() {
        return new MASCallback<List<MASGroup>>() {
            @Override
            public void onSuccess(final List<MASGroup> groups) {
                if (groups != null && groups.size() > 0) {
                    Map<String, MASGroup> groupsMap = new HashMap<>();
                    for (MASGroup group : groups) {
                        String id = group.getGroupName();
                        groupsMap.put(id, group);
                    }

                    GroupsManager.INSTANCE.setGroups(groupsMap);
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        GroupRecyclerAdapter adapter = new GroupRecyclerAdapter(groups);
                        mRecyclerView.setAdapter(adapter);
                        mRecyclerView.addItemDecoration(new DividerDecoration(mContext));
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                MAGError error = (MAGError) e;
                Log.e(TAG, error.getMessage());
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_main, menu);
        return true;
    }

}
