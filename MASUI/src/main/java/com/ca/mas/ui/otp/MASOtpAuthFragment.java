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
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ca.mas.core.auth.otp.OtpConstants;
import com.ca.mas.foundation.MASOtpAuthenticationHandler;
import com.ca.mas.ui.R;
import com.ca.mas.ui.listener.MASErrorMessageListener;
import com.ca.mas.ui.listener.MASFragmentAttachDismissListener;


/**
 * Sample to show a Otp authentication dialog as a Fragment
 */
public class MASOtpAuthFragment extends DialogFragment {

    private static final String TAG = MASOtpAuthFragment.class.getCanonicalName();

    private static final String HANDLER = "handle";

    private MASOtpAuthenticationHandler handler;

    String errorMessage = "Request Cancelled";
    boolean isRequestProcessing=false;


    /**
     * Use this factory method to create a new instance of
     * this fragment
     *
     * @return A new instance of fragment MASOtpAuthFragment.
     */
    public static MASOtpAuthFragment newInstance(MASOtpAuthenticationHandler handler) {
        MASOtpAuthFragment fragment = new MASOtpAuthFragment();
        Bundle args = new Bundle();
        args.putParcelable(HANDLER, handler);
        fragment.setArguments(args);
        return fragment;
    }

    public MASOtpAuthFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        if (getArguments() != null) {
            handler = getArguments().getParcelable(HANDLER);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_otp_auth, container, false);

        boolean isInactiveOtp = getArguments().getBoolean(OtpConstants.IS_INVALID_OTP);
        if (isInactiveOtp ) {
            CharSequence errorMsg = "Authentication failed due to an invalid OTP.";
            TextView tvErrorMsg = (TextView) view.findViewById(R.id.tvErrorMsg);
            tvErrorMsg.setText(errorMsg);
        }
        Button button = (Button) view.findViewById(R.id.btnValidateOtp);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRequestProcessing=true;
                final String otp = ((EditText) view.findViewById(R.id.etxtOtp)).getText().toString();
                if (otp  == null || "".equals(otp.trim())) {
                    return;
                }
                handler.proceed(getActivity().getBaseContext(), otp);
                errorMessage="Processing Request";
                dismiss();
            }

        });
        Button btnCancel = (Button) view.findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isRequestProcessing=false;
                handler.cancel();
                errorMessage = "Request Cancelled";
                dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();

    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        handler.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        Activity activity = getActivity();
        if(activity instanceof MASFragmentAttachDismissListener)
            ((MASFragmentAttachDismissListener)activity).handleDialogOpen(activity);
    }

    @Override
    public void onPause() {
        super.onPause();
        Activity activity = getActivity();
        if(activity instanceof MASFragmentAttachDismissListener)
            ((MASFragmentAttachDismissListener)activity).handleDialogClose(activity,isRequestProcessing);
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if(activity instanceof MASFragmentAttachDismissListener)
            ((MASFragmentAttachDismissListener)activity).handleDialogClose(activity,isRequestProcessing);
        if(activity instanceof MASErrorMessageListener)
            ((MASErrorMessageListener)activity).getErrorMessage(errorMessage);
    }
}
