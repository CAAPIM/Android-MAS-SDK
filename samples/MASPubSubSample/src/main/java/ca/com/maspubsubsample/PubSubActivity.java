package ca.com.maspubsubsample;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.connecta.util.ConnectaConsts;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASConfiguration;
import com.ca.mas.foundation.MASException;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;

import java.util.HashMap;

public class PubSubActivity extends NavDrawerActivity
        implements NavigationView.OnNavigationItemSelectedListener, TopicSubscriptionListener, View.OnClickListener {

    private static final String TAG = PubSubActivity.class.getSimpleName();
    private static final String FRAGMENT_SUBSCRIBE = "ca.com.maspubsubsample.SubscribeFragment";
    private static final String FRAGMENT_MESSAGES = "ca.com.maspubsubsample.MessagesFragment";

    NavigationView navigationView;
    private HashMap<String, Fragment> fragments;
    private HashMap<String, Topic> subscribedTopics;
    private boolean publicBroker;
    private String host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pub_sub);
        Intent i = getIntent();
        if( i != null ){
            publicBroker = i.getBooleanExtra(MainActivity.INTENT_EXTRA_PUBLIC_BROKER, false);
            host = i.getStringExtra(MainActivity.INTENT_EXTRA_HOST);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ( (TextView) navigationView.getHeaderView(0).findViewById(R.id.nav_header_host)).setText(host);
        (navigationView.getHeaderView(0).findViewById(R.id.nav_header_disconnect)).setOnClickListener(this);

        fragments = new HashMap<>();
        fragments.put(FRAGMENT_SUBSCRIBE, getSupportFragmentManager().findFragmentById(R.id.content_pub_sub_fragment_subscribe));
        fragments.put(FRAGMENT_MESSAGES, getSupportFragmentManager().findFragmentById(R.id.content_pub_sub_fragment_messages));
        hideAllFragments();

        subscribedTopics = new HashMap<>();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED);
        LocalBroadcastManager.getInstance(this).registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (!intent.getAction().equals(ConnectaConsts.MAS_CONNECTA_BROADCAST_MESSAGE_ARRIVED)) {
                    return;
                }

                try {
                    MASMessage message = MASMessage.newInstance(intent);
                    String topic = message.getTopic();
                    //TopicFragment topicFragment = subscribedTopics.get(topic).getTopicFragment();
                    //topicFragment.onMessageReceived(message);
                    MessagesFragment messagesFragment = (MessagesFragment) fragments.get(FRAGMENT_MESSAGES);
                    messagesFragment.onMessageReceived(message);
                } catch (MASException e) {
                    Log.d(TAG, e.getMessage());
                }

            }
        }, intentFilter);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.subscribe_to_topic) {
            showFragment(FRAGMENT_SUBSCRIBE);
        } else if ( id == R.id.messages) {
            showFragment(FRAGMENT_MESSAGES);
        } else {
            hideAllFragments();
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            for( String topic : subscribedTopics.keySet() ){
                if( topic.equals(item.getTitle()) ){
                    fragmentTransaction.show(subscribedTopics.get(topic).getTopicFragment());
                    break;
                } else {
                    fragmentTransaction.hide(subscribedTopics.get(topic).getTopicFragment());
                }
            }
            fragmentTransaction.commit();
        }

        item.setChecked(true);
        closeDrawer();
        return true;
    }

    @Override
    public void onSubscribeToTopic(String topicName, MASTopic topic) {
        //subscribedTopics.put(topicName, new Topic(topicName, topic));
        //final Menu menu = navigationView.getMenu();
        //MenuItem item = menu.add(R.id.topics_group, Menu.NONE, Menu.NONE, topicName);
        //item.setCheckable(true);
    }

    @Override
    public void onUnsubscribeToTopic(String topicName, MASTopic topic) {

    }

    @Override
    public boolean isSubscribedToTopic(String topicName) {
        return subscribedTopics.containsKey(topicName);
    }

    public boolean isPublicBroker() {
        return publicBroker;
    }

    @Override
    int getDrawerLayoutViewId() {
        return R.id.drawer_layout;
    }

    public HashMap<String, Topic> getSubscribedTopics() {
        return subscribedTopics;
    }

    @Override
    public void onClick(View view) {
        closeDrawer();
        int id = view.getId();
        switch (id){
            case R.id.nav_header_disconnect:
                MASConnectaManager.getInstance().disconnect(new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        Intent i = new Intent(PubSubActivity.this, MainActivity.class);
                        startActivity(i);
                        finish();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Util.showSnackbar(PubSubActivity.this, "Failed to disconnect", (CoordinatorLayout) findViewById(R.id.coordinator_layout));
                    }
                });
                break;
        }
    }

    private void showFragment(String fragmentKey) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for( Fragment fragment : fragments.values() ){
            transaction.hide(fragment);
        }
        transaction.show( fragments.get(fragmentKey) );
        transaction.commit();
    }

    private void hideAllFragments(){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        for( Fragment fragment : fragments.values() ){
            fragmentTransaction.hide(fragment);
        }
        fragmentTransaction.commit();
    }

    public static class Topic {
        private String topicName;
        private MASTopic masTopic;
        private TopicFragment topicFragment;

        public Topic(String topicName, MASTopic masTopic) {
            this.topicName = topicName;
            this.masTopic = masTopic;
            topicFragment = new TopicFragment();
            topicFragment.setTopicName(topicName);
        }

        public String getTopicName() {
            return topicName;
        }

        public MASTopic getMasTopic() {
            return masTopic;
        }

        public TopicFragment getTopicFragment() {
            return topicFragment;
        }
    }
}
