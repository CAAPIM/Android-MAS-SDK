/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package com.ca.mas.masmessagingsample.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ca.mas.core.error.MAGError;
import com.ca.mas.foundation.MAS;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.masmessagingsample.R;
import com.ca.mas.masmessagingsample.adapter.DividerDecoration;
import com.ca.mas.masmessagingsample.adapter.GroupRecyclerAdapter;
import com.ca.mas.masmessagingsample.mas.DataManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GroupListActivity extends BaseActivity {

    private final String TAG = GroupListActivity.class.getSimpleName();
    private Context mContext;
    private RecyclerView mRecyclerView;
    private ProgressDialog mProgress;
    private boolean _freshLaunch = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContext = this;
        _freshLaunch = true;
        mProgress = new ProgressDialog(this);
        mProgress.show();
        mRecyclerView = (RecyclerView) findViewById(R.id.group_list);
        assert mRecyclerView != null;

        FloatingActionButton searchGroup = (FloatingActionButton) findViewById(R.id.search_group);
        searchGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, SearchGroupActivity.class);
                startActivity(intent);
            }
        });

        FloatingActionButton createGroup = (FloatingActionButton) findViewById(R.id.create_group);
        createGroup.setOnClickListener(getGroupListener(false));

        FloatingActionButton deleteGroup = (FloatingActionButton) findViewById(R.id.delete_group);
        deleteGroup.setOnClickListener(getGroupListener(true));

        MAS.start(this, true);
        MASUser.login("username", "password".toCharArray(), getUserCallback());
    }

    @Override
    public void onResume() {
        super.onResume();
        MASUser user = MASUser.getCurrentUser();
        if (!_freshLaunch && (user == null || !user.isAuthenticated())) {
            mProgress.dismiss();
            finishActivity(1L);
        }
        _freshLaunch = false;
    }

    private MASCallback<MASUser> getUserCallback() {
        return new MASCallback<MASUser>() {
            @Override
            public void onSuccess(MASUser user) {
                MASGroup.newInstance().getAllGroups(user.getId(), getGroupsCallback());

                user.startListeningToMyMessages(new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Log.i(TAG, "");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, e.toString());
                    }
                });
            }

            @Override
            public void onError(Throwable e) {
                String msg = e.getMessage();
                mProgress.dismiss();
                Log.e(TAG, msg);
                if (e instanceof MASException) {
                    MASException err = (MASException) e;
                    msg = err.getRootCause().getMessage() != null ? err.getRootCause().getMessage() : msg;
                }
                Snackbar.make(getWindow().getDecorView(), msg, Snackbar.LENGTH_LONG).show();
                finishActivity(2000L);
            }
        };
    }

    private void finishActivity(long pauseTime) {
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                MAS.getCurrentActivity().finish();
                Intent i = new Intent(MAS.getCurrentActivity(), MessagingLaunch.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                MAS.getCurrentActivity().startActivity(i);
            }
        }, pauseTime);

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

                    DataManager.INSTANCE.setGroups(groupsMap);
                }
                mProgress.dismiss();
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
                mProgress.dismiss();
                Snackbar.make(getWindow().getDecorView(), error.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.search_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_inbox:
                Intent intent = new Intent(this, MessageListActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_logout:
                MASUser user = MASUser.getCurrentUser();
                if (user != null && user.isAuthenticated()) {
                    user.logout(new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Snackbar.make(getWindow().getDecorView(), "Logout Done", Snackbar.LENGTH_SHORT).show();
                            finish();
                        }

                        @Override
                        public void onError(final Throwable e) {
                            Snackbar.make(getWindow().getDecorView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    });
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private View.OnClickListener getGroupListener(final boolean delete) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                EditText editText = new EditText(mContext);


                int density = mContext.getResources().getDisplayMetrics().densityDpi;
                int margin = Math.round(16 * density / DisplayMetrics.DENSITY_DEFAULT);

                builder.setTitle(mContext.getString(!delete ? R.string.title_create_group : R.string.title_delete_group));
                builder.setPositiveButton(mContext.getString(!delete ? R.string.button_create : R.string.button_delete), groupListener(editText, delete));
                builder.setNegativeButton(mContext.getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.setView(editText, margin, 0, margin, 0);
                builder.show();
            }
        };
    }

    private DialogInterface.OnClickListener groupListener(final EditText editText, final boolean delete) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String groupName = editText.getEditableText().toString();
                if (groupName == null || groupName.isEmpty() || groupName.trim().isEmpty()) {
                    Snackbar.make(getWindow().getDecorView(), "Invalid Group Name entered", Snackbar.LENGTH_SHORT).show();
                    dialogInterface.dismiss();
                    return;
                }

                if (delete) {
                    MASGroup.newInstance().getGroupByGroupName(groupName, getGroupCallback());
                } else {
                    MASGroup group = MASGroup.newInstance();
                    group.setGroupName(groupName);
                    group.save(getGroupCreateCallback());
                }

            }
        };
    }

    private MASCallback<List<MASGroup>> getGroupCallback() {
        return new MASCallback<List<MASGroup>>() {
            @Override
            public void onSuccess(List<MASGroup> result) {
                for (final MASGroup grp : result) {
                    if (MASUser.getCurrentUser().getUserName().equalsIgnoreCase(grp.getOwner().getValue())) {
                        grp.delete(getGroupDeleteCallback());
                    }
                }
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(getWindow().getDecorView(), "Group deletion failed:" + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private MASCallback<Void> getGroupDeleteCallback() {
        return new MASCallback<Void>() {
            @Override
            public void onSuccess(Void result) {
                Snackbar.make(getWindow().getDecorView(), "Group deleted", Snackbar.LENGTH_SHORT).show();
                MASGroup.newInstance().getAllGroups(MASUser.getCurrentUser().getId(), getGroupsCallback());
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(getWindow().getDecorView(), "Group deletion failed:" + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        };
    }

    private MASCallback<MASGroup> getGroupCreateCallback() {
        return new MASCallback<MASGroup>() {
            @Override
            public void onSuccess(MASGroup result) {
                Snackbar.make(getWindow().getDecorView(), "Group created", Snackbar.LENGTH_SHORT).show();
                MASGroup.newInstance().getAllGroups(MASUser.getCurrentUser().getId(), getGroupsCallback());
            }

            @Override
            public void onError(Throwable e) {
                Snackbar.make(getWindow().getDecorView(), "Group creation failed:" + e.getMessage(), Snackbar.LENGTH_LONG).show();
            }
        };
    }
}
