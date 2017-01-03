/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */

package ca.com.maspubsubsample;


import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.ca.mas.connecta.client.MASConnectaManager;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASException;
import com.ca.mas.foundation.MASUser;
import com.ca.mas.messaging.MASMessage;
import com.ca.mas.messaging.topic.MASTopic;
import com.ca.mas.messaging.topic.MASTopicBuilder;
import com.ca.mas.messaging.util.MessagingConsts;

public class PublishDialogFragment extends DialogFragment implements DialogInterface.OnClickListener, DialogInterface.OnShowListener {

    private static final String TAG = PublishDialogFragment.class.getSimpleName();

    TextInputEditText editTextTopic;
    TextInputEditText editTextMessage;
    CheckBox checkBoxRetain;
    SelectQosView selectQosView;
    EmptyFieldTextWatcher emptyFieldTextWatcher;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder b = new AlertDialog.Builder(getActivity())
                .setPositiveButton("Publish", this)
                .setNegativeButton("Cancel", this);

        LayoutInflater i = getActivity().getLayoutInflater();

        View v = i.inflate(R.layout.fragment_publish_dialog, null);
        editTextTopic = (TextInputEditText) v.findViewById(R.id.fragment_publish_edit_text_topic);
        editTextMessage = (TextInputEditText) v.findViewById(R.id.fragment_publish_edit_text_message);
        checkBoxRetain = (CheckBox) v.findViewById(R.id.fragment_publish_check_box_retain);
        selectQosView = (SelectQosView) v.findViewById(R.id.fragment_publish_select_qos);
        b.setView(v);
        AlertDialog alertDialog = b.create();
        alertDialog.setOnShowListener(this);

        return alertDialog;
    }

    @Override
    public void onClick(DialogInterface dialogInterface, int i) {
        switch (i) {
            case DialogInterface.BUTTON_POSITIVE:
                String topic = editTextTopic.getText().toString();
                String message = editTextMessage.getText().toString();
                boolean retain = checkBoxRetain.isChecked();
                Integer qos = selectQosView.getSelectedQos();

                try {
                    MASTopicBuilder masTopicBuilder = new MASTopicBuilder()
                            .setCustomTopic(topic)
                            .setQos(qos);
                    if (MASUser.getCurrentUser() != null) {
                        masTopicBuilder.setUserId(MASUser.getCurrentUser().getId());
                    }
                    if (getPubSubActivity().isPublicBroker()) {
                        masTopicBuilder.enforceTopicStructure(false);
                    }
                    MASTopic masTopic = masTopicBuilder.build();

                    MASMessage masMessage = MASMessage.newInstance();
                    masMessage.setTopic(masTopic.toString());
                    masMessage.setContentType(MessagingConsts.MT_TEXT_PLAIN);
                    masMessage.setPayload(message.getBytes());
                    masMessage.setQos(qos);
                    masMessage.setRetained(retain);

                    MASConnectaManager.getInstance().publish(masTopic, masMessage, new MASCallback<Void>() {
                        @Override
                        public void onSuccess(Void result) {
                            Log.i(TAG, "Published message");
                        }

                        @Override
                        public void onError(Throwable e) {
                            Log.d(TAG, "Failed to publish message: " + e.getMessage());
                        }
                    });
                } catch (MASException e) {
                    Log.d(TAG, "Failed to build topic: " + e.getMessage());
                }

                break;
            case DialogInterface.BUTTON_NEGATIVE:
                break;
        }
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        // Disable button on start dialog
        AlertDialog alertDialog = (AlertDialog) getDialog();
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
        emptyFieldTextWatcher = new EmptyFieldTextWatcher(new View[]{alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)},
                new EditText[]{editTextTopic, editTextMessage});
    }

    private PubSubActivity getPubSubActivity() {
        return (PubSubActivity) getActivity();
    }
}
