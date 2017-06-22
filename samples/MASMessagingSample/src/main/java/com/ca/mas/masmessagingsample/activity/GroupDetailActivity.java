/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.masmessagingsample.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASGroup;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.masmessagingsample.R;
import com.ca.mas.masmessagingsample.adapter.MemberRecyclerAdapter;
import com.ca.mas.masmessagingsample.mas.DataManager;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.MessagingConsts;

public class GroupDetailActivity extends BaseActivity implements MemberRecyclerAdapter.GroupDetailMessageListener {
    private Context mContext;
    private String mGroupName;
    private CoordinatorLayout mContainer;
    private String callingActivity = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_detail);
        mContext = this;

        mContainer = (CoordinatorLayout) findViewById(R.id.container);
        Toolbar toolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mGroupName = getIntent().getStringExtra(GroupDetailFragment.GROUP_NAME);
        callingActivity = getIntent().getStringExtra("callingActivity");

        final MASGroup group = DataManager.INSTANCE.getGroupById(mGroupName);

        FloatingActionButton fab_send = (FloatingActionButton) findViewById(R.id.fab_send);
        fab_send.setOnClickListener(getMessageOnClickListener(group));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, AddUsersActivity.class);
                intent.putExtra(GroupDetailFragment.GROUP_NAME, mGroupName);
                startActivity(intent);
            }
        });

        // savedInstanceState is non-null when there is fragment state
        // saved from previous configurations of this activity
        // (e.g. when rotating the screen from portrait to landscape).
        // In this case, the fragment will automatically be re-added
        // to its container so we don't need to manually add it.
        // For more information, see the Fragments API guide at:
        //
        // http://developer.android.com/guide/components/fragments.html
        //
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            arguments.putString(GroupDetailFragment.GROUP_NAME,
                    getIntent().getStringExtra(GroupDetailFragment.GROUP_NAME));
            GroupDetailFragment fragment = new GroupDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.group_detail_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            Class<?> clazz = GroupListActivity.class;
            if (callingActivity != null && callingActivity.equalsIgnoreCase(SearchGroupActivity.class.getName())) {
                clazz = SearchGroupActivity.class;
            }
            NavUtils.navigateUpTo(this, new Intent(this, clazz));
            //NavUtils.navigateUpTo(this, new Intent(this, GroupListActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void showMessage(String text) {
        Snackbar.make(mContainer, text, Snackbar.LENGTH_SHORT).show();
    }

    private View.OnClickListener getMessageOnClickListener(final MASGroup group) {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                EditText editText = new EditText(mContext);
                editText.setTag(group.getId());

                int density = mContext.getResources().getDisplayMetrics().densityDpi;
                int margin = Math.round(16 * density / DisplayMetrics.DENSITY_DEFAULT);

                builder.setTitle(mContext.getString(R.string.title_send_message));
                builder.setPositiveButton(mContext.getString(R.string.button_send), sendMessageOnClickListener(group, editText));
                builder.setView(editText, margin, 0, margin, 0);
                builder.show();
            }
        };
    }

    private DialogInterface.OnClickListener sendMessageOnClickListener(final MASGroup group, final EditText editText) {
        return new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String msg = editText.getEditableText().toString();
                String id = editText.getTag().toString();

                MASMessage message = MASMessage.newInstance();
                message.setContentType(MessagingConsts.DEFAULT_TEXT_PLAIN_CONTENT_TYPE);
                message.setPayload(msg.getBytes());

                MASUser.getCurrentUser().sendMessage(message, group, new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        showMessage("Message sent successfully");
                    }

                    @Override
                    public void onError(Throwable e) {
                        showMessage(e.getMessage());
                    }
                });
            }
        };
    }


}
