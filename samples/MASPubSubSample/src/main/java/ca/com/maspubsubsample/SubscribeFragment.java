package ca.com.maspubsubsample;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;


/**
 * A simple {@link Fragment} subclass.
 */
public class SubscribeFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = SubscribeFragment.class.getSimpleName();

    EditText editTextTopicName;
    QosSpinner qosSpinner;

    public SubscribeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_subscribe, container, false);
        editTextTopicName = (EditText) v.findViewById(R.id.fragment_subscribe_edit_text_topic_name);
        qosSpinner = (QosSpinner) v.findViewById(R.id.fragment_subscribe_spinner_qos);
        return v;
    }

    @Override
    public void onClick(View view) {
        try {
            MASTopic masTopic = new MASTopicBuilder()
                    .setCustomTopic(editTextTopicName.getText().toString())
                    .setQos(qosSpinner.getSelectedQos())
                    .build();

            MASConnectaManager.getInstance().subscribe(masTopic, new MASCallback<Void>() {
                @Override
                public void onSuccess(Void result) {

                }

                @Override
                public void onError(Throwable e) {

                }
            });
        } catch (MASException e) {
            Log.d(TAG, e.getMessage());
        }
    }
}
