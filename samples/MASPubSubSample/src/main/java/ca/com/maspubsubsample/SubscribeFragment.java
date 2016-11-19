package ca.com.maspubsubsample;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscribeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = SubscribeFragment.class.getSimpleName();

    EditText editTextTopicName;
    QosSpinner qosSpinner;
    TextView textViewMessage;
    Button buttonSubscribe;

    private TopicSubscriptionListener topicSubscriptionListener;

    public SubscribeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subscribe, container, false);
        editTextTopicName = (EditText) v.findViewById(R.id.fragment_subscribe_edit_text_topic_name);
        qosSpinner = (QosSpinner) v.findViewById(R.id.fragment_subscribe_spinner_qos);
        textViewMessage = (TextView) v.findViewById(R.id.fragment_subscribe_text_view_message);
        buttonSubscribe = (Button) v.findViewById(R.id.fragment_subscribe_button_subscribe);
        buttonSubscribe.setOnClickListener(this);

        topicSubscriptionListener = (TopicSubscriptionListener) getActivity();
        return v;
    }

    @Override
    public void onClick(View view) {
        try {
            final String topicName = editTextTopicName.getText().toString();
            if( topicSubscriptionListener.isSubscribedToTopic(topicName) ){
                setMessage(getResources().getString(R.string.subscribe_to_topic_message_already_subscribed));
                return;
            }

            final MASTopic masTopic = new MASTopicBuilder()
                    .setCustomTopic(topicName)
                    .setQos(qosSpinner.getSelectedQos())
                    .setUserId(MASUser.getCurrentUser().getId())
                    .build();

            MASConnectaManager.getInstance().subscribe(masTopic, new MASCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    setMessage(String.format(getResources()
                            .getString(R.string.subscribe_to_topic_message_success), topicName));
                    topicSubscriptionListener.onSubscribeToTopic(masTopic.toString(), masTopic);
                }

                @Override
                public void onError(Throwable e) {
                    setMessage(String.format(getResources().getString(
                            R.string.subscribe_to_topic_message_error), e.getMessage()));
                }
            });
        } catch (MASException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    private void setMessage(String message){
        textViewMessage.setText(message);

    }
}
