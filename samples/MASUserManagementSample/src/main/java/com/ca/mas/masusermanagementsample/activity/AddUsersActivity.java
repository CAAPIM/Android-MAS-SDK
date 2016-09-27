/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masusermanagementsample.activity;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.identity.common.MASFilteredRequest;
import com.ca.mas.identity.group.MASMember;
import com.ca.mas.identity.user.UserAttributes;
import com.ca.mas.identity.util.IdentityConsts;
import com.ca.mas.masusermanagementsample.R;
import com.ca.mas.masusermanagementsample.adapter.DividerDecoration;
import com.ca.mas.masusermanagementsample.adapter.UserRecyclerAdapter;
import com.ca.mas.masusermanagementsample.mas.GroupsManager;

import java.util.List;

public class AddUsersActivity extends AppCompatActivity {
    private static final String TAG = AddUsersActivity.class.getSimpleName();
    private TextInputEditText mSearchView;
    private RecyclerView mRecyclerView;
    private Context mContext;
    private String mGroupName;
    private List<MASUser> mResultList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_users);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mResultList != null && mResultList.size() > 0) {
                    save();
                } else {
                    Snackbar.make(mRecyclerView, "No users to add.", Snackbar.LENGTH_SHORT)
                            .show();
                }
            }
        });

        mContext = this;
        mGroupName = getIntent().getStringExtra(GroupDetailFragment.GROUP_NAME);
        mSearchView = (TextInputEditText) findViewById(R.id.searchView);
        mRecyclerView = (RecyclerView) findViewById(R.id.searchResults);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_add_to_group:
                MASUser.getCurrentUser().getUserMetaData(new MASCallback<UserAttributes>() {
                    @Override
                    public void onSuccess(UserAttributes result) {
                        searchForUser(result);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Failed to get user attributes: " + e);
                    }
                });
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void searchForUser(UserAttributes attributes) {
        MASFilteredRequest request = new MASFilteredRequest(attributes.getAttributes(),
                IdentityConsts.KEY_USER_ATTRIBUTES);
        request.contains("userName", mSearchView.getText().toString());

        MASUser.getCurrentUser().getUsersByFilter(request, new MASCallback<List<MASUser>>() {
            @Override
            public Handler getHandler() {
                return new Handler(Looper.getMainLooper());
            }

            @Override
            public void onSuccess(List<MASUser> result) {
                LinearLayoutManager llm = new LinearLayoutManager(mContext);
                llm.setOrientation(LinearLayoutManager.VERTICAL);
                mRecyclerView.setLayoutManager(llm);

                mResultList = result;
                UserRecyclerAdapter adapter = new UserRecyclerAdapter(result);
                mRecyclerView.setAdapter(adapter);
                mRecyclerView.addItemDecoration(new DividerDecoration(mContext));
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG + "getUsersByFilter()", e.toString());
            }
        });
    }

    private void save() {
        MASGroup group = GroupsManager.INSTANCE.getGroupById(mGroupName);
        if (group != null) {
            if (mResultList != null && mResultList.size() > 0) {
                for (MASUser user : mResultList) {
                    saveUserToGroup(group, user);
                }
            }
        }
    }

    private void saveUserToGroup(final MASGroup group, MASUser user) {
        final MASMember member = new MASMember(user);
        group.addMember(member);
        group.save(new MASCallback<MASGroup>() {
            @Override
            public void onSuccess(MASGroup result) {
                Snackbar.make(mRecyclerView, "Successfully added member to the group.", Snackbar.LENGTH_SHORT)
                        .show();
            }

            @Override
            public void onError(Throwable e) {
                group.removeMember(member);
                Snackbar.make(mRecyclerView, "Failed to save member to group: " + e, Snackbar.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
