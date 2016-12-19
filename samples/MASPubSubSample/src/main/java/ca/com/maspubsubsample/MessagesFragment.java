/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package ca.com.maspubsubsample;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ca.mas.messaging.MASMessage;

public class MessagesFragment extends Fragment implements View.OnClickListener {

    MessagesRecyclerView recyclerView;
    MessagesAdapter messagesAdapter;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        View v = inflater.inflate(R.layout.fragment_messages, container, false);
        recyclerView = (MessagesRecyclerView) v.findViewById(R.id.fragment_messages_recycler);
        messagesAdapter = new MessagesAdapter(getContext());
        recyclerView.setAdapter(messagesAdapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), linearLayoutManager.getOrientation()));
        recyclerView.setEmptyView(v.findViewById(R.id.fragment_messages_empty_view));
        FloatingActionButton fab = (FloatingActionButton) v.findViewById(R.id.fragment_messages_fab);
        fab.setOnClickListener(this);
        return v;
    }

    @Override
    public void onClick(View view) {
        int id = view.getId();
        switch (id) {
            case R.id.fragment_messages_fab:
                PublishDialogFragment publishDialogFragment = new PublishDialogFragment();
                publishDialogFragment.show(getActivity().getSupportFragmentManager(), null);
                break;
        }
    }

    public void onMessageReceived(MASMessage masMessage) {
        messagesAdapter.addMessage(masMessage);
    }
}
