/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 */
package com.ca.mas.masmessagingsample.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.ca.mas.masmessagingsample.R;
import com.ca.mas.masmessagingsample.adapter.DividerDecoration;
import com.ca.mas.masmessagingsample.adapter.MessageRecyclerAdapter;
import com.ca.mas.masmessagingsample.mas.DataManager;
import com.ca.mas.messaging.MASMessage;

import java.util.List;

public class MessageListActivity extends BaseActivity {
    private final String TAG = MessageListActivity.class.getSimpleName();
    private Context mContext;
    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mContext = this;
        mRecyclerView = (RecyclerView) findViewById(R.id.group_list);
        assert mRecyclerView != null;

        List<MASMessage> messages = DataManager.INSTANCE.getMessages();
        MessageRecyclerAdapter adapter = new MessageRecyclerAdapter(messages);
        mRecyclerView.setAdapter(adapter);
        mRecyclerView.addItemDecoration(new DividerDecoration(mContext));
    }
}
