/*
 * Copyright (c) 2016 CA. All rights reserved.
 *
 * This software may be modified and distributed under the terms
 * of the MIT license.  See the LICENSE file for details.
 *
 */
package com.ca.mas.ui.otp;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableLayout;

import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.core.error.MAGServerException;
import com.ca.mas.foundation.MASCallback;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.ui.R;
import com.ca.mas.ui.listener.MASErrorMessageListener;
import com.ca.mas.ui.listener.MASFragmentAttachDismissListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample to show a select otp delivery channel dialog as a Fragment
 * Deprecated: use {@link MASOtpDialogFragment}
 */
@Deprecated
public class MASOtpSelectDeliveryChannelFragment extends DialogFragment {
    private static final String TAG = MASOtpSelectDeliveryChannelFragment.class.getCanonicalName();
    private static final String HANDLER = "handle";
    private MASOtpAuthenticationHandler handler;
    String errorMessage = "Request Cancelled";
    boolean isRequestProcessing = false;

    /**
     * Use this factory method to create a new instance of this fragment
     *
     * @return A new instance of fragment MASOtpSelectDeliveryChannelFragment.
     */
    public static MASOtpSelectDeliveryChannelFragment newInstance(MASOtpAuthenticationHandler handler) {
        MASOtpSelectDeliveryChannelFragment fragment = new MASOtpSelectDeliveryChannelFragment();
        Bundle args = new Bundle();
        args.putParcelable(HANDLER, handler);
        fragment.setArguments(args);
        return fragment;
    }

    public MASOtpSelectDeliveryChannelFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setCancelable(false);
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            handler = getArguments().getParcelable(HANDLER);

            if (handler.isInvalidOtp()) {
                DialogFragment otpAuthFragment = MASOtpAuthFragment.newInstance(handler);
                Bundle bundle = new Bundle();
                bundle.putBoolean(OtpConstants.IS_INVALID_OTP, true);
                bundle.putParcelable(HANDLER, handler);
                otpAuthFragment.setArguments(bundle);
                otpAuthFragment.show(getActivity().getFragmentManager(), "OTPDialog");
                dismiss();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_otp_select_delivery_channels, container, false);
        LinearLayout llOtpChannels = (LinearLayout) view.findViewById(R.id.llOtpChannels);
        List<String> channels = handler.getChannels();
        if (channels == null || channels.size() == 0) {
            channels = new ArrayList<String>();
            channels.add("EMAIL");
        }
        final List<String> otpChannelsFinal = channels;
        LinearLayout llCheckboxContainer = new TableLayout(getActivity());
        llCheckboxContainer.setPadding(100, 0, 0, 0);
        final CheckBox[] cbChannels = new CheckBox[channels.size()];
        for (int i = 0; i < channels.size(); i++) {
            cbChannels[i] = new CheckBox(getActivity());
            // cbChannels[i].set
            cbChannels[i].setText(channels.get(i));
            llCheckboxContainer.addView(cbChannels[i], i);
        }
        llOtpChannels.addView(llCheckboxContainer, 0);

        Button btnsendotp = (Button) view.findViewById(R.id.btnsendotp);
        btnsendotp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String channel = "";
                isRequestProcessing = true;
                if (otpChannelsFinal.size() == 1) {
                    channel = otpChannelsFinal.get(0);
                } else {
                    for (int i = 0; i < otpChannelsFinal.size(); i++) {
                        if (cbChannels[i].isChecked()) {
                            if (channel.isEmpty()) {
                                channel = (String) cbChannels[i].getText();
                            } else {
                                channel = channel + "," + cbChannels[i].getText();
                            }
                        }
                    }
                }

                handler.deliver(channel, new MASCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        DialogFragment otpAuthFragment = MASOtpAuthFragment.newInstance(handler);
                        otpAuthFragment.show(getActivity().getFragmentManager(), "OTPDialog");
                        dismiss();
                    }

                    @Override
                    public void onError(Throwable e) {
                        isRequestProcessing = false;
                        Activity activity = getActivity();
                        if (activity instanceof MASErrorMessageListener) {
                            String errorMessage = "OTP delivery failure";
                            if (e instanceof MAGServerException) {
                                int errorCode = ((MAGServerException) e).getErrorCode();
                                String errorCodeString = Integer.toString(errorCode);
                                if (errorCodeString.endsWith("140")) {
                                    errorMessage = getResources().getString(R.string.errorCode140);
                                } else if (errorCodeString.endsWith("142")) {
                                    errorMessage = getResources().getString(R.string.errorCode142);
                                } else if (errorCodeString.endsWith("143")) {
                                    errorMessage = getResources().getString(R.string.errorCode143);
                                } else if (errorCodeString.endsWith("144")) {
                                    errorMessage = getResources().getString(R.string.errorCode144);
                                } else if (errorCodeString.endsWith("145")) {
                                    errorMessage = getResources().getString(R.string.errorCode145);
                                }
                            }
                            ((MASErrorMessageListener) activity).getErrorMessage(errorMessage);
                        }
                        handler.cancel();
                        dismiss();
                    }
                });
            }
        });
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.cancel();
                isRequestProcessing = false;
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if (activity instanceof MASFragmentAttachDismissListener)
            ((MASFragmentAttachDismissListener) activity).handleDialogOpen(activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if (activity instanceof MASFragmentAttachDismissListener)
            ((MASFragmentAttachDismissListener) activity).handleDialogClose(activity, isRequestProcessing);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if (activity instanceof MASFragmentAttachDismissListener)
            ((MASFragmentAttachDismissListener) activity).handleDialogClose(activity, isRequestProcessing);
        if (activity instanceof MASErrorMessageListener)
            ((MASErrorMessageListener) activity).getErrorMessage(errorMessage);
    }
}
