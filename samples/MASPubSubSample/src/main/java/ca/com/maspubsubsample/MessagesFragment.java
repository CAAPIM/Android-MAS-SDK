package ca.com.maspubsubsample;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ca.mas.messaging.MASMessage;

public class MessagesFragment extends Fragment implements View.OnClickListener{

    MessagesRecyclerView recyclerView;
    TopicMessagesAdapter topicMessagesAdapter;

    public MessagesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        View v = inflater.inflate(R.layout.fragment_messages, container, false);
        recyclerView = (MessagesRecyclerView) v.findViewById(R.id.fragment_messages_recycler);
        topicMessagesAdapter = new TopicMessagesAdapter(getContext());
        recyclerView.setAdapter(topicMessagesAdapter);
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
        switch (id){
            case R.id.fragment_messages_fab:
                PublishDialogFragment publishDialogFragment = PublishDialogFragment.newInstance();
                publishDialogFragment.show(getActivity().getSupportFragmentManager(), null);
                break;
        }
    }

    public void onMessageReceived(MASMessage masMessage){
        topicMessagesAdapter.addMessage(masMessage);
    }
}
