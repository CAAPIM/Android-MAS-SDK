package ca.com.maspubsubsample;


import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;

public class SubscribeFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = SubscribeFragment.class.getSimpleName();

    TextInputEditText editTextTopicName;
    SelectQosView selectQosView;
    TextView textViewMessage;
    ProgressBar progressBar;
    Button buttonSubscribe;
    Button buttonUnsubscribe;
    EmptyFieldTextWatcher emptyFieldTextWatcher;

    public SubscribeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subscribe, container, false);
        editTextTopicName = (TextInputEditText) v.findViewById(R.id.fragment_subscribe_edit_text_topic_name);
        selectQosView = (SelectQosView) v.findViewById(R.id.fragment_subscribe_select_qos);
        textViewMessage = (TextView) v.findViewById(R.id.fragment_subscribe_text_view_message);
        progressBar = (ProgressBar) v.findViewById(R.id.fragment_subscribe_progress_bar);
        buttonSubscribe = (Button) v.findViewById(R.id.fragment_subscribe_button_subscribe);
        buttonSubscribe.setOnClickListener(this);
        buttonUnsubscribe = (Button) v.findViewById(R.id.fragment_subscribe_button_unsubscribe);
        buttonUnsubscribe.setOnClickListener(this);
        emptyFieldTextWatcher = new EmptyFieldTextWatcher(
                new View[]{buttonSubscribe, buttonUnsubscribe},
                new EditText[]{editTextTopicName});
        return v;
    }

    @Override
    public void onClick(View view) {
        textViewMessage.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        int id = view.getId();
        try {
            final String topicName = editTextTopicName.getText().toString();

            MASTopicBuilder masTopicBuilder = new MASTopicBuilder()
                    .setCustomTopic(topicName)
                    .setQos(selectQosView.getSelectedQos());
            if (MASUser.getCurrentUser() != null) {
                masTopicBuilder.setUserId(MASUser.getCurrentUser().getId());
            }
            if (getPubSubActivity().isPublicBroker()) {
                masTopicBuilder.enforceTopicStructure(false);
            }

            final MASTopic masTopic = masTopicBuilder.build();

            MASConnectaManager masConnectaManager = MASConnectaManager.getInstance();
            switch (id) {
                case R.id.fragment_subscribe_button_subscribe:
                    masConnectaManager.subscribe(masTopic, new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            setMessage(String.format(getResources()
                                    .getString(R.string.subscribe_to_topic_message_success), topicName));
                        }

                        @Override
                        public void onError(Throwable e) {
                            setMessage(String.format(getResources().getString(
                                    R.string.subscribe_to_topic_message_error), e.getMessage()));
                        }
                    });
                    break;
                case R.id.fragment_subscribe_button_unsubscribe:
                    masConnectaManager.unsubscribe(masTopic, new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            setMessage(String.format(getResources()
                                    .getString(R.string.unsubscribe_from_topic_message_success), topicName));
                        }

                        @Override
                        public void onError(Throwable e) {
                            setMessage(String.format(getResources().getString(
                                    R.string.unsubscribe_from_topic_message_error), e.getMessage()));
                        }
                    });
                    break;
            }
        } catch (MASException e) {
            Log.d(TAG, e.getMessage());
        } finally {
            Util.hideKeyboard(getActivity());
            editTextTopicName.setText("");
        }
    }

    private PubSubActivity getPubSubActivity() {
        return (PubSubActivity) getActivity();
    }

    private void setMessage(final String message) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViewMessage.setText(message);
                progressBar.setVisibility(View.GONE);
                textViewMessage.setVisibility(View.VISIBLE);
            }
        });
    }
}
