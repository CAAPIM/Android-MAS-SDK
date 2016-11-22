package ca.com.maspubsubsample;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ca.mas.connecta.client.MASConnectaListener;
import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.MASMessage;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class TopicFragment extends Fragment implements View.OnClickListener{

    private static final String TAG = TopicFragment.class.getSimpleName();

    RecyclerView recyclerView;
    TopicMessagesAdapter topicMessagesAdapter;
    FloatingActionButton fabPublish;

    private String topicName;

    public TopicFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setRetainInstance(true);
        View v = inflater.inflate(R.layout.fragment_topic, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.fragment_topic_recycler_view);
        fabPublish = (FloatingActionButton) v.findViewById(R.id.fragment_topic_fab);
        topicMessagesAdapter = new TopicMessagesAdapter(getContext());
        fabPublish.setOnClickListener(this);
        recyclerView.setAdapter(topicMessagesAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return v;
    }

    @Override
    public void onClick(View view) {
        // On click fab
        PubSubActivity pubSubActivity = (PubSubActivity) getActivity();
        PublishDialogFragment publishDialogFragment = PublishDialogFragment.newInstance(topicName, pubSubActivity.getSubscribedTopics().get(topicName).getMasTopic());
        publishDialogFragment.show(pubSubActivity.getSupportFragmentManager(), null);
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public void onMessageReceived(MASMessage masMessage){
        topicMessagesAdapter.addMessage(masMessage);
    }
}
